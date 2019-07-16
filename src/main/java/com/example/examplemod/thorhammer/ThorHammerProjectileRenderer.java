package com.example.examplemod.thorhammer;

import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import net.minecraftforge.fml.common.SidedProxy;

public class ThorHammerProjectileRenderer implements HackFMLEventListener {

	@SidedProxy(clientSide = "com.example.examplemod.thorhammer.ThorHammerProjectileRendererClient",
			serverSide = "com.example.examplemod.thorhammer.ThorHammerProjectileRenderer")
	public static ThorHammerProjectileRenderer proxy;

	public ThorHammerProjectileRenderer() {
		subscribeToFMLEvents();
	}

	public ThorHammerProjectileRenderer init() {
		return this;
	}
}
