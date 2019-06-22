package com.example.examplemod.batfight.common;

import com.example.examplemod.utilities.HackFMLEventListener;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/*
Generic BatFight interface used by the main mod.  Just used to initialize the
fight.
 */
public class BatFight implements HackFMLEventListener {
	@SidedProxy(clientSide = "com.example.examplemod.batfight.client.BatFightClient",
			serverSide = "com.example.examplemod.batfight.server.BatFightServer")
	public static BatFight proxy;
	public static BatFightCommand command;

	public BatFight() {
		subscribeToFMLEvents();
	}

	@Override
	public void handleFMLEvent(FMLPreInitializationEvent event) {
		command = new BatFightCommand();
	}
}
