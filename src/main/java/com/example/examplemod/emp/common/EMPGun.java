package com.example.examplemod.emp.common;

import com.example.examplemod.ExampleMod;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.SidedProxy;
import com.example.examplemod.items.GenericItem;
import com.example.examplemod.utilities.InventoryUtils;

import java.util.Random;

//@Mod.EventBusSubscriber(modid = Reference.MODID)
public class EMPGun extends GenericItem {
	private static final boolean ALLOWED_IN_CREATIVE = true;
	@SidedProxy(clientSide = "com.example.examplemod.emp.client.EMPGunClient",
			serverSide = "com.example.examplemod.emp.server.EMPGunServer")
	public static EMPGun proxy;

	private static String name = "emp_gun";
//	private static String EMPSoundName = "fire_emp";
//	private SoundEvent empBlastSound;

	private static final String EMPSoundNames[] = {
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
	}

	public void doPreInit() {
//		empBlastSound = createSoundEvent(EMPSoundName);
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
		ItemStack ammo = InventoryUtils.findInInventory(player, ItemArrow.class);
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
//			world.playSound(player, player.getPosition(), empBlastSound, SoundCategory.PLAYERS, 1.0f, 1.0f);
			if (!world.isRemote) {
				EMPProjectile projectile = new EMPProjectile(world, player);
				projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0f, 1.6f, 0f);
				world.spawnEntity(projectile);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
		} else if (inCreativeMode) {
			// in creative mode we don't shoot, we just pass the event along
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
		} else {
			// out of ammo.  FAIL.
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
		}
	}

	private ItemStack findAmmo(EntityPlayer player) {
		if (this.isEMPRound(player.getHeldItem(EnumHand.OFF_HAND))) {
			return player.getHeldItem(EnumHand.OFF_HAND);
		} else if (this.isEMPRound(player.getHeldItem(EnumHand.MAIN_HAND))) {
			return player.getHeldItem(EnumHand.MAIN_HAND);
		} else {
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
				ItemStack itemstack = player.inventory.getStackInSlot(i);

				if (this.isEMPRound(itemstack)) {
					return itemstack;
				}
			}

			return ItemStack.EMPTY;
		}
	}

	private boolean isEMPRound(ItemStack stack) {
		return stack.getItem() instanceof ItemArrow;
	}
}
