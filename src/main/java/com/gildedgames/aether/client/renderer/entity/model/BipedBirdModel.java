package com.gildedgames.aether.client.renderer.entity.model;

import com.gildedgames.aether.common.entity.miscellaneous.NotGrounded;
import com.gildedgames.aether.common.entity.miscellaneous.WingedBird;
import com.gildedgames.aether.core.AetherConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public abstract class BipedBirdModel<T extends Entity & WingedBird & NotGrounded> extends EntityModel<T> {
    public final ModelRenderer head;
    public final ModelRenderer jaw;
    public final ModelRenderer neck;
    public final ModelRenderer body;
    public final ModelRenderer rightLeg;
    public final ModelRenderer leftLeg;
    public final ModelRenderer rightWing;
    public final ModelRenderer leftWing;
    public final ModelRenderer rightTailFeather;
    public final ModelRenderer middleTailFeather;
    public final ModelRenderer leftTailFeather;

    public BipedBirdModel(float scale) {
        this.head = new ModelRenderer(this, 0, 13);
        this.head.addBox(-2.0F, -4.0F, -6.0F, 4.0F, 4.0F, 8.0F, 0.0F);
        this.head.setPos(0.0F, 8.0F, -4.0F);

        this.jaw = new ModelRenderer(this, 24, 13);
        this.jaw.addBox(-2.0F, -1.0F, -6.0F, 4.0F, 1.0F, 8.0F, -0.1F);
        this.head.addChild(this.jaw);

        this.neck = new ModelRenderer(this, 22, 0);
        this.neck.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, 0.0F);
        this.head.addChild(this.neck);

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-3.0F, -3.0F, 0.0F, 6.0F, 8.0F, 5.0F, scale);
        this.body.setPos(0.0F, 16.0F, 0.0F);


        this.rightLeg = new ModelRenderer(this, 54, 21);
        this.rightLeg.addBox(-0.99F, -1.0F, -1.0F, 2.0F, 9.0F, 2.0F, 0.0F);
        this.rightLeg.setPos(-2.0F, 16.0F, 1.0F);

        this.leftLeg = new ModelRenderer(this, 46, 21);
        this.leftLeg.addBox(-1.01F, -1.0F, -1.0F, 2.0F, 9.0F, 2.0F, 0.0F);
        this.leftLeg.setPos(2.0F, 16.0F, 1.0F);

        this.rightWing = new ModelRenderer(this, 40, 0);
        this.rightWing.addBox(-1.0F, 0.0F, -2.0F, 1.0F, 8.0F, 4.0F, 0.0F);
        this.rightWing.setPos(-3.001F, -3.0F, 3.0F);
        this.body.addChild(this.rightWing);

        this.leftWing = new ModelRenderer(this, 30, 0);
        this.leftWing.addBox(0.0F, 0.0F, -2.0F, 1.0F, 8.0F, 4.0F, 0.0F);
        this.leftWing.setPos(3.001F, -3.0F, 3.0F);
        this.body.addChild(this.leftWing);

        this.rightTailFeather = new ModelRenderer(this, 0, 26);
        this.rightTailFeather.addBox(-1.0F, -5.0F, 5.0F, 2.0F, 1.0F, 5.0F, -0.3F);
        this.rightTailFeather.setPos(0.0F, 17.5F, 1.0F);

        this.middleTailFeather = new ModelRenderer(this, 14, 26);
        this.middleTailFeather.addBox(-1.0F, -5.0F, 5.0F, 2.0F, 1.0F, 5.0F, -0.3F);
        this.middleTailFeather.setPos(0.0F, 17.5F, 1.0F);

        this.leftTailFeather = new ModelRenderer(this, 28, 26);
        this.leftTailFeather.addBox(-1.0F, -5.0F, 5.0F, 2.0F, 1.0F, 5.0F, -0.3F);
        this.leftTailFeather.setPos(0.0F, 17.5F, 1.0F);

    }
    public static final float DEG_TO_RAD = 0.017453292F;
    public static final float RAD_TO_DEG = 57.295776F;



    @Override
    public void setupAnim(T bipedBird, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.xRot = headPitch * DEG_TO_RAD;
        this.head.yRot = netHeadYaw * DEG_TO_RAD;
        this.neck.xRot = -this.head.xRot;

        if (!bipedBird.isOnGround() && !AetherConfig.CLIENT.inverted_moa_anims.get() || bipedBird.isOnGround() && AetherConfig.CLIENT.inverted_moa_anims.get()) {
            this.rightWing.setPos(-3.001F, 0.0F, 4.0F);
            this.leftWing.setPos(3.001F, 0.0F, 4.0F);
            this.rightWing.xRot = -((float) Math.PI / 2F);
            this.leftWing.xRot = this.rightWing.xRot;
            this.rightLeg.xRot = 0.6F;
            this.leftLeg.xRot = this.rightLeg.xRot;
            this.rightWing.yRot = ageInTicks;
        } else {
            this.rightWing.setPos(-3.001F, -3.0F, 3.0F);
            this.leftWing.setPos(3.001F, -3.0F, 3.0F);
            this.rightWing.xRot = 0.0F;
            this.leftWing.xRot = 0.0F;
            this.rightLeg.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            this.leftLeg.xRot = MathHelper.cos(limbSwing * 0.6662F + ((float)Math.PI)) * 1.4F * limbSwingAmount;
            this.rightWing.yRot = 0.0F;
        }

        this.leftWing.yRot = -this.rightWing.yRot;
    }

    public float setupWingsAnimation(T bipedBird, float partialTicks) {
        float rotVal = MathHelper.lerp(partialTicks, bipedBird.getPrevWingRotation(), bipedBird.getWingRotation());
        float destVal = MathHelper.lerp(partialTicks, bipedBird.getPrevWingDestPos(), bipedBird.getWingDestPos());
        return (MathHelper.sin(rotVal * 0.225F) + 1.0F) * destVal;
    }

    @Override
    public void renderToBuffer(MatrixStack poseStack, IVertexBuilder consumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.head.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.body.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightTailFeather.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.middleTailFeather.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftTailFeather.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
