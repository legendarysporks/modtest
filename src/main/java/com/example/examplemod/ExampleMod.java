package com.example.examplemod;

import com.example.examplemod.batfight.common.BatFight;
import com.example.examplemod.blocks.GenericBlock;
import com.example.examplemod.emp.common.EMPGun;
import com.example.examplemod.init.ModBlocks;
import com.example.examplemod.init.ModItems;
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

    private static Logger logger;

    public static void logInfo(String info) {
        if (logger != null) {
            logger.info(info);
        } else {
            System.out.println(Reference.MODNAME + ":" + info);
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        System.out.println(Reference.MODID + ": preInit");
        ModItems.init();
        ModBlocks.init();
        BatFight.proxy.doPreInit();
        EMPGun.proxy.doPreInit();
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
