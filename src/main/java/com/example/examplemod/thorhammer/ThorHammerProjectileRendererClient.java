package com.example.examplemod.thorhammer;

import com.example.examplemod.Reference;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ThorHammerProjectileRendererClient extends ThorHammerProjectileRenderer implements IRenderFactory<ThorHammerProjectile> {

	@Override
	public void handleFMLEvent(FMLPreInitializationEvent event) {
		super.handleFMLEvent(event);
		RenderingRegistry.registerEntityRenderingHandler(ThorHammerProjectile.class, this);
	}

	@Override
	public Render<ThorHammerProjectile> createRenderFor(RenderManager manager) {
		return new Renderer(manager);
	}

	public static class Renderer extends Render<ThorHammerProjectile> {
		private static final ResourceLocation texture =
				new ResourceLocation(Reference.MODID, "textures/entity/emp_projectile3.png");
		private ModelBase model;

		public Renderer(RenderManager manager) {
			super(manager);
			model = new Model();
		}

		@Override
		protected ResourceLocation getEntityTexture(ThorHammerProjectile entity) {
			return texture;
		}

		@Override
		public void doRender(ThorHammerProjectile entity, double x, double y, double z, float yaw, float partialTick) {
			GL11.glPushMatrix();
			bindTexture(texture);
			GL11.glTranslated(x, y - 1.25D, z);
			model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			GL11.glPopMatrix();
		}
	}

	private static class Model extends ModelBase {
		ModelRenderer Shape1;
		ModelRenderer Shape2;
		ModelRenderer Shape3;
		ModelRenderer Shape4;

		public Model() {
			textureWidth = 64;
			textureHeight = 32;

			Shape1 = new ModelRenderer(this, 0, 0);
			Shape1.addBox(0F, 0F, 0F, 4, 4, 4);
			Shape1.setRotationPoint(-2F, 19F, -2F);
			Shape1.setTextureSize(64, 32);
			Shape1.mirror = true;
			setRotation(Shape1, 0F, 0F, 0F);
			Shape2 = new ModelRenderer(this, 0, 0);
			Shape2.addBox(0F, 0F, 0F, 2, 6, 2);
			Shape2.setRotationPoint(-1F, 18F, -1F);
			Shape2.setTextureSize(64, 32);
			Shape2.mirror = true;
			setRotation(Shape2, 0F, 0F, 0F);
			Shape3 = new ModelRenderer(this, 0, 0);
			Shape3.addBox(0F, 0F, 0F, 6, 2, 2);
			Shape3.setRotationPoint(-3F, 20F, -1F);
			Shape3.setTextureSize(64, 32);
			Shape3.mirror = true;
			setRotation(Shape3, 0F, 0F, 0F);
			Shape4 = new ModelRenderer(this, 0, 0);
			Shape4.addBox(0F, 0F, 0F, 2, 2, 6);
			Shape4.setRotationPoint(-1F, 20F, -3F);
			Shape4.setTextureSize(64, 32);
			Shape4.mirror = true;
			setRotation(Shape4, 0F, 0F, 0F);
		}

		@Override
		public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
			super.render(entity, f, f1, f2, f3, f4, f5);
			setRotationAngles(f, f1, f2, f3, f4, f5, entity);
			Shape1.render(f5);
			Shape2.render(f5);
			Shape3.render(f5);
			Shape4.render(f5);
		}

		private void setRotation(ModelRenderer model, float x, float y, float z) {
			model.rotateAngleX = x;
			model.rotateAngleY = y;
			model.rotateAngleZ = z;
		}
	}
}
