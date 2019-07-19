package com.example.examplemod.emp;

import com.example.examplemod.utilities.RendererHelper;
import net.minecraftforge.fml.common.SidedProxy;

public class EMPRenderer {
	@SidedProxy(clientSide = "com.example.examplemod.emp.EMPRendererClientSide",
			serverSide = "com.example.examplemod.utilities.RendererHelper")
	public static RendererHelper proxy;
}
