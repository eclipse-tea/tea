package org.eclipse.tea.samples.tasks;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.tea.core.MarkerStatus;
import org.eclipse.tea.core.services.TaskingLog;

public class SampleMarkerStatusTask {

	@Execute
	public IStatus run(TaskingLog log) {
		IMarker marker = getAnyMarker();

		if (marker == null) {
			return new Status(IStatus.WARNING, "my.plugin.id", "Cannot find any problem");
		}

		String location = marker.getAttribute(IMarker.LOCATION,
				marker.getResource().getProjectRelativePath().toOSString());

		return new MarkerStatus(IStatus.WARNING, "my.plugin.id", "Double-click to go to " + location + ")", marker);
	}

	private IMarker getAnyMarker() {
		try {
			return ResourcesPlugin.getWorkspace().getRoot().findMarkers(IMarker.PROBLEM, true,
					IResource.DEPTH_INFINITE)[0];
		} catch (Exception e) {
			return null;
		}
	}

}
