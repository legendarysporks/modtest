package com.example.examplemod.emp.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import com.example.examplemod.Reference;
import com.example.examplemod.emp.common.EMPProjectile;

import javax.annotation.Nullable;

public class EMPRenderer extends Render<EMPProjectile> {
	private static final ResourceLocation texture =
			new ResourceLocation(Reference.MODID, "textures/entity/emp_projectile3.png");
	private ModelBase model;

	public EMPRenderer(RenderManager manager) {
		super(manager);
		model = new Model();
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EMPProjectile entity) {
		return texture;
	}

	@Override
	public void doRender(EMPProjectile entity, double x, double y, double z, float yaw, float partialTick) {
		GL11.glPushMatrix();
		bindTexture(texture);
		GL11.glTranslated(x, y - 1.25D, z);
		model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		GL11.glPopMatrix();
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

		@Override
		public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
			super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		}
	}
}