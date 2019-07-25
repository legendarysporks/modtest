package com.example.examplemod.thorhammer;

import com.example.examplemod.Reference;
import com.example.examplemod.utilities.ModelRendererBuilder;
import com.example.examplemod.utilities.RendererHelper;
import com.sun.javafx.geom.Vec3f;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
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
			GL11.glRotatef(180, 0, 0, 1);
//			GL11.glRotatef((float)(yaw*180/Math.PI), 0, 1, 0);

/*			Vec3f v = getVectorForRotation(entity.getInitialPitch(), entity.getInitialYaw());
//			Vec3f v = getVectorForRotation(0, entity.getInitialYaw());

			if (v.z != 0.0F) {
				GlStateManager.rotate(v.z + 180, 0.0F, 0.0F, 1.0F);
			}

			if (v.y != 0.0F) {
				GlStateManager.rotate(v.y, 0.0F, 1.0F, 0.0F);
			}

			if (v.x != 0.0F) {
				GlStateManager.rotate(v.x, 1.0F, 0.0F, 0.0F);
			} */

//			model.render(entity, (float)x, (float)y, (float)z, 0.0F, 0.0F, 1.0f/16.0f);
			model.renderHammer(1.0f / 16.0f);
			GL11.glPopMatrix();
		}
	}

	private static class Model3 extends ModelBase {
		public ModelRenderer shape0;
		public ModelRenderer shape1;
		public ModelRenderer shape2;
		public ModelRenderer shape3;
		public ModelRenderer shape4;

		public Model3() {
			textureWidth = 66;
			textureHeight = 66;

			if (false) {
				this.shape2 = new ModelRenderer(this, 0, 24);
				this.shape2.setRotationPoint(0.0F, 3.0F, 0.0F);
				this.shape2.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, true);
				this.setRotateAngle(shape2, 0.0F, 0.0f, 0.0F);
				this.shape3 = new ModelRenderer(this, 0, 44);
				this.shape3.setRotationPoint(0.0F, 3.0F, 0.0F);
				this.shape3.addBox(-4.0F, -5.0F, -6.0F, 8, 10, 12, true);
				this.setRotateAngle(shape3, 0.0F, 0.0f, 0.0F);
				this.shape1 = new ModelRenderer(this, 0, 0);
				this.shape1.setRotationPoint(0.0F, 16.0F, 0.0F);
				this.shape1.addBox(-1.0F, -8.0F, -1.0F, 2, 16, 2, true);
				this.setRotateAngle(shape1, 0.0F, 0.0f, 0.0F);
				this.shape4 = new ModelRenderer(this, 8, 0);
				this.shape4.setRotationPoint(0.0F, 3.0F, 0.0F);
				this.shape4.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 16, true);
				this.setRotateAngle(shape4, 0.0F, 0.0f, 0.0F);
			} else if (true) {
				shape2 = new ModelRenderer(this, 0, 24);
				shape2.setRotationPoint(0.0F, 3.0F, 0.0F);
				shape2.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, true);
				setRotateAngle(shape2, 0.0F, 0.0f, 0.0F);
				shape3 = new ModelRenderer(this, 0, 44);
				shape3.setRotationPoint(0.0F, 3.0F, 0.0F);
				shape3.addBox(-4.0F, -5.0F, -6.0F, 8, 10, 12, true);
				setRotateAngle(shape3, 0.0F, 0.0f, 0.0F);
				shape1 = new ModelRenderer(this, 0, 0);
				shape1.setRotationPoint(0.0F, 16.0F, 0.0F);
				shape1.addBox(-1.0F, -8.0F, -1.0F, 2, 16, 2, true);
				setRotateAngle(shape1, 0.0F, 0.0f, 0.0F);
				shape4 = new ModelRenderer(this, 8, 0);
				shape4.setRotationPoint(0.0F, 3.0F, 0.0F);
				shape4.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 16, true);
				setRotateAngle(shape4, 0.0F, 0.0f, 0.0F);
				ModelRendererBuilder.addChildRenderer(shape3, shape1);
				shape1 = null;
				ModelRendererBuilder.addChildRenderer(shape3, shape2);
				shape2 = null;
				ModelRendererBuilder.addChildRenderer(shape3, shape4);
				shape4 = null;
			} else if (true) {
				shape2 = ModelRendererBuilder.model(this)
						.textureOffset(0, 24)
						.rotationPoint(0.0F, 3.0F, 0.0F)
						.box()
						.at(-5.0F, -4.0F, -6.0F)
						.sizedTo(10, 8, 12)
						.rotation(0.0F, 0.39269908169872414F, 0.0F)
						.create();

				shape3 = ModelRendererBuilder.model(this)
						.textureOffset(0, 44)
						.rotationPoint(0.0F, 3.0F, 0.0F)
						.box()
						.at(-4.0F, -5.0F, -6.0F)
						.sizedTo(8, 10, 12)
						.rotation(0.0F, 0.39269908169872414F, 0.0F)
						.create();

				shape1 = ModelRendererBuilder.model(this)
						.textureOffset(0, 0)
						.rotationPoint(0.0F, 16.0F, 0.0F)
						.box()
						.at(-1.0F, -8.0F, -1.0F)
						.sizedTo(2, 16, 2)
						.rotation(0.0F, 0.39269908169872414F, 0.0F)
						.create();

				shape4 = ModelRendererBuilder.model(this)
						.textureOffset(8, 0)
						.rotationPoint(0.0F, 3.0F, 0.0F)
						.box()
						.at(-4.0F, -4.0F, -8.0F)
						.sizedTo(8, 8, 16)
						.rotation(0.0F, 0.39269908169872414F, 0.0F)
						.create();
			}
		}

		public void renderHammer(float f5) {
			if (shape2 != null) shape2.render(f5);
			if (shape3 != null) shape3.render(f5);
			if (shape1 != null) shape1.render(f5);
			if (shape4 != null) shape4.render(f5);
		}

		@Override
		public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
			renderHammer(f5);
		}

		@Override
		public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
			// The model doesn't rotate correctly.  The parts don't rotate around the same point.
			/*
			Vec3f v = getVectorForRotation(headPitch, netHeadYaw);
			Logging.logInfo(String.format("pitch=%f yaw=%f x=%f y=%f z=%f", headPitch, netHeadYaw, v.x, v.y, v.z));
			if (shape1 != null) setRotateAngle(shape1, v.x, v.y, v.z);
			if (shape2 != null) setRotateAngle(shape2, v.x, v.y, v.z);
			if (shape3 != null) setRotateAngle(shape3, v.x, v.y, v.z);
			if (shape4 != null) setRotateAngle(shape4, v.x, v.y, v.z);
			 */
		}

		/**
		 * This is a helper function from Tabula to set the rotation of model parts
		 */
		private ModelRenderer setRotateAngle(ModelRenderer modelRenderer, float xRad, float yRad, float zRad) {
			modelRenderer.rotateAngleX = xRad;
			modelRenderer.rotateAngleY = yRad + 0.39269908169872414F; // constant needed to account for this particular model
			modelRenderer.rotateAngleZ = zRad;
			return modelRenderer;
		}

	}

	private static Vec3f getVectorForRotation(float pitchDegrees, float yawDegrees) {
		final float _180divPi = (float) (180.0f / Math.PI);
		float x = -MathHelper.sin(yawDegrees * 0.017453292F) * MathHelper.cos(pitchDegrees * 0.017453292F);
		float y = -MathHelper.sin(pitchDegrees * 0.017453292F);
		float z = MathHelper.cos(yawDegrees * 0.017453292F) * MathHelper.cos(pitchDegrees * 0.017453292F);
		return new Vec3f(x * _180divPi, y * _180divPi, z * _180divPi);
	}
}


