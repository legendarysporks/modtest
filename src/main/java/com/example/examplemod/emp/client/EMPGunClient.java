package com.example.examplemod.emp.client;

import com.example.examplemod.emp.common.EMPGun;
import com.example.examplemod.emp.common.EMPProjectile;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EMPGunClient extends EMPGun implements IRenderFactory<EMPProjectile> {
	@Override
	public void handleFMLEvent(FMLPreInitializationEvent event) {
		super.handleFMLEvent(event);
		RenderingRegistry.registerEntityRenderingHandler(EMPProjectile.class, this);
	}

	@Override
	public Render<EMPProjectile> createRenderFor(RenderManager manager) {
		return new EMPRenderer(manager);
	}
}
