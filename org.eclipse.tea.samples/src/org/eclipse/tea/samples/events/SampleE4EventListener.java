package org.eclipse.tea.samples.events;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.tea.core.services.TaskingLog;
import org.eclipse.tea.core.ui.internal.listeners.EventBrokerBridge;
import org.osgi.service.component.annotations.Activate;
//import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

@SuppressWarnings("restriction")
// uncomment this and the import to enable the listener.
// @Component(immediate = true)
public class SampleE4EventListener implements EventHandler {

	@Activate
	public void activate() {
		IEventBroker broker = E4Workbench.getServiceContext().get(IEventBroker.class);
		broker.subscribe(EventBrokerBridge.EVENT_TOPIC_BASE + "*", null, SampleE4EventListener.this, true);
	}

	@Override
	public void handleEvent(Event event) {
		Object data = event.getProperty(IEventBroker.DATA);
		if (!(data instanceof IEclipseContext)) {
			return;
		}
		IEclipseContext eventContext = (IEclipseContext) data;
		IEclipseContext child = eventContext.createChild("My Processing Context");

		child.set(Event.class, event);

		ContextInjectionFactory.invoke(this, Execute.class, child);
	}

	@Execute
	public void process(TaskingLog log, Event event) {
		log.info("received " + event.getTopic());
	}

}
