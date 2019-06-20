package com.example.examplemod.items;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.Reference;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class GenericItem extends Item {
	// map holds all GenericItems for registration.
	//TODO: if you don't need this after registraction, nuke it there when done
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
		ExampleMod.logInfo(">>" + name+".init");
		setUnlocalizedName(name);
		setRegistryName(name);
		setCreativeTab(tab);
		setMaxStackSize(maxStackSize);
		// save this item so it can be registered later
		items.put(name, this);
		ExampleMod.logInfo("<<" + name + ".init");
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		for (GenericItem i : items.values()) {
			event.getRegistry().registerAll(i);
			ExampleMod.logInfo(i.getRegistryName() + ".registerItems");
		}
	}

	@SubscribeEvent
	public static void registerRenders(ModelRegistryEvent event) {
		for (GenericItem i : items.values()) {
			ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation(i.getRegistryName(),
					"inventory"));
			ExampleMod.logInfo(i.getRegistryName() + ".registerRenders");
		}
	}

	protected static SoundEvent createSoundEvent(String soundName) {
		final ResourceLocation soundID = new ResourceLocation(Reference.MODID, soundName);
		SoundEvent result = new SoundEvent(soundID).setRegistryName(soundID);
		sounds.put(soundName, result);
		ExampleMod.logInfo("createSoundEvent( " + soundName + " )");
		return result;
	}

	@SubscribeEvent
	public static void registerSoundEvents(RegistryEvent.Register<SoundEvent> event) {
		for (SoundEvent e : sounds.values()) {
			event.getRegistry().register(e);
		}
		sounds.clear();
	}
}
