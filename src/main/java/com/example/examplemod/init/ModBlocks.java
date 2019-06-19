package com.example.examplemod.init;

import com.example.examplemod.Reference;
import com.example.examplemod.blocks.GenericBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid= Reference.MODID)
public class ModBlocks {
    static Block tutorialBlock;
    static Block uru_ore;

    public static void init(){

        tutorialBlock = new GenericBlock("tutorial_block", Material.ROCK);
        uru_ore = new GenericBlock("uru_ore", Material.ROCK);
    }
}
