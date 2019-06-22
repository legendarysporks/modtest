package com.example.examplemod.utilities;

import net.minecraftforge.fml.common.event.*;

import java.util.ArrayList;
import java.util.List;

/*
Simple class used to send FLMEvents to multiple subscribers.
 */
public class HackFMLEventBus {
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

	public void close() {
		listeners = null;
	}
}
