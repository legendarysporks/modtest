package com.example.examplemod.expodingsheep;

import com.example.examplemod.utilities.GenericCommand;
import com.example.examplemod.utilities.GenericSettings;
import com.example.examplemod.utilities.GenericSettings.Setting;
import com.example.examplemod.utilities.HackFMLEventListener;
import net.minecraft.command.ICommandSender;
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

import java.util.*;


@Mod.EventBusSubscriber
@SideOnly(Side.CLIENT)
public class ExplodingAnimals implements HackFMLEventListener {
	private static final TriggerInfo[] triggers = {
			new TriggerInfo("cow", EntityCow.class),
			new TriggerInfo("chicken", EntityChicken.class),
			new TriggerInfo("pig", EntityPig.class),
			new TriggerInfo("sheep", EntitySheep.class),
			new TriggerInfo("llama", EntityLlama.class),
	};
	private static final HashMap<Class<? extends EntityAnimal>, TriggerInfo> typeToInfoMap = new HashMap<>();
	private static final HashMap<String, TriggerInfo> nameToInfoMap = new HashMap<>();
	{
		for (TriggerInfo info : triggers) {
			typeToInfoMap.put(info.targerAnimal, info);
			nameToInfoMap.put(info.name, info);
		}
	}

	private static final String cmdNamd = "ExplodingAnimals";
	private static final String cmdUsage = "ExplodingAnimals [ list | list <animal> | get <animal> <setting> | "
			+ "set <animal> <setting> <value> ]";
	private static final String[] cmdAliases = {"explodingAnimals", "explodinganimals", "ea", "EA", "boom", "BOOM"};
	public final GenericSettings settings;

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

	private final ExplodingAnimalsCommand command;
	@Setting
	public boolean enabled = true;

	public ExplodingAnimals() {
		subscribeToFMLEvents();
		command = new ExplodingAnimalsCommand(this);
		settings = new GenericSettings(this);
	}

	public List<String> getAnimals() {
		List<String> result = new ArrayList<>(nameToInfoMap.keySet().size());
		result.addAll(nameToInfoMap.keySet());
		Collections.sort(result);
		return result;
	}

	public TriggerInfo getTriggerInfo(String animal) {
		return nameToInfoMap.get(animal);
	}

	private static class TriggerInfo {
		private static final int defaulChance = 25;     //chances in 100 entity will explode when attacked
		private static final int defaultBurnTime = 5;   //seconds animal burns and is "primed" to explode
		private static final float defaultDamage = 3;   //4 = tnt
		private static final boolean defaultSmoking = true; //smoke after explosion
		public final String name;
		public final Class<? extends EntityAnimal> targerAnimal;
		@Setting
		public int chanceToExplode;
		@Setting
		public int burnTime;
		@Setting
		public float explosionDamage;
		@Setting
		public boolean smoking;
		private final Random rand = new Random();
		public boolean enabled;
		public GenericSettings settings;

		public TriggerInfo(String animalName, Class<? extends EntityAnimal> animal) {
			name = animalName;
			enabled = true;
			targerAnimal = animal;
			chanceToExplode = defaulChance;
			burnTime = defaultBurnTime;
			explosionDamage = defaultDamage;
			smoking = defaultSmoking;
			settings = new GenericSettings(this);
		}

		public void handleAttach(World world, EntityPlayer player, EntityAnimal animal) {
			if (enabled) {
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
		}

		private void explode(World world, EntityAnimal animal) {
			animal.setDead();
			world.createExplosion(null, animal.posX, animal.posY, animal.posZ, explosionDamage, smoking);
		}
	}

	public class ExplodingAnimalsCommand extends GenericCommand {
		private ExplodingAnimals animals;

		public ExplodingAnimalsCommand(ExplodingAnimals animals) {
			super(cmdNamd, cmdUsage, cmdAliases);
			this.animals = animals;
		}

		public void doList(ICommandSender sender) {
			sendMsg(sender, settings.list());
			sendMsg(sender, nameToInfoMap.keySet());
		}

		public void doList(ICommandSender sender, String animal) {
			TriggerInfo info = nameToInfoMap.get(animal);
			if (info == null) {
				sendMsg(sender, "Unknown animal: " + animal);
			} else {
				sendMsg(sender, info.settings.list());
			}
		}

		public void doGet(ICommandSender sender, String setting) {
			try {
				sendMsg(sender, settings.get(setting));
			} catch (GenericSettings.SettingNotFoundException e) {
				sendMsg(sender, setting + " not found");
			}
		}

		public void doSet(ICommandSender sender, String setting, String value) {
			try {
				settings.set(setting, value);
				sendMsg(sender, setting + " set to " + value);
			} catch (GenericSettings.SettingNotFoundException e) {
				sendMsg(sender, setting + " not found");
			}
		}

		public void doGet(ICommandSender sender, String animal, String setting) {
			TriggerInfo info = nameToInfoMap.get(animal);
			if (info == null) {
				sendMsg(sender, "Unknown animal: " + animal);
			} else {
				try {
					sendMsg(sender, info.settings.get(setting));
				} catch (GenericSettings.SettingNotFoundException e) {
					sendMsg(sender, setting + " not found");
				}
			}
		}

		public void doSet(ICommandSender sender, String animal, String setting, String value) {
			TriggerInfo info = nameToInfoMap.get(animal);
			if (info == null) {
				sendMsg(sender, "Unknown animal: " + animal);
			} else {
				try {
					info.settings.set(setting, value);
					sendMsg(sender, setting + " set to " + value);
				} catch (GenericSettings.SettingNotFoundException e) {
					sendMsg(sender, setting + " not found");
				}
			}
		}
	}
}
