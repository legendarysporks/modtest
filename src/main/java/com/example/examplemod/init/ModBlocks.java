package com.example.examplemod.init;

import com.example.examplemod.Reference;
import com.example.examplemod.blocks.GenericBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;

import static com.example.examplemod.init.ModItems.raw_uru;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class ModBlocks {
	public static Block tutorialBlock;
	public static Block uru_ore;

	public static void init() {
		tutorialBlock = new GenericBlock("tutorial_block", Material.ROCK);
		uru_ore = new GenericBlock("uru_ore", Material.ROCK, raw_uru, 1, 3, CreativeTabs.MISC);
		uru_ore.setHardness(3);
		uru_ore.setHarvestLevel("pickaxe", 2);

	}
}
