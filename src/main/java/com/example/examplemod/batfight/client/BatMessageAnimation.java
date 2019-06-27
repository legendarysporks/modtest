package com.example.examplemod.batfight.client;

import com.example.examplemod.batfight.common.BatFightWords;
import com.example.examplemod.batfight.common.PeriodicBatAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class BatMessageAnimation extends PeriodicBatAnimation {
	// Alpha channel values from 100% to 0
	private static int[] alphaValue = {
			0x00000000,
			0xF0000000,
			0xE0000000,
			0xD0000000,
			0xC0000000,
			0xB0000000,
			0xA0000000,
			0x90000000,
			0x80000000,
			0x70000000,
			0x60000000,
			0x50000000,
			0x40000000,
			0x30000000,
			0x20000000,
			0x10000000,
	};
	private static long frameRate = 126;

	private final String text;
	private final float x;
	private final float y;

	public BatMessageAnimation() {
		super(frameRate);

		// get the word and BOLD it with a formatting code
		// https://minecraft.gamepedia.com/Formatting_codes
		text = BatFightWords.getWord();

		// get the display size in Minecraft pixels
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution scaled = new ScaledResolution(mc);
		int displayWidth = scaled.getScaledWidth();
		int displayHeight = scaled.getScaledHeight();
		FontRenderer renderer = mc.fontRenderer;

		// place text anywhere from left to right edge of display
		int textWidth = renderer.getStringWidth(text);
		int xWiggleRoom = displayWidth - textWidth;
		Random rand = new Random();
		x = rand.nextInt(xWiggleRoom);

		// place text in top half of display
		int yWiggleRoom = (displayHeight / 2) - (renderer.FONT_HEIGHT);
		y = rand.nextInt(yWiggleRoom);
	}

	@Override
	protected boolean doStuff(long elapsedTime, long elapsedFrameTime, int frameIndex, boolean frameChanged) {
		if (frameIndex >= alphaValue.length) {
			// we've rendered all frames, don't do anything and tell the caller
			// to stop bothering us
			return true;
		} else {
			// muck with the alpha channel of the color
			int color = 0x00FFAA00 | alphaValue[frameIndex % alphaValue.length];

			// draw the string
			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);

			// tell caller we're not done yet
			return false;
		}
	}
}
