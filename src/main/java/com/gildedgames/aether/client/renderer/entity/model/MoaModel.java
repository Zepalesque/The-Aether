package com.gildedgames.aether.client.renderer.entity.model;


import com.gildedgames.aether.common.entity.passive.MoaEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

public class MoaModel extends BipedBirdModel<MoaEntity> {
	public boolean renderLegs;

	public MoaModel(float scale) {
		super(scale);
	}

	@Override
	public void prepareMobModel(MoaEntity moa, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.prepareMobModel(moa, limbSwing, limbSwingAmount, partialTicks);
		this.renderLegs = !moa.isSitting() || (!moa.isEntityOnGround() && moa.isSitting());
	}

	@Override
	public void setupAnim(MoaEntity moa, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(moa, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		if (moa.isSitting()) {
			this.jaw.xRot = 0.0F;
		} else {
			this.jaw.xRot = 0.35F;
		}
	}

	@Override
	public void renderToBuffer(MatrixStack poseStack, IVertexBuilder consumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		super.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
		if (this.renderLegs) {
			this.rightLeg.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
			this.leftLeg.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
		}
	}
}
