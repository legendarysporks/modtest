package com.example.examplemod;

import com.example.examplemod.expodingsheep.ExplodingAnimals;
import com.example.examplemod.init.ModBlocks;
import com.example.examplemod.init.ModItems;
import com.example.examplemod.init.ModRecipes;
import com.example.examplemod.utilities.HackFMLEventBus;
import com.example.examplemodtests.testUtilities.RunTestsCommand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, version = Reference.VERSION, acceptedMinecraftVersions = Reference.ACCEPTED_MINECRAFT_VERSIONS)
public class ExampleMod {
	/**
	 * Since FML events are only dispatched to the main mod class, we want to be able to pass
	 * them on to interested code ourselves.  These queues essentially allow us to do that.
	 * Code interested in an event adds itself to the notifier and when then even fires the
	 * event will be passed on to its handle method
	 */
	public static final HackFMLEventBus FMLEventBus = new HackFMLEventBus();
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
		FMLEventBus.publish(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		logTrace(Reference.MODID + ": init");
		FMLEventBus.publish(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		logTrace(Reference.MODID + ": postInit");
		FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerAboutToStartEvent event) {
		logTrace("Server about to start");
		FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStartingEvent event) {
		logTrace("Server starting");
		FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStartedEvent event) {
		logTrace("Server started");
		FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStoppingEvent event) {
		logTrace("Server stopping");
		FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStoppedEvent event) {
		logTrace("Server stopped");
		FMLEventBus.publish(event);
	}
}
