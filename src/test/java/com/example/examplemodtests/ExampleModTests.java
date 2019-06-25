package com.example.examplemodtests;

import com.example.examplemod.Reference;
import com.example.examplemod.utilities.HackFMLEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@Mod(modid = ExampleModTests.MODID, name = ExampleModTests.MODNAME, version = ExampleModTests.VERSION, acceptedMinecraftVersions = ExampleModTests.ACCEPTED_MINECRAFT_VERSIONS)
public class ExampleModTests {
	public static final String MODID = "evansmodtests";
	public static final String MODNAME = "Evan's Mod Tests";
	public static final String VERSION = "1.0";
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12]";

	@Mod.Instance
	public static ExampleModTests instance;

	private static Logger logger;

	/**
	 * Logging method that is safe to call even before the logger has been properly initialized.  Kind
	 * of a hack for debugging startup code.
	 */
	public static void logInfo(String info) {
		log(Level.INFO, info);
	}

	public static void logTrace(String info) {
		log(Level.INFO, info);
	}

	public static void log(Level level, String info) {
		if (logger != null) {
			logger.log(level, info);
		} else {
			System.out.println(Reference.MODNAME + "." + level + ":" + info);
		}
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		logTrace(Reference.MODID + ": preInit");
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		logTrace(Reference.MODID + ": init");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		logTrace(Reference.MODID + ": postInit");
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerAboutToStartEvent event) {
		logTrace("Server about to start");
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStartingEvent event) {
		logTrace("Server starting");
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStartedEvent event) {
		logTrace("Server started");
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStoppingEvent event) {
		logTrace("Server stopping");
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStoppedEvent event) {
		logTrace("Server stopped");
	}
}
