package com.example.examplemod.emp.common;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.items.GenericItem;
import com.example.examplemod.utilities.InventoryUtils;
import com.example.examplemod.utilities.commands.GenericCommand;
import com.example.examplemod.utilities.commands.Setting;
import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class EMPGun extends GenericItem implements HackFMLEventListener {
	public static final String COMMAND_NAME = "EMP";
	private static final Class<? extends Item> AMMO_CLASS = EMPAmmo.class;
	public static final String COMMAND_USAGE = "EMP whatever";
	public static final String[] COMMAND_ALIASES = {"emp"};
	public static final String COMMAND_CONFIG_FILE_VERSION = "0.1";
	private static final String NAME = "emp_gun";
	private static final String[] EMP_SOUND_NAMES = new String[]{
			"alien_blaster_fired",
//			"emp_fired",
			"pistol_alien_blaster_fired",
//			"pulsegun_fired",
			"varmnitrifle_fired",
	};

	@SidedProxy(clientSide = "com.example.examplemod.emp.client.EMPGunClient",
			serverSide = "com.example.examplemod.emp.server.EMPGunServer")
	public static EMPGun proxy;
	private static SoundEvent[] EMPSounds = new SoundEvent[EMP_SOUND_NAMES.length];
	@Setting
	public boolean ALLOWED_IN_CREATIVE = true;
	@Setting
	public float VELOCITY = 1.6f;
	@Setting
	public float INACCURACY = 0.0f;
	private GenericCommand command;

	public EMPGun() {
		super(NAME, CreativeTabs.COMBAT, 1);
		setMaxDamage(0);
		GenericCommand.create(COMMAND_NAME, COMMAND_USAGE, COMMAND_ALIASES).addTargetWithPersitentSettings(
				this, NAME, COMMAND_CONFIG_FILE_VERSION);
		subscribeToFMLEvents();
	}

	@Override
	public void handleFMLEvent(FMLPreInitializationEvent event) {
		for (int i = 0; i < EMP_SOUND_NAMES.length; i++) {
			EMPSounds[i] = createSoundEvent(EMP_SOUND_NAMES[i]);
		}
		EMPProjectile.registerModEntity();
	}

	/**
	 * Called when the equipped item is right clicked.
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
		ItemStack itemstack = player.getHeldItem(handIn);
		ItemStack ammo = InventoryUtils.findInInventory(player, AMMO_CLASS);
		boolean hasAmmo = !ammo.isEmpty();
		boolean inCreativeMode = player.capabilities.isCreativeMode;
		boolean creativeOK = !inCreativeMode || ALLOWED_IN_CREATIVE;

		if (creativeOK && hasAmmo) {
			player.setActiveHand(handIn);
			ammo.grow(-1);
			player.swingArm(handIn);
			int soundNumber = player.world.rand.nextInt(EMPSounds.length);
			SoundEvent sound = EMPSounds[soundNumber];
			world.playSound(player, player.getPosition(), sound, SoundCategory.PLAYERS, 1.0f, 1.0f);
			ExampleMod.logInfo("Playing sound: " + EMP_SOUND_NAMES[soundNumber]);
			if (!world.isRemote) {
				EMPProjectile projectile = new EMPProjectile(world, player);
				projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0f, VELOCITY, INACCURACY);
				world.spawnEntity(projectile);
			}
			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		} else if (inCreativeMode) {
			// in creative mode we don't shoot, we just pass the event along
			return new ActionResult<>(EnumActionResult.PASS, itemstack);
		} else {
			// out of ammo.  FAIL.
			return new ActionResult<>(EnumActionResult.FAIL, itemstack);
		}
	}
}
