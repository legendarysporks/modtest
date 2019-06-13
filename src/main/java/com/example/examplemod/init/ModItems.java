package com.example.examplemod.init;

import com.example.examplemod.Reference;
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

    static Item tutorialItem;

    public static void init() {
        tutorialItem = new ItemBasic("tutorial_item");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(tutorialItem);
    }

    @SubscribeEvent
    public static void registerRenderss(ModelRegistryEvent event){
        registerRender(tutorialItem);
    }

    private static void registerRender(Item item){ //jkljjjk
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation( item.getRegistryName(), "inventory"));
    }

}
