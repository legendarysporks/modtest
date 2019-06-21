package com.example.examplemod.init;

import com.example.examplemod.Reference;
import com.example.examplemod.items.GenericItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid= Reference.MODID)
public class ModItems {

    static Item basicIngot;
    static Item raw_uru;
    static Item thor_hammer;
    public static void init() {

        basicIngot = new GenericItem("basic_ingot");
        raw_uru = new GenericItem("raw_uru");
        thor_hammer = new GenericItem("thor_hammer");
    }

}
