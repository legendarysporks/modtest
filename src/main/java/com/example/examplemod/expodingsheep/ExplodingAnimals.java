package com.example.examplemod.expodingsheep;

import com.example.examplemod.utilities.GenericSettings.Setting;
import com.example.examplemod.utilities.HackFMLEventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Random;


@Mod.EventBusSubscriber
@SideOnly(Side.CLIENT)
public class ExplodingAnimals implements HackFMLEventListener {
	private static class TriggerInfo {
		private static final int defaulChance = 25;     //chances in 100 entity will explode when attacked
		private static final int defaultBurnTime = 5;   //seconds animal burns and is "primed" to explode
		private static final float defaultDamage = 3;   //4 = tnt
		private static final boolean defaultSmoking = true; //smoke after explosion

		public boolean enabled;
		public final String name;
		public final Class<? extends EntityAnimal> targerAnimal;
		public final int chanceToExplode;
		public final int burnTime;
		public final float explosionDamage;
		public final boolean smoking;
		private final Random rand = new Random();

		public TriggerInfo(String animalName, Class<? extends EntityAnimal> animal) {
			name = animalName;
			enabled = true;
			targerAnimal = animal;
			chanceToExplode = defaulChance;
			burnTime = defaultBurnTime;
			explosionDamage = defaultDamage;
			smoking = defaultSmoking;
		}

		public void handleAttach(World world, EntityPlayer player, EntityAnimal animal) {
			if (animal.isBurning()) {
				// if you hit it while it's burning...BOOM
				explode(world, animal);
			} else if (rand.nextInt(100) <= chanceToExplode) {
				if (burnTime > 0) {
					// if it burns first, set it on fire
					animal.setFire(burnTime);
				} else {
					// if it doesn't burn, too bad for you.  BOOM!
					explode(world, animal);
				}
			}
		}

		private void explode(World world, EntityAnimal animal) {
			animal.setDead();
			world.createExplosion(null, animal.posX, animal.posY, animal.posZ, explosionDamage, smoking);
		}
	}

	private static final TriggerInfo triggers[] = {
			new TriggerInfo("cow", EntityCow.class),
			new TriggerInfo("chicken", EntityChicken.class),
			new TriggerInfo("pig", EntityPig.class),
			new TriggerInfo("sheep", EntitySheep.class),
	};

	private static final HashMap<Class<? extends EntityAnimal>, TriggerInfo> typeToInfoMap = new HashMap<>();
	private static final HashMap<String, TriggerInfo> nameToInfoMap = new HashMap<>();

	{
		for (TriggerInfo info : triggers) {
			typeToInfoMap.put(info.targerAnimal, info);
			nameToInfoMap.put(info.name, info);
		}
	}

	@Setting
	private boolean enabled = true;

	public ExplodingAnimals() {
		subscribeToFMLEvents();
	}

	@Override
	public void handleFMLEvent(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void attackEntityEventHandler(AttackEntityEvent event) {
		if (!enabled) return;
		Entity target = event.getTarget();
		World world = target.world;
		if (world.isRemote) return;
		TriggerInfo trigger = typeToInfoMap.get(target.getClass());
		if (trigger != null) {
			trigger.handleAttach(world, event.getEntityPlayer(), (EntityAnimal) target);
		}
	}
}
