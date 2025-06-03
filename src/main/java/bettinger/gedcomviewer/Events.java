package bettinger.gedcomviewer;

import com.google.common.eventbus.EventBus;

public abstract class Events {

	private static final EventBus eventBus = new EventBus();

	public static void register(final Object object) {
		if (object != null) {
			eventBus.register(object);
		}
	}

	public static void unregister(final Object object) {
		if (object != null) {
			eventBus.unregister(object);
		}
	}

	public static void post(final Object event) {
		if (event != null) {
			eventBus.post(event);
		}
	}

	private Events() {}
}
