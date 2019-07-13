package com.example.examplemod;

import com.example.examplemod.expodingsheep.ExplodingAnimals;
import com.example.examplemod.init.ModBlocks;
import com.example.examplemod.init.ModItems;
import com.example.examplemod.init.ModRecipes;
import com.example.examplemod.utilities.Logging;
import com.example.examplemod.utilities.eventsnoopers.EventSnooper;
import com.example.examplemod.utilities.hackfmlevents.HackFMLEventBus;
import com.example.examplemodtests.testUtilities.RunTestsCommand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, version = Reference.VERSION, acceptedMinecraftVersions = Reference.ACCEPTED_MINECRAFT_VERSIONS)
public class ExampleMod {
	@Mod.Instance
	public static ExampleMod instance;
	/**
	 * Instance of ExplodingAnimals
	 */
	private ExplodingAnimals explodingAnimals;
	private RunTestsCommand runTestCommand;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Logging.logger = event.getModLog();
		Logging.logTrace(Reference.MODID + ": preInit");

		ModItems.init();
		ModBlocks.init();
		ModRecipes.init();
		EventSnooper.init();

		explodingAnimals = new ExplodingAnimals();
		runTestCommand = new RunTestsCommand();

		// notify listeners last so the rest of initialization/construction is done before they are called.
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		Logging.logTrace(Reference.MODID + ": init");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Logging.logTrace(Reference.MODID + ": postInit");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerAboutToStartEvent event) {
		Logging.logTrace("Server about to start");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStartingEvent event) {
		Logging.logTrace("Server starting");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStartedEvent event) {
		Logging.logTrace("Server started");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStoppingEvent event) {
		Logging.logTrace("Server stopping");
		HackFMLEventBus.FMLEventBus.publish(event);
	}

	@EventHandler
	public void fmlLifeCycle(FMLServerStoppedEvent event) {
		Logging.logTrace("Server stopped");
		HackFMLEventBus.FMLEventBus.publish(event);
	}
}
