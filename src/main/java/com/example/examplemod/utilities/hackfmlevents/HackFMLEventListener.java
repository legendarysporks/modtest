package com.example.examplemod.utilities.hackfmlevents;

import net.minecraftforge.fml.common.event.*;

public interface HackFMLEventListener {
	default void subscribeToFMLEvents() {
		HackFMLEventBus.FMLEventBus.subscribe(this);
	}

	default void unsubscribeToFMLEvents() {
		HackFMLEventBus.FMLEventBus.unsubscribe(this);
	}

	default void handleFMLEvent(FMLPreInitializationEvent event) {
	}

	default void handleFMLEvent(FMLInitializationEvent event) {
	}

	default void handleFMLEvent(FMLPostInitializationEvent event) {
	}

	default void handleFMLEvent(FMLServerAboutToStartEvent event) {
	}

	default void handleFMLEvent(FMLServerStartingEvent event) {
	}

	default void handleFMLEvent(FMLServerStartedEvent event) {
	}

	default void handleFMLEvent(FMLServerStoppingEvent event) {
	}

	default void handleFMLEvent(FMLServerStoppedEvent event) {
	}
}
