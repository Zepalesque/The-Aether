package com.aetherteam.aether.client.renderer.geo;

import com.aetherteam.aether.Aether;
import com.aetherteam.aether.client.AetherClient;
import com.aetherteam.aether.client.renderer.entity.MoaRenderer;
import com.aetherteam.aether.client.renderer.geo.layers.MoaEmissiveGeoLayer;
import com.aetherteam.aether.entity.passive.Moa;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MoaGeoRenderer extends GeoEntityRenderer<Moa> {
    public MoaGeoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Model());
        addRenderLayer(new MoaEmissiveGeoLayer(this));
    }

    @Override
    public void preRender(PoseStack poseStack, Moa moa, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        float moaScale = moa.isBaby() ? 0.5F : 0.9F;
        poseStack.scale(moaScale, moaScale, moaScale);
        super.preRender(poseStack, moa, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Moa moa) {
        return MoaRenderer.location(moa);
    }

    public static class Model extends AetherGeoModel<Moa> {

        private static final ResourceLocation GEO_LOCATION = new ResourceLocation(Aether.MODID, "geo/moa.geo.json");
        private static final ResourceLocation ANIM_LOCATION = new ResourceLocation(Aether.MODID, "animations/moa.animation.json");

        @Override
        public ResourceLocation getModelResource(Moa moa) {
            return GEO_LOCATION;
        }

        @Override
        public ResourceLocation getTextureResource(Moa moa) {
            return MoaRenderer.location(moa);
        }

        @Override
        public ResourceLocation getAnimationResource(Moa moa) {
            return ANIM_LOCATION;
        }

        @Override
        public void applyMolangQueries(Moa moa, double animTime) {
            super.applyMolangQueries(moa, animTime);
            MolangParser molangParser = MolangParser.INSTANCE;
            Minecraft mc = Minecraft.getInstance();

            // Limb swing calculation
            molangParser.setMemoizedValue(AetherClient.LIMB_MOVEMENT, () -> {
                float limbSwingAmount = 0;
                float limbSwing = 0;
                boolean shouldSit = moa.isPassenger() && (moa.getVehicle() != null && moa.getVehicle().shouldRiderSit());
                if (!shouldSit && moa.isAlive()) {
                    limbSwingAmount = moa.walkAnimation.speed(mc.getPartialTick());
                    limbSwing = moa.walkAnimation.position(mc.getPartialTick());

                    if (moa.isBaby())
                        limbSwing *= 3f;

                    if (limbSwingAmount > 1f)
                        limbSwingAmount = 1f;
                }
                return Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * Mth.RAD_TO_DEG;
            });

            molangParser.setMemoizedValue(AetherClient.HEAD_X_ROT, () -> Mth.lerp(mc.getPartialTick(), moa.xRotO, moa.getXRot()));
            molangParser.setMemoizedValue(AetherClient.HEAD_Y_ROT, () -> this.calculateHeadY(moa, mc.getPartialTick()));

        }


    }


}
