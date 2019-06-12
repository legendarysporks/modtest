package com.example.examplemod;

import com.example.examplemod.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, version = Reference.VERSION, acceptedMinecraftVersions=Reference.ACCEPTED_MINECRAFT_VERSIONS)
public class ExampleMod
{
    @Mod.Instance
    public static ExampleMod instance;


    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        System.out.println(Reference.MODID + ": preInit");
        ModItems.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        System.out.println(Reference.MODID + ": init");

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        System.out.println(Reference.MODID + ": postInit");

    }
}
