package com.example.examplemod.thorhammer;

import com.example.examplemod.Reference;
import com.example.examplemod.utilities.BoxUtils;
import com.example.examplemod.utilities.RendererHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ThorHammerProjectileRendererClientSide extends RendererHelper.ClientSide<ThorHammerProjectile> {
	private static final ResourceLocation texture =
			new ResourceLocation(Reference.MODID, "textures/entity/thor_hammer_projectile.png");

	public ThorHammerProjectileRendererClientSide() {
		super(ThorHammerProjectile.class);
	}

	@Override
	public Renderer createRenderFor(RenderManager manager) {
		return new Renderer(manager);
	}

	private static class Renderer extends Render<ThorHammerProjectile> {
		private Model3 model;

		public Renderer(RenderManager manager) {
			super(manager);
			model = new Model3();
		}

		@Override
		protected ResourceLocation getEntityTexture(ThorHammerProjectile entity) {
			return texture;
		}

		@Override
		public void doRender(ThorHammerProjectile entity, double x, double y, double z, float yaw, float partialTick) {
			GL11.glPushMatrix();
			bindTexture(texture);
			GL11.glTranslated(x, y, z);
//			model.render(entity, (float)x, (float)y, (float)z, 0.0F, 0.0F, 1.0f/16.0f);
			model.renderHammer(1.0f / 16.0f);
			GL11.glPopMatrix();
		}
	}

	private static class Model3 extends ModelBase {
		public ModelRenderer shape1;
		public ModelRenderer shape2;
		public ModelRenderer shape3;
		public ModelRenderer shape4;

		public Model3() {
			this.textureWidth = 66;
			this.textureHeight = 66;
			this.shape2 = new ModelRenderer(this, 0, 24);
			this.shape2.setRotationPoint(0.0F, 3.0F, 0.0F);
			this.shape2.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, 0.0F);
			this.setRotateAngle(shape2, 0.0F, 0.39269908169872414F, 0.0F);
			this.shape3 = new ModelRenderer(this, 0, 44);
			this.shape3.setRotationPoint(0.0F, 3.0F, 0.0F);
			this.shape3.addBox(-4.0F, -5.0F, -6.0F, 8, 10, 12, 0.0F);
			this.setRotateAngle(shape3, 0.0F, 0.39269908169872414F, 0.0F);
			this.shape1 = new ModelRenderer(this, 0, 0);
			this.shape1.setRotationPoint(0.0F, 16.0F, 0.0F);
			this.shape1.addBox(-1.0F, -8.0F, -1.0F, 2, 16, 2, 0.0F);
			this.setRotateAngle(shape1, 0.0F, 0.39269908169872414F, 0.0F);
			this.shape4 = new ModelRenderer(this, 8, 0);
			this.shape4.setRotationPoint(0.0F, 3.0F, 0.0F);
			this.shape4.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 16, 0.0F);
			this.setRotateAngle(shape4, 0.0F, 0.39269908169872414F, 0.0F);
		}

		public void addBox(ModelRenderer rend, float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored)
		{
			rend.cubeList.add(new ModelBox(rend, rend.textureOffsetX, rend.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F, mirrored));
		}


		public void  renderHammer(float f5) {
			this.shape2.render(f5);
			this.shape3.render(f5);
			this.shape1.render(f5);
			this.shape4.render(f5);
		}

		@Override
		public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
			this.shape2.render(f5);
			this.shape3.render(f5);
			this.shape1.render(f5);
			this.shape4.render(f5);
		}

		/**
		 * This is a helper function from Tabula to set the rotation of model parts
		 */
		public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
			modelRenderer.rotateAngleX = x;
			modelRenderer.rotateAngleY = y;
			modelRenderer.rotateAngleZ = z;
		}
	}
}


