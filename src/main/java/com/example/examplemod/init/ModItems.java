package com.example.examplemod.init;

import com.example.examplemod.Reference;
import com.example.examplemod.items.GenericItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid= Reference.MODID)
public class ModItems {

	public static Item basicIngot;
	public static Item raw_uru;
    public static Item empRound;

    public static void init() {
        basicIngot = new GenericItem("basic_ingot");
        raw_uru = new GenericItem("raw_uru");
        empRound = new GenericItem("emp_round");
    }

}
