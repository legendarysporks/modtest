package com.example.examplemod.utilities;

import net.minecraftforge.fml.common.event.*;

public interface HackFMLEventListener {
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
