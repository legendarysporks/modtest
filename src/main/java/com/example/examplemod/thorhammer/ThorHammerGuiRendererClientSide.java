package com.example.examplemod.thorhammer;

import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ThorHammerGuiRendererClientSide extends ThorHammerGuiRenderer implements HackFMLEventListener {
	private static final int DONT_RENDER = -1;
	private static final int MARGIN = 50;
	private int ticksLeft = DONT_RENDER;
	private int timeToCharge;

	public ThorHammerGuiRendererClientSide() {
		subscribeToFMLEvents();
	}

	@Override
	public void handleFMLEvent(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onUseItem(LivingEntityUseItemEvent.Start event) {
		timeToCharge = event.getDuration();
		ticksLeft = event.getDuration();
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
		ticksLeft = DONT_RENDER;
	}

	//	@SubscribeEvent
	public void onRenderGuiAsText(RenderGameOverlayEvent.Post event) {
		if (ticksLeft != DONT_RENDER) {
			// Client Side method only
			Minecraft mc = Minecraft.getMinecraft();
			ScaledResolution scaled = new ScaledResolution(mc);
			int displayWidth = scaled.getScaledWidth();
			int displayHeight = scaled.getScaledHeight();
			FontRenderer renderer = mc.fontRenderer;

			// place text anywhere from left to right edge of display
			int ticksUsed = timeToCharge - ticksLeft;
			String text = Integer.toString(ticksUsed * 100 / timeToCharge) + "%";
			text = "    ".substring(text.length()) + text;
			int textWidth = renderer.getStringWidth(text);
			int x = displayWidth - textWidth - MARGIN;
			int y = renderer.FONT_HEIGHT + MARGIN;
			int color = 0x00FFAA00;
			int grey = 0x00211600;

			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GL11.glPushMatrix();
			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);
			GL11.glPopMatrix();
			GL11.glPopAttrib();
		}
	}

	@SubscribeEvent
	public void onRenderGuiAsBox(RenderGameOverlayEvent.Post event) {
		if (ticksLeft != DONT_RENDER) {
			final int top = 10;
			final int left = 10;
			final int color = 0x00FFAA00;
			final int grey = 0x00211600;
			final int red = 0x000000FF;
			final int boxHeight = 5;
			final int boxWidth = 50;

			// place text anywhere from left to right edge of display
			int ticksUsed = timeToCharge - ticksLeft;

			int bottom = top + boxHeight;
			int right = left + boxWidth;

			GlStateManager.pushAttrib();
			GlStateManager.pushMatrix();

			GlStateManager.translate(0, 0, 0);
			GlStateManager.scale(1, 1, 1);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableAlpha();

			//draw box around charge indicator
			Gui.drawRect(left, top + 1, left + 1, bottom, red);
			Gui.drawRect(left, top, right + 1, top + 1, red);
			Gui.drawRect(left, bottom, right + 1, bottom + 1, red);
			Gui.drawRect(right, top + 1, right + 1, bottom, red);

			right = left + boxWidth * ticksUsed / timeToCharge;

			Gui.drawRect(left, top, right, bottom, red);

			GlStateManager.popMatrix();
			GlStateManager.popAttrib();
		}
	}
}
