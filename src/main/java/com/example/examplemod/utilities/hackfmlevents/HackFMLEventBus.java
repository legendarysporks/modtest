package com.example.examplemod.utilities.hackfmlevents;

import net.minecraftforge.fml.common.event.*;

import java.util.ArrayList;
import java.util.List;

/*
Simple class used to send FLMEvents to multiple subscribers.
A REAL @Mod needs to publish the FML events it receives.
 */
public class HackFMLEventBus {
	/**
	 * Since FML events are only dispatched to the main mod class, we want to be able to pass
	 * them on to interested code ourselves.  These queues essentially allow us to do that.
	 * Code interested in an event adds itself to the notifier and when then even fires the
	 * event will be passed on to its handle method
	 */
	public static final HackFMLEventBus FMLEventBus = new HackFMLEventBus();

	private List<HackFMLEventListener> listeners = new ArrayList<>();

	public void subscribe(HackFMLEventListener listener) {
		listeners.add(listener);
	}

	public void unsubscribe(HackFMLEventListener listener) {
		listeners.remove(listener);
	}

	public void publish(FMLPreInitializationEvent event) {
		for (HackFMLEventListener listerner : listeners) {
			listerner.handleFMLEvent(event);
		}
	}

	public void publish(FMLInitializationEvent event) {
		for (HackFMLEventListener listerner : listeners) {
			listerner.handleFMLEvent(event);
		}
	}

	public void publish(FMLPostInitializationEvent event) {
		for (HackFMLEventListener listerner : listeners) {
			listerner.handleFMLEvent(event);
		}
	}

	public void publish(FMLServerAboutToStartEvent event) {
		for (HackFMLEventListener listerner : listeners) {
			listerner.handleFMLEvent(event);
		}
	}

	public void publish(FMLServerStartingEvent event) {
		for (HackFMLEventListener listerner : listeners) {
			listerner.handleFMLEvent(event);
		}
	}

	public void publish(FMLServerStartedEvent event) {
		for (HackFMLEventListener listerner : listeners) {
			listerner.handleFMLEvent(event);
		}
	}

	public void publish(FMLServerStoppingEvent event) {
		for (HackFMLEventListener listerner : listeners) {
			listerner.handleFMLEvent(event);
		}
	}

	public void publish(FMLServerStoppedEvent event) {
		for (HackFMLEventListener listerner : listeners) {
			listerner.handleFMLEvent(event);
		}
	}

	public void reset() {
		listeners = new ArrayList<>();
	}

	public void close() {
		listeners = null;
	}
}
