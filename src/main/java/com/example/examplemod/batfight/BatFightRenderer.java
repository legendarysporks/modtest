package com.example.examplemod.batfight;

import net.minecraftforge.fml.common.SidedProxy;

public class BatFightRenderer {
	@SidedProxy(clientSide = "com.example.examplemod.batfight.BatFightRendererClientSide",
			serverSide = "com.example.examplemod.batfight.BatFightRenderer")
	public static BatFightRenderer proxy;
}
