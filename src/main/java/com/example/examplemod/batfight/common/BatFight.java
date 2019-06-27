package com.example.examplemod.batfight.common;

import net.minecraftforge.fml.common.SidedProxy;

/*
Generic BatFight interface used by the main mod.  Just used to initialize the
fight.
 */
public class BatFight {
	@SidedProxy(clientSide = "com.example.examplemod.batfight.client.BatFightClient",
			serverSide = "com.example.examplemod.batfight.server.BatFightServer")
	public static BatFight proxy;
	public static BatFightCommand command;

	public BatFight() {
		command = new BatFightCommand();
	}
}
