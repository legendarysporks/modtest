package com.example.examplemod.blocks;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Random;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class GenericBlock extends Block {
	private static HashMap<String, GenericBlock> blocks = new HashMap<>();
	private Item dropItem;
	private int minDropAmount;
	private int maxDropAmount;

	public GenericBlock(String name, Material materialIn) {
		this(name, materialIn, null, 1, 1, CreativeTabs.BUILDING_BLOCKS);
	}

	public GenericBlock(String name, Material materialIn, Item dropItem, int minDrop, int maxDrop, CreativeTabs tab) {
		super(materialIn);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.dropItem = dropItem;
		assert (minDropAmount > 0);
		assert (maxDropAmount >= minDropAmount);
		minDropAmount = minDrop;
		maxDropAmount = maxDrop;
		setCreativeTab(tab);
		blocks.put(name, this);
		ExampleMod.logger.info("GenericBlock.init");
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		for (GenericBlock block : blocks.values()) {
			event.getRegistry().registerAll(block);
			ExampleMod.logger.info(block.getRegistryName() + ".registerBlocks");
		}
	}

	@SubscribeEvent
	public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
		for (GenericBlock block : blocks.values()) {
			event.getRegistry().registerAll(new ItemBlock(block).setRegistryName(block.getRegistryName()));
			ExampleMod.logger.info(block.getRegistryName() + ".registerItemBlocks");
		}
	}

	@SubscribeEvent
	public static void registerRenders(ModelRegistryEvent event) {
		for (GenericBlock block : blocks.values()) {
			Item item = Item.getItemFromBlock(block);
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
			ExampleMod.logger.info(block.getRegistryName() + ".registerRenders");
		}
	}

	@Override
	public Item getItemDropped(IBlockState blockState, Random rand, int fortune) {
		return (dropItem == null) ? super.getItemDropped(blockState, rand, fortune) : dropItem;
	}

	@Override
	public int quantityDropped(Random random) {
		return minDropAmount + random.nextInt(maxDropAmount - minDropAmount + 1);
	}

	@Override
	public int quantityDroppedWithBonus(int fortune, Random random) {
		return super.quantityDroppedWithBonus(fortune, random);
	}
}
