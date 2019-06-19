package com.example.examplemod.init;

import com.example.examplemod.Reference;
import com.example.examplemod.items.GenericItem;
import com.example.examplemod.items.ItemBasic;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid= Reference.MODID)
public class ModItems {

    public static Item basicIngot;

    public static void init() {
        basicIngot = new GenericItem("basic_ingot");
    }
}
