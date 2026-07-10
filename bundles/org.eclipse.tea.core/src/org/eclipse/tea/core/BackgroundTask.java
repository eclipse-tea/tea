/*******************************************************************************
 *  Copyright (c) 2017 SSI Schaefer IT Solutions GmbH and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      SSI Schaefer IT Solutions GmbH
 *******************************************************************************/
package org.eclipse.tea.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.tea.core.internal.TaskProgressEstimationService;
import org.eclipse.tea.core.internal.TaskingEngineActivator;
import org.eclipse.tea.core.internal.model.TaskingModel;
import org.eclipse.tea.core.services.TaskProgressTracker;
import org.eclipse.tea.core.services.TaskProgressTracker.TaskProgressProvider;

public class BackgroundTask {

	private static final ExecutorService backgroundTasks = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private final Object task;
	private Future<IStatus> future;

	public BackgroundTask(Object task) {
		Object actualTask;
		if (task instanceof Class) {
			try {
				actualTask = ((Class<?>) task).getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				throw new IllegalStateException("Cannot create task " + task);
			}
		} else {
			actualTask = task;
		}

		this.task = actualTask;
	}

	private static final TaskProgressTracker EMPTY_PROGRESS_TRACKER = new TaskProgressTracker() {

		@Override
		public void worked(int amount) {

		}

		@Override
		public boolean isCanceled() {
			return false;
		}

		@Override
		public void setTaskName(String name) {

		}
	};

	@Execute
	public void run(IEclipseContext context) throws Exception {
		future = backgroundTasks.submit(() -> {
			IEclipseContext taskCtx = context.createChild();
			ContextInjectionFactory.invoke(task, Execute.class, context);
			return taskCtx.get(IStatus.class);
		});
	}

	public Object barrier() {
		return new Object() {
			@Execute
			public Object doWait(TaskProgressEstimationService progressService, TaskProgressTracker rootTracker)
					throws Exception {
				String estimationId = progressService.calculateId(task);
				if (estimationId != null) {
					progressService.begin(estimationId, EMPTY_PROGRESS_TRACKER);
				}
				Future<?> ticker = tickProgressTracker(rootTracker, getWork(progressService));
				IStatus taskStatus = future.get();
				if (estimationId != null) {
					progressService.finish(estimationId, taskStatus);
				}
				ticker.cancel(true);
				return taskStatus;
			}

			@Override
			public String toString() {
				return "Wait for: " + BackgroundTask.this.toString();
			}

			@TaskProgressProvider
			public int getWork(TaskProgressEstimationService progressService) {
				String estimationId = progressService.calculateId(task);
				return estimationId == null ? 1 : progressService.getEstimatedTicks(estimationId);
			}
		};
	}

	public static Object allBarrier(List<Object> tasks) {
		List<BackgroundTask> toAwait = new ArrayList<>();
		int hash = 0;
		for (Object task : tasks) {
			if (task instanceof BackgroundTask) {
				toAwait.add((BackgroundTask) task);
				hash ^= ((BackgroundTask) task).task.getClass().getName().hashCode();
			}
		}
		if (toAwait.isEmpty()) {
			return null;
		}
		String estimationId = "await_async_" + tasks.size() + "_" + hash;

		return new Object() {
			@Execute
			public void doWaitAll(IEclipseContext context, TaskProgressTracker tracker,
					TaskProgressEstimationService progressService) throws Exception {
				Future<?> ticker = tickProgressTracker(tracker, getWork(progressService));
				progressService.begin(estimationId, EMPTY_PROGRESS_TRACKER);
				int severity = 0;
				for (BackgroundTask bt : toAwait) {
					severity |= bt.future.get().getSeverity();
				}
				progressService.finish(estimationId, new Status(severity, TaskingEngineActivator.PLUGIN_ID,
						"Finished waiting for background tasks."));
				ticker.cancel(true);
			}

			@Override
			public String toString() {
				return "Await unfinished background tasks.";
			}

			@TaskProgressProvider
			public int getWork(TaskProgressEstimationService progressService) {
				return progressService.getEstimatedTicks(estimationId);
			}
		};
	}

	private static Future<?> tickProgressTracker(TaskProgressTracker rootTracker, long estimationTicks) {
		int resolution = 100;
		return backgroundTasks.submit(() -> {
			long remaining = estimationTicks;
			while (remaining > 0) {
				try {
					Thread.sleep(resolution);
				} catch (InterruptedException e) {
					return; // expected if task completes earlier
				}
				rootTracker.worked(1);
				remaining--;
			}
		});
	}

	@Override
	public String toString() {
		return TaskingModel.getTaskName(task) + " (parallel)";
	}

}
