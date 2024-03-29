package com.example.examplemod.items;

import com.example.examplemod.Reference;
import com.example.examplemod.utilities.Logging;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class GenericItem extends Item {
	private static HashMap<String, GenericItem> items = new HashMap<>();
	private static HashMap<String, SoundEvent> sounds = new HashMap<>();

	public GenericItem(String name) {
		this(name, CreativeTabs.MISC, 64);
	}

	public GenericItem(String name, CreativeTabs tab) {
		this(name, tab, 64);
	}

	public GenericItem(String name, int maxStackSize) {
		this(name, CreativeTabs.MISC, maxStackSize);
	}

	public GenericItem(String name, CreativeTabs tab, int maxStackSize) {
		Logging.logTrace(name + " consructed");
		setUnlocalizedName(name);
		setRegistryName(name);
		setCreativeTab(tab);
		setMaxStackSize(maxStackSize);
		// save this item so it can be registered later
		items.put(name, this);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		for (GenericItem i : items.values()) {
			//TODO hey jf - could this just call register() or maybe pass the entire collection?
			event.getRegistry().registerAll(i);
			Logging.logTrace(i.getRegistryName() + ".registerItems");
		}
	}

	@SubscribeEvent
	public static void registerRenders(ModelRegistryEvent event) {
		for (GenericItem i : items.values()) {
			ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation(i.getRegistryName(),
					"inventory"));
			Logging.logInfo(i.getRegistryName() + ".registerRenders");
		}
	}
}
