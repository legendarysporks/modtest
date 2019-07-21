package com.example.examplemod.thorhammer;

import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ThorHammerGuiRendererClientSide extends ThorHammerGuiRenderer implements HackFMLEventListener {
	private static final int DONT_RENDER = -1;
	private static final int MARGIN = 50;
	private int ticksLeft = DONT_RENDER;

	public ThorHammerGuiRendererClientSide() {
		subscribeToFMLEvents();
	}

	@Override
	public void handleFMLEvent(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onUseItem(LivingEntityUseItemEvent.Start event) {
	}

	@SubscribeEvent
	public void onUseItem(LivingEntityUseItemEvent.Tick event) {
		if (ticksLeft > 0) {
			ticksLeft--;
		}
	}

	@SubscribeEvent
	public void onUseItem(LivingEntityUseItemEvent.Stop event) {
		ticksLeft = DONT_RENDER;
	}

	@SubscribeEvent
	public void onUseItem(LivingEntityUseItemEvent.Finish event) {
	}

	@SubscribeEvent
	public void onWindUpEvent(ThorHammer.WindUpEvent event) {
		ticksLeft = event.chargeDuration;
	}

	@SubscribeEvent
	public void onThrowEvent(ThorHammer.ThrowEvent event) {
		ticksLeft = DONT_RENDER;
	}

	@SubscribeEvent
	public void onRenderGui(RenderGameOverlayEvent.Post event) {
		if (ticksLeft != DONT_RENDER) {
			// Client Side method only
			Minecraft mc = Minecraft.getMinecraft();
			ScaledResolution scaled = new ScaledResolution(mc);
			int displayWidth = scaled.getScaledWidth();
			int displayHeight = scaled.getScaledHeight();
			FontRenderer renderer = mc.fontRenderer;

			// place text anywhere from left to right edge of display
			int ticksUsed = ThorHammer.getTimeToCharge() - ticksLeft;
			String text = Integer.toString(ticksUsed * 100 / ThorHammer.getTimeToCharge()) + "%";
			text = "    ".substring(text.length()) + text;
			int textWidth = renderer.getStringWidth(text);
			int x = displayWidth - textWidth - MARGIN;
			int y = renderer.FONT_HEIGHT + MARGIN;
			int color = 0x00FFAA00;

			// draw the string
			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);
		}
	}
}
