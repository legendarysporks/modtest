package com.example.examplemod.utilities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class InventoryUtils {
	public static ItemStack findInInventoryHandsFirst(EntityPlayer player, Class<? extends Item> itemClass) {
		ItemStack stack = findInHands(player, itemClass);
		if (stack.isEmpty()) {
			return findInInventory(player, itemClass);
		} else {
			return stack;
		}
	}

	public static ItemStack findInInventoryHandsLast(EntityPlayer player, Class<? extends Item> itemClass) {
		ItemStack stack = findInInventory(player, itemClass);
		if (stack.isEmpty()) {
			return findInHands(player, itemClass);
		} else {
			return stack;
		}
	}

	public static ItemStack findInHands(EntityPlayer player, Class<? extends Item> itemClass) {
		if (isA(player.getHeldItem(EnumHand.OFF_HAND), itemClass)) {
			return player.getHeldItem(EnumHand.OFF_HAND);
		} else if (isA(player.getHeldItem(EnumHand.MAIN_HAND), itemClass)) {
			return player.getHeldItem(EnumHand.MAIN_HAND);
		} else {
			return ItemStack.EMPTY;
		}
	}

	public static ItemStack findInInventory(EntityPlayer player, Class<? extends Item> itemClass) {
		if (isA(player.getHeldItem(EnumHand.OFF_HAND), itemClass)) {
			return player.getHeldItem(EnumHand.OFF_HAND);
		} else if (isA(player.getHeldItem(EnumHand.MAIN_HAND), itemClass)) {
			return player.getHeldItem(EnumHand.MAIN_HAND);
		} else {
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
				ItemStack itemstack = player.inventory.getStackInSlot(i);

				if (isA(itemstack, itemClass)) {
					return itemstack;
				}
			}

			return ItemStack.EMPTY;
		}
	}

	private static boolean isA(ItemStack stack, Class<? extends Item> itemClass) {
		return itemClass.isInstance(stack.getItem());
	}
}
