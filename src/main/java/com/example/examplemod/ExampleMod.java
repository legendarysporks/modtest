package com.example.examplemod;

import com.example.examplemod.expodingsheep.ExplodingAnimals;
import com.example.examplemod.init.ModBlocks;
import com.example.examplemod.init.ModItems;
import com.example.examplemod.init.ModRecipes;
import com.example.examplemod.utilities.hackfmlevents.HackFMLEventBus;
import com.example.examplemodtests.testUtilities.RunTestsCommand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, version = Reference.VERSION, acceptedMinecraftVersions = Reference.ACCEPTED_MINECRAFT_VERSIONS)
public class ExampleMod {
	@Mod.Instance
	public static ExampleMod instance;
	public static Logger logger;
	/**
	 * Instance of ExplodingAnimals
	 */
	private ExplodingAnimals explodingAnimals;
	private RunTestsCommand runTestCommand;

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

		ModItems.init();
		ModBlocks.init();
		ModRecipes.init();

		explodingAnimals = new ExplodingAnimals();
		runTestCommand = new RunTestsCommand();

		// notify listeners last so the rest of initialization/construction is done before they are called.
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		logTrace(Reference.MODID + ": init");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		logTrace(Reference.MODID + ": postInit");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerAboutToStartEvent event) {
		logTrace("Server about to start");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStartingEvent event) {
		logTrace("Server starting");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStartedEvent event) {
		logTrace("Server started");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStoppingEvent event) {
		logTrace("Server stopping");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStoppedEvent event) {
		logTrace("Server stopped");
		HackFMLEventBus.FMLEventBus.publish(event);
	}
}
