package com.example.examplemod.utilities;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

/*
The coordinate system is as follows as values increase.  X goes right, y goes down, z goes toward you.
 */
public class BoxUtils {
	public float xPos;
	public float yPos;
	public float zPos;
	public int xSize;
	public int ySize;
	public int zSize;
	public int textureX;
	public int textureY;
	public final ModelRenderer renderer;

	public BoxUtils(ModelRenderer renderer) {
		this.renderer = renderer;
	}

	public BoxUtils(ModelBase model, float rotationPointXIn, float rotationPointYIn, float rotationPointZIn) {
		this(new ModelRenderer(model));
		renderer.setRotationPoint(rotationPointXIn, rotationPointYIn, rotationPointZIn);
	}

	public BoxUtils set(float xPos, float yPos, float zPos, int xSize, int ySize, int zSize) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.zPos = zPos;
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;
		return this;
	}

	public BoxUtils at(float x, float y, float z) {
		xPos = x;
		yPos = y;
		zPos = z;
		return this;
	}

	public BoxUtils centerAt(float x, float y, float z) {
		xPos = x - xSize / 2.0f;
		yPos = y - ySize / 2.0f;
		zPos = z - zSize / 2.0f;
		return this;
	}

	public BoxUtils offsetBy(float x, float y, float z) {
		xPos += x;
		yPos += y;
		zPos += z;
		return this;
	}

	public BoxUtils grownTo(int xSize, int ySize, int zSize) {
		xPos -= xSize / 2.0f;
		yPos -= ySize / 2.0f;
		zPos -= zSize / 2.0f;
		sizedTo(xSize, ySize, zSize);
		return this;
	}

	public BoxUtils sizedTo(int xSize, int ySize, int zSize) {
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;
		return this;
	}

	public BoxUtils alignTop(float y) {
		yPos = y - ySize;
		return this;
	}

	public BoxUtils alignBottom(float y) {
		yPos = y;
		return this;
	}

	public BoxUtils alignLeft(float x) {
		xPos = x;
		return this;
	}

	public BoxUtils alignRight(float x) {
		xPos = x - xSize;
		return this;
	}

	public BoxUtils alignFront(float z) {
		zPos = z - zSize;
		return this;
	}

	public BoxUtils alignBack(float z) {
		zPos = z;
		return this;
	}


	public BoxUtils texture(int x, int y) {
		textureX = x;
		textureY = y;
		return this;
	}

	public BoxUtils reset() {
		xPos = 0;
		yPos = 0;
		zPos = 0;
		xSize = 0;
		ySize = 0;
		zSize = 0;
		textureX = 0;
		textureY = 0;
		return this;
	}

	public ModelRenderer done() {
//		renderer.addBox(xPos, yPos, zPos, xSize, ySize, zSize, true);
		renderer.cubeList.add(new ModelBox(renderer, textureX, textureY, xPos, yPos, zPos, xSize, ySize, zSize, 0.0F, true));
		reset();
		return renderer;
	}

	public BoxUtils childOf(ModelRenderer parent) {
		// move child rotation point to be relative to parent
		renderer.rotationPointX -= parent.rotationPointX;
		renderer.rotationPointY -= parent.rotationPointY;
		renderer.rotationPointZ -= parent.rotationPointZ;
		// make rotations relative to parent
		renderer.rotateAngleX -= parent.rotateAngleX;
		renderer.rotateAngleY -= parent.rotateAngleY;
		renderer.rotateAngleZ -= parent.rotateAngleZ;
		// create relationship
		parent.addChild(renderer);

		return this;
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
