package com.example.examplemod.emp.common;

import com.example.examplemod.Reference;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import com.example.examplemod.ExampleMod;

public class EMPProjectile extends EntityThrowable {
	private static float GRAVITY = 0.0f;
	private static int SPARKINESS = 1;

	private static final String NAME = "emp_projectile";
	private static final int ID = 120;
	private EntityLivingBase launcher;

	public EMPProjectile(World world) {
		super(world);
	}

	public EMPProjectile(World world, EntityLivingBase entity) {
		super(world, entity);
		launcher = entity;
	}

	public static void registerModEntity() {
		EntityRegistry.registerModEntity(
				new ResourceLocation(Reference.MODID, NAME),
				EMPProjectile.class,
				NAME,
				ID,
				ExampleMod.instance,
				64,
				10,
				true);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (ticksExisted > 20) {
			explode();
		}

		for (int i = 0; i < SPARKINESS; i++) {
			double x = (double) (rand.nextInt(10) - 5) / 8.0D;
			double y = (double) (rand.nextInt(10) - 5) / 8.0D;
			double z = (double) (rand.nextInt(10) - 5) / 8.0D;
			this.world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, posX, posY, posZ, x, y, z);
		}
	}

	@Override
	protected float getGravityVelocity() {
		return GRAVITY;
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (!world.isRemote) {
			if (result.entityHit == launcher) {
				return;
			}
			explode();
		}
	}

	private void explode() {
		setDead();
		this.world.createExplosion(this, posX, posY, posZ, 1.75F, true);
	}
}