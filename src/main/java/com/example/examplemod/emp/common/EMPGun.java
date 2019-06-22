package com.example.examplemod.emp.common;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.items.GenericItem;
import com.example.examplemod.utilities.HackFMLEventListener;
import com.example.examplemod.utilities.InventoryUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class EMPGun extends GenericItem implements HackFMLEventListener {
	private static final String name = "emp_gun";
	private static final Class<? extends Item> AMMO_CLASS = ItemArrow.class;
	private static boolean ALLOWED_IN_CREATIVE = true;
	private static float VELOCITY = 1.6f;
	private static float INACCURACY = 0.0f;

	@SidedProxy(clientSide = "com.example.examplemod.emp.client.EMPGunClient",
			serverSide = "com.example.examplemod.emp.server.EMPGunServer")
	public static EMPGun proxy;

	private static final String EMPSoundNames[] = new String[]{
			"alien_blaster_fired",
//			"emp_fired",
			"pistol_alien_blaster_fired",
//			"pulsegun_fired",
			"varmnitrifle_fired",
	};
	private static SoundEvent EMPSounds[] = new SoundEvent[EMPSoundNames.length];

	public EMPGun() {
		super(name, CreativeTabs.COMBAT, 1);
		setMaxDamage(0);
		subscribeToFMLEvents();
	}

	@Override
	public void handleFMLEvent(FMLPreInitializationEvent event) {
		for (int i = 0; i < EMPSoundNames.length; i++) {
			EMPSounds[i] = createSoundEvent(EMPSoundNames[i]);
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
			ExampleMod.logInfo("Playing sound: " + EMPSoundNames[soundNumber]);
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
