package com.example.examplemod.expodingsheep;

import com.example.examplemod.utilities.commands.*;
import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
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
	private static final String CONFIG_VERSION = "0.1";
	private static final TriggerInfo[] triggers = {
			new TriggerInfo("chicken", EntityChicken.class),
			new TriggerInfo("cow", EntityCow.class, "minecraft:beef", "evansmod:emp_round"),
			new TriggerInfo("donkey", EntityDonkey.class),
			new TriggerInfo("horse", EntityHorse.class),
			new TriggerInfo("llama", EntityLlama.class),
			new TriggerInfo("mule", EntityMule.class),
			new TriggerInfo("ocelot", EntityOcelot.class),
			new TriggerInfo("pig", EntityPig.class, "minecraft:cake", "minecraft:portchop"),
			new TriggerInfo("parrot", EntityParrot.class),
			new TriggerInfo("rabbit", EntityRabbit.class),
			new TriggerInfo("sheep", EntitySheep.class, "minecraft:mutton", "evansmod:raw_uru"),
			new TriggerInfo("wolf", EntityWolf.class),
	};
	private static final HashMap<Class<? extends EntityAnimal>, TriggerInfo> typeToInfoMap = new HashMap<>();
	private static final HashMap<String, TriggerInfo> nameToInfoMap = new HashMap<>();
	private static final String cmdNamd = "ExplodingAnimals";
	private static final String cmdUsage = "ExplodingAnimals [ settings | settings <animal> | get <animal> <setting> | "
			+ "set <animal> <setting> <value> ]";
	private static final String[] cmdAliases = {"explodingAnimals", "explodinganimals", "ea", "EA", "boom", "BOOM"};

	static {
		for (TriggerInfo info : triggers) {
			typeToInfoMap.put(info.targerAnimal, info);
			nameToInfoMap.put(info.name, info);
		}
	}

	public final SettingAccessor settings;
	@Setting
	public boolean enabled = true;

	public ExplodingAnimals() {
		subscribeToFMLEvents();
		GenericCommand.create(cmdNamd, cmdUsage, cmdAliases).addTarget(this);
		settings = new SettingAccessor(this, cmdNamd, CONFIG_VERSION);
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

	@SubscribeEvent
	public void onEvent(LivingDropsEvent event) {
		Entity entity = event.getEntity();
		TriggerInfo triggerInfo = typeToInfoMap.get(entity.getClass());
		if ((triggerInfo != null) && triggerInfo.hasDrops()) {
			event.getDrops().clear();
			for (ItemStack stack : triggerInfo.getDrops()) {
				event.getDrops().add(new EntityItem(entity.getEntityWorld(), entity.posX,
						entity.posY, entity.posZ, stack));
			}
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

	@Command(help = "List available settings")
	public void doSettings(ICommandSender sender) {
		GenericCommand.sendMsg(sender, settings.getSettingNames());
		GenericCommand.sendMsg(sender, nameToInfoMap.keySet());
	}

	@Command(help = "List settings for an animal: 'settings <animal>'")
	public void doSettings(ICommandSender sender, String animal) {
		TriggerInfo info = nameToInfoMap.get(animal);
		if (info == null) {
			GenericCommand.sendMsg(sender, "Unknown animal: " + animal);
		} else {
			GenericCommand.sendMsg(sender, info.settings.getSettingNames());
		}
	}

	@Command(help = "Get the value of a setting: 'get <settingName'")
	public void doGet(ICommandSender sender, String setting) {
		try {
			GenericCommand.sendMsg(sender, settings.get(setting));
		} catch (SettingNotFoundException e) {
			GenericCommand.sendMsg(sender, e.getMessage());
		}
	}

	@Command(help = "Get the value of a setting: 'set <settingName> <value>'")
	public void doSet(ICommandSender sender, String setting, String value) {
		try {
			settings.set(setting, value);
			GenericCommand.sendMsg(sender, setting + " set to " + value);
		} catch (InvalidValueException | SettingNotFoundException e) {
			GenericCommand.sendMsg(sender, e.getMessage());
		}
	}

	@Command(help = "Change the value of a setting: 'get <animal> <settingName>'")
	public void doGet(ICommandSender sender, String animal, String setting) {
		TriggerInfo info = nameToInfoMap.get(animal);
		if (info == null) {
			GenericCommand.sendMsg(sender, "Unknown animal: " + animal);
		} else {
			try {
				GenericCommand.sendMsg(sender, info.settings.get(setting));
			} catch (SettingNotFoundException e) {
				GenericCommand.sendMsg(sender, e.getMessage());
			}
		}
	}

	@Command(help = "Change the value of a setting on a particular animal: 'set <animal> <settingName> <value>'")
	public void doSet(ICommandSender sender, String animal, String setting, String value) {
		TriggerInfo info = nameToInfoMap.get(animal);
		if (info == null) {
			GenericCommand.sendMsg(sender, "Unknown animal: " + animal);
		} else {
			try {
				info.settings.set(setting, value);
				GenericCommand.sendMsg(sender, setting + " set to " + value);
			} catch (InvalidValueException | SettingNotFoundException e) {
				GenericCommand.sendMsg(sender, e.getMessage());
			}
		}
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
		public final SettingAccessor settings;
		private final List<ItemStack> drops;
		private final String[] dropNames;

		public TriggerInfo(String animalName, Class<? extends EntityAnimal> animal) {
			this(animalName, animal, new String[0]);
		}

		public TriggerInfo(String animalName, Class<? extends EntityAnimal> animal, String... dropNames) {
			name = animalName;
			enabled = true;
			targerAnimal = animal;
			chanceToExplode = defaulChance;
			burnTime = defaultBurnTime;
			explosionDamage = defaultDamage;
			smoking = defaultSmoking;
			settings = new SettingAccessor(this);
			drops = new ArrayList<>();
			this.dropNames = dropNames;
		}

		public boolean hasDrops() {
			return dropNames.length > 0;
		}

		public List<ItemStack> getDrops() {
			if (drops.size() != dropNames.length) {
				for (String dropName : dropNames) {
					Item item = Item.getByNameOrId(dropName);
					if (item != null) {
						drops.add(new ItemStack(item));
					}
				}
			}
			return drops;
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
}
