package com.example.examplemod.thorhammer;

import com.example.examplemod.utilities.RendererHelper;
import net.minecraftforge.fml.common.SidedProxy;

public class ThorHammerProjectileRenderer extends RendererHelper {
	@SidedProxy(clientSide = "com.example.examplemod.thorhammer.ThorHammerProjectileRendererClientSide",
			serverSide = "com.example.examplemod.utilities.RendererHelper")
	public static RendererHelper proxy;
}
