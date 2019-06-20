package com.example.examplemod.init;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModRecipes {
    public static void init(){
        GameRegistry.addSmelting(ModItems.raw_uru, new ItemStack(ModItems.basicIngot,1 ), 1.5f);
    }
}
