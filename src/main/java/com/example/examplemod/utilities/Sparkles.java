package com.example.examplemod.utilities;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import java.util.Random;

public class Sparkles {
	private static final int SPARK_SPEED_MAX = 10;
	private static final int SPARK_SPEED_MIN = 5;
	private static final double SPARK_SPEED_DIVISOR = 8.0D;
	private static final Random rand = new Random();

	public static void yay(World world, double posX, double posY, double posZ, EnumParticleTypes sparkleType) {
		double x = (double) (rand.nextInt(SPARK_SPEED_MAX) - SPARK_SPEED_MIN) / SPARK_SPEED_DIVISOR;
		double y = (double) (rand.nextInt(SPARK_SPEED_MAX) - SPARK_SPEED_MIN) / SPARK_SPEED_DIVISOR;
		double z = (double) (rand.nextInt(SPARK_SPEED_MAX) - SPARK_SPEED_MIN) / SPARK_SPEED_DIVISOR;
		world.spawnParticle(sparkleType, posX, posY, posZ, x, y, z);
	}
}
