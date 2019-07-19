package com.example.examplemod.utilities;

import com.example.examplemod.utilities.hackfmlevents.HackFMLEventListener;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class RendererHelper {
//	@SidedProxy(clientSide = "com.example.examplemod.utilities.RendererHelper.ClientSide<E,R>",
//			serverSide = "com.example.examplemod.utilities.RendererHelper")
//	public static RendererHelper proxy;

	public static abstract class ClientSide<E extends Entity> extends RendererHelper implements IRenderFactory<E>, HackFMLEventListener {
		private Class<E> entityClass;

		public ClientSide(Class<E> entityClass) {
			this.entityClass = entityClass;
			subscribeToFMLEvents();
		}

		@Override
		public void handleFMLEvent(FMLPreInitializationEvent event) {
			RenderingRegistry.registerEntityRenderingHandler(entityClass, this);
		}
	}
}
