package com.example.examplemod.init;

import com.example.examplemod.Reference;
import com.example.examplemod.emp.common.EMPAmmo;
import com.example.examplemod.emp.common.EMPGun;
import com.example.examplemod.items.GenericItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class ModItems {

	public static Item thor_hammer;
	public static Item basicIngot;
	public static Item raw_uru;
	public static Item empRound;
	public static Item empGun;

	public static void init() {
		basicIngot = new GenericItem("basic_ingot");
		raw_uru = new GenericItem("raw_uru");
		thor_hammer = new GenericItem("thor_hammer");
		empRound = new EMPAmmo("emp_round");
		empGun = EMPGun.proxy;
	}
}
