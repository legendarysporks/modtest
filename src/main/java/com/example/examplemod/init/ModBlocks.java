package com.example.examplemod.init;

import com.example.examplemod.Reference;
import com.example.examplemod.blocks.BlockBasic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.rmi.registry.Registry;
@Mod.EventBusSubscriber(modid= Reference.MODID)
public class ModBlocks {
    static Block tutorialBlock;
    static Block uru_ore;

    public static void init(){

        tutorialBlock = new BlockBasic("tutorial_block", Material.ROCK);
        uru_ore = new BlockBasic("uru_ore", Material.ROCK);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event){
        event.getRegistry().registerAll(tutorialBlock);
        event.getRegistry().registerAll(uru_ore);
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event){
        event.getRegistry().registerAll(new ItemBlock(tutorialBlock).setRegistryName(tutorialBlock.getRegistryName()));
        event.getRegistry().registerAll(new ItemBlock(uru_ore).setRegistryName(uru_ore.getRegistryName()));
    }

    @SubscribeEvent
    public static void registerRenders(ModelRegistryEvent event){
        registerRender(Item.getItemFromBlock(tutorialBlock));
        registerRender(Item.getItemFromBlock(uru_ore));
    }

    public static void registerRender(Item item){
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation( item.getRegistryName(), "inventory"));
    }
}
