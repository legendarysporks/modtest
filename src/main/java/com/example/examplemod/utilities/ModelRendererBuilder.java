package com.example.examplemod.utilities;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/*
The coordinate system is as follows as values increase.  X goes right, y goes down, z goes toward you.
 */
public class ModelRendererBuilder {
	public static Builder model(ModelBase model) {
		return new Builder(model);
	}

	public static class Builder {
/*				this.shape2 = new ModelRenderer(this, 0, 24);
				this.shape2.setRotationPoint(0.0F, 3.0F, 0.0F);
				this.shape2.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, 0.0F);
				this.setRotateAngle(shape2, 0.0F, 0.39269908169872414F, 0.0F); */

		public int xTextureOffset;
		public int yTextureOffset;
		public float xRotPoint;
		public float yRotPoint;
		public float zRotPoint;
		public float xRot;
		public float yRot;
		public float zRot;
		public ModelRenderer renderer;

		public Builder(ModelBase model) {
			renderer = new ModelRenderer(model);
		}

		public Builder(ModelRenderer renderer) {
			this.renderer = renderer;
		}

		public Builder textureOffset(int x, int y) {
			xTextureOffset = x;
			yTextureOffset = y;
			return this;
		}

		public Builder rotationPoint(float x, float y, float z) {
			xRotPoint = x;
			yRotPoint = y;
			zRotPoint = z;
			return this;
		}

		public Builder rotation(float x, float y, float z) {
			xRot = x;
			yRot = y;
			zRot = z;
			return this;
		}

		public BoxBuilder box() {
			renderer.setTextureOffset(xTextureOffset, yTextureOffset);
			renderer.setRotationPoint(xRotPoint, yRotPoint, zRotPoint);
			renderer.rotateAngleX = xRot;
			renderer.rotateAngleY = yRot;
			renderer.rotateAngleZ = zRot;
			return new BoxBuilder(this);
		}

		public ModelRenderer create() {
			box();
			return renderer;
		}
	}

	public static class BoxBuilder {
		public float xPos;
		public float yPos;
		public float zPos;
		public int xSize;
		public int ySize;
		public int zSize;
		public Builder builder;

		private BoxBuilder(Builder builder) {
			this.builder = builder;
		}

		public BoxBuilder set(float xPos, float yPos, float zPos, int xSize, int ySize, int zSize) {
			this.xPos = xPos;
			this.yPos = yPos;
			this.zPos = zPos;
			this.xSize = xSize;
			this.ySize = ySize;
			this.zSize = zSize;
			return this;
		}

		public BoxBuilder at(float x, float y, float z) {
			xPos = x;
			yPos = y;
			zPos = z;
			return this;
		}

		public BoxBuilder centerAt(float x, float y, float z) {
			xPos = x - xSize / 2.0f;
			yPos = y - ySize / 2.0f;
			zPos = z - zSize / 2.0f;
			return this;
		}

		public BoxBuilder offsetBy(float x, float y, float z) {
			xPos += x;
			yPos += y;
			zPos += z;
			return this;
		}

		public BoxBuilder grownTo(int xSize, int ySize, int zSize) {
			xPos -= xSize / 2.0f;
			yPos -= ySize / 2.0f;
			zPos -= zSize / 2.0f;
			sizedTo(xSize, ySize, zSize);
			return this;
		}

		public BoxBuilder sizedTo(int xSize, int ySize, int zSize) {
			this.xSize = xSize;
			this.ySize = ySize;
			this.zSize = zSize;
			return this;
		}

		public BoxBuilder alignTop(float y) {
			yPos = y - ySize;
			return this;
		}

		public BoxBuilder alignBottom(float y) {
			yPos = y;
			return this;
		}

		public BoxBuilder alignLeft(float x) {
			xPos = x;
			return this;
		}

		public BoxBuilder alignRight(float x) {
			xPos = x - xSize;
			return this;
		}

		public BoxBuilder alignFront(float z) {
			zPos = z - zSize;
			return this;
		}

		public BoxBuilder alignBack(float z) {
			zPos = z;
			return this;
		}

		public BoxBuilder box() {
			builder.renderer.addBox(xPos, yPos, zPos, xSize, ySize, zSize, true);
			xPos = 0;
			yPos = 0;
			zPos = 0;
			xSize = 0;
			ySize = 0;
			zSize = 0;
			return this;
		}

		public Builder textureOffset(int x, int y) {
			return builder.textureOffset(x, y);
		}

		public Builder rotationPoint(float x, float y, float z) {
			return builder.rotationPoint(x, y, z);
		}

		public Builder rotation(float x, float y, float z) {
			return builder.rotation(x, y, z);
		}

		public ModelRenderer create() {
			box();
			return builder.renderer;
		}

		public Builder model() {
			return builder;
		}

		/*

			private static Vec3f angleCrap(float pitchDegrees, float yawDegrees) {
			// from EntityThrowable.shoot
			float yawRad = deg2rad(yawDegrees);
			float pitchRad = deg2rad(pitchDegrees);


			float fX = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad);
			float fY = -MathHelper.sin(pitchRad);
			float fZ = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad);



			float x = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad);
			float y = -MathHelper.sin(pitchRad);
			float z = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad);
			float magnitude = MathHelper.sqrt(x * x + y * y + z * z);
			x = x / magnitude;
			y = y / magnitude;
			z = z / magnitude;
			return new Vec3f(rad2deg(x), rad2deg(y), rad2deg(z));
		}

		private static Vec2f angleCrap(float x, float y, float z) {
			float f1 = MathHelper.sqrt(x * x + z * z);
			float yaw = rad2deg((float)MathHelper.atan2(x, z));
			float pitch = rad2deg((float)MathHelper.atan2(y, (double)f1));
			return new Vec2f(pitch, yaw);
		}

		private static final float _piDiv180 = (float)Math.PI / 180.0f;
		private static final float _180DivPi = (float)(180f / Math.PI);

		private static float deg2rad(float v) {
			return v * _piDiv180;
		}

		private static float rad2deg(float v) {
			return (float)(v * _180DivPi);
		}

					private final Vec3f getVectorForRotation(float pitch, float yaw)
				{
					float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
					float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
					float f2 = -MathHelper.cos(-pitch * 0.017453292F);
					float f3 = MathHelper.sin(-pitch * 0.017453292F);
					return new Vec3f((f1 * f2), f3, (f * f2));
				}



		 */
	}

	public static void addChildRenderer(ModelRenderer parent, ModelRenderer child) {
		// move child rotation point to be relative to parent
		child.rotationPointX -= parent.rotationPointX;
		child.rotationPointY -= parent.rotationPointY;
		child.rotationPointZ -= parent.rotationPointZ;
		// make rotations relative to parent
		child.rotateAngleX -= parent.rotateAngleX;
		child.rotateAngleY -= parent.rotateAngleY;
		child.rotateAngleZ -= parent.rotateAngleZ;
		// create relationship
		parent.addChild(child);
	}
}
