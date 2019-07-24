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
		private final ModelRenderer hammer;

		public Model3() {
			textureWidth = 16;
			textureHeight = 16;

			if (true) {
				BoxUtils boxes = new BoxUtils(this, 0F, -7.0F, 0F);
				boxes.at(-0.5F, 0.0F, -0.5F).sizedTo(1, 7, 1).texture(0, 8).done();
				boxes.at(-1.5F, 7.0F, -2.0F).sizedTo(3, 3, 4).done();
				hammer = boxes.renderer;
			} else {
				hammer = new ModelRenderer(this);
				hammer.setRotationPoint(0F, -7.0F, 0F);
				hammer.cubeList.add(new ModelBox(hammer, 0, 8, -0.5F, 0.0F, -0.5F, 1, 7, 1, 0.0F, true));
				hammer.cubeList.add(new ModelBox(hammer, 0, 0, -1.5F, 7.0F, -2.0F, 3, 3, 4, 0.0F, true));
			}
		}

		public void renderHammer(float scale) {
			hammer.render(scale);
		}

		@Override
		public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float yaw, float pitch, float scale) {
			hammer.render(scale);
		}
	}
}


