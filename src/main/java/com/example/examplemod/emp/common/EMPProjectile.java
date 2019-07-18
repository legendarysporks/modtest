package com.example.examplemod.emp.common;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.Reference;
import com.example.examplemod.utilities.Sparkles;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import static net.minecraft.util.math.RayTraceResult.Type.*;

public class EMPProjectile extends EntityThrowable {
	private static final String NAME = "emp_projectile";
	private static final int ID = 120;
	private static float GRAVITY = 0.0f;
	private static int LIFETIME_TICKS = 20;
	private static float EXPLOSION_STRENGTH = 1.75F;
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
		if (ticksExisted > LIFETIME_TICKS) {
			explode();
		}
		Sparkles.yay(world, posX, posY, posZ, EnumParticleTypes.FIREWORKS_SPARK);
	}

	@Override
	protected float getGravityVelocity() {
		return GRAVITY;
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		/* The contents of a RayTraceResult differ depending on its Type field:
				BLOCK
					blockPos
					sideHit
					hitVec
				ENTITY
					entityHit
					hitVec
				MISS
		 */
		if (!world.isRemote) {
			if (result.typeOfHit == MISS) {
			} else if (result.typeOfHit == BLOCK) {
				explode();
			} else if (result.typeOfHit == ENTITY) {
				if (result.entityHit != launcher) {
					explode();
				}
			}
		}
	}

	private void explode() {
		setDead();
		this.world.createExplosion(this, posX, posY, posZ, EXPLOSION_STRENGTH, true);
	}
}