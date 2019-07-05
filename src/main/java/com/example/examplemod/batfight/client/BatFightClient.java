package com.example.examplemod.batfight.client;

import com.example.examplemod.batfight.common.BatAnimation;
import com.example.examplemod.batfight.common.BatFight;
import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;

@SideOnly(Side.CLIENT)
public class BatFightClient extends BatFight implements HackFMLEventListener {
	private HashSet<BatAnimation> animations = new HashSet<>();

	public BatFightClient() {
		subscribeToFMLEvents();
	}

	@Override
	public void handleFMLEvent(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void attackEntityEventHandler(AttackEntityEvent event) {
		Entity target = event.getTarget();
		// only worry about these events on the client side, since server side
		// doesn't do animation.
		if (target.world.isRemote) return;

		if (target instanceof EntityAnimal) {
			animations.add(new BatMessageAnimation());
		}
	}

	@SubscribeEvent
	public void onRenderGui(RenderGameOverlayEvent.Post event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) return;
		Minecraft mc = Minecraft.getMinecraft();
		HashSet<BatAnimation> deadAnimations = new HashSet<>();
		for (BatAnimation b : animations) {
			if (b.doStuff()) {
				deadAnimations.add(b);
			}
		}
		animations.removeAll(deadAnimations);
	}
}
