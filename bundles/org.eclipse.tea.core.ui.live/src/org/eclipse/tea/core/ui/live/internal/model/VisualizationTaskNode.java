package org.eclipse.tea.core.ui.live.internal.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tea.core.TaskExecutionContext;
import org.eclipse.tea.core.TaskingInjectionHelper;
import org.eclipse.tea.core.internal.TaskProgressExtendedTracker;
import org.eclipse.tea.core.internal.TaskProgressExtendedTracker.ProgressListener;
import org.eclipse.tea.core.internal.listeners.TaskingStatusTracker;
import org.eclipse.tea.core.internal.model.TaskingModel;
import org.eclipse.tea.core.ui.live.internal.Refreshable;

public class VisualizationTaskNode implements VisualizationNode, ProgressListener {

	private final String name;
	private final int maxProgress;
	private final Object task;
	private final Refreshable refreshable;
	private IStatus status = Status.OK_STATUS;
	private int currentProgress;
	private long duration = -1;
	private boolean active;
	private boolean skipped;

	private final List<VisualizationStatusNode> statusNodes = new ArrayList<>();

	@Inject
	public VisualizationTaskNode(@Named(TaskingInjectionHelper.CTX_TASK) Object task,
			@Named(TaskingInjectionHelper.CTX_TASK_WORK_AMOUNT) Integer work, Refreshable refreshable) {
		this.task = task;
		this.refreshable = refreshable;
		this.name = TaskingModel.getTaskName(task);
		this.maxProgress = work;
	}

	public Object getTask() {
		return task;
	}

	public void begin(TaskProgressExtendedTracker tracker) {
		tracker.addListener(this);

		active = true;
	}

	public void done(TaskProgressExtendedTracker tracker, TaskExecutionContext ctx, TaskingStatusTracker states) {
		tracker.removeListener(this);

		active = false;

		status = states.getStatus(ctx, task);
		duration = states.getDuration(ctx, task);

		if (status.getSeverity() > IStatus.OK) {
			if (status.isMultiStatus()) {
				for (IStatus s : status.getChildren()) {
					statusNodes.add(new VisualizationStatusNode(s));
				}
			} else {
				statusNodes.add(new VisualizationStatusNode(status));
			}
		}

		this.currentProgress = maxProgress;
	}

	public void skip() {
		duration = 0;
		skipped = true;
		status = Status.CANCEL_STATUS;
	}

	public boolean isSkipped() {
		return skipped;
	}

	public List<VisualizationStatusNode> getStatusNodes() {
		return statusNodes;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getTotalProgress() {
		return maxProgress;
	}

	@Override
	public int getCurrentProgress() {
		return currentProgress;
	}

	@Override
	public void progressChanged(int currentProgress) {
		if (this.currentProgress == currentProgress) {
			return; // inhibit events.
		}

		if ((currentProgress - this.currentProgress) * 100L < maxProgress) {
			return; // less then 1% progress - not significant: don't draw
		}
		this.currentProgress = currentProgress;
		this.refreshable.refresh();
	}

	@Override
	public IStatus getStatus() {
		return status;
	}

	@Override
	public long getDuration() {
		return duration;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isDone() {
		return duration != -1;
	}

}
