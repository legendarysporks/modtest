package com.example.examplemod.expodingsheep;

import com.example.examplemod.utilities.commands.CommandMethod;
import com.example.examplemod.utilities.commands.GenericCommand;
import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import com.example.examplemod.utilities.settings.GenericSettings;
import com.example.examplemod.utilities.settings.Setting;
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
			new TriggerInfo("chicken", EntityChicken.class),
			new TriggerInfo("cow", EntityCow.class),
			new TriggerInfo("donkey", EntityDonkey.class),
			new TriggerInfo("horse", EntityHorse.class),
			new TriggerInfo("llama", EntityLlama.class),
			new TriggerInfo("mule", EntityMule.class),
			new TriggerInfo("ocelot", EntityOcelot.class),
			new TriggerInfo("pig", EntityPig.class),
			new TriggerInfo("parrot", EntityParrot.class),
			new TriggerInfo("rabbit", EntityRabbit.class),
			new TriggerInfo("sheep", EntitySheep.class),
			new TriggerInfo("wolf", EntityWolf.class),
	};
	private static final HashMap<Class<? extends EntityAnimal>, TriggerInfo> typeToInfoMap = new HashMap<>();
	private static final HashMap<String, TriggerInfo> nameToInfoMap = new HashMap<>();
	private static final String cmdNamd = "ExplodingAnimals";
	private static final String cmdUsage = "ExplodingAnimals [ settings | settings <animal> | get <animal> <setting> | "
			+ "set <animal> <setting> <value> ]";
	private static final String[] cmdAliases = {"explodingAnimals", "explodinganimals", "ea", "EA", "boom", "BOOM"};
	public final GenericSettings settings;
	private final ExplodingAnimalsCommand command;
	@Setting
	public boolean enabled = true;

	static {
		for (TriggerInfo info : triggers) {
			typeToInfoMap.put(info.targerAnimal, info);
			nameToInfoMap.put(info.name, info);
		}
	}

	public ExplodingAnimals() {
		subscribeToFMLEvents();
		command = new ExplodingAnimalsCommand(this);
		settings = new GenericSettings(this, cmdNamd, "1.0");
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
		private final Random rand = new Random();
		@Setting
		public int chanceToExplode;
		@Setting
		public int burnTime;
		@Setting
		public float explosionDamage;
		@Setting
		public boolean smoking;
		@Setting
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

		@CommandMethod(help = "List available settings")
		public void doSettings(ICommandSender sender) {
			sendMsg(sender, settings.getSettingNames());
			sendMsg(sender, nameToInfoMap.keySet());
		}

		@CommandMethod(help = "List settings for an animal: 'settings <animal>'")
		public void doSettings(ICommandSender sender, String animal) {
			TriggerInfo info = nameToInfoMap.get(animal);
			if (info == null) {
				sendMsg(sender, "Unknown animal: " + animal);
			} else {
				sendMsg(sender, info.settings.getSettingNames());
			}
		}

		@CommandMethod(help = "Get the value of a setting: 'get <settingName'")
		public void doGet(ICommandSender sender, String setting) {
			try {
				sendMsg(sender, settings.get(setting));
			} catch (GenericSettings.SettingNotFoundException e) {
				sendMsg(sender, e.getMessage());
			}
		}

		@CommandMethod(help = "Get the value of a setting: 'set <settingName> <value>'")
		public void doSet(ICommandSender sender, String setting, String value) {
			try {
				settings.set(setting, value);
				sendMsg(sender, setting + " set to " + value);
			} catch (GenericSettings.InvalidValueException | GenericSettings.SettingNotFoundException e) {
				sendMsg(sender, e.getMessage());
			}
		}

		@CommandMethod(help = "Change the value of a setting: 'get <animal> <settingName>'")
		public void doGet(ICommandSender sender, String animal, String setting) {
			TriggerInfo info = nameToInfoMap.get(animal);
			if (info == null) {
				sendMsg(sender, "Unknown animal: " + animal);
			} else {
				try {
					sendMsg(sender, info.settings.get(setting));
				} catch (GenericSettings.SettingNotFoundException e) {
					sendMsg(sender, e.getMessage());
				}
			}
		}

		@CommandMethod(help = "Change the value of a setting on a particular animal: 'set <animal> <settingName> <value>'")
		public void doSet(ICommandSender sender, String animal, String setting, String value) {
			TriggerInfo info = nameToInfoMap.get(animal);
			if (info == null) {
				sendMsg(sender, "Unknown animal: " + animal);
			} else {
				try {
					info.settings.set(setting, value);
					sendMsg(sender, setting + " set to " + value);
				} catch (GenericSettings.InvalidValueException | GenericSettings.SettingNotFoundException e) {
					sendMsg(sender, e.getMessage());
				}
			}
		}
	}
}
