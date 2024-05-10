package com.aetherteam.aether.client.renderer.geo;

import com.aetherteam.aether.Aether;
import com.aetherteam.aether.client.AetherClient;
import com.aetherteam.aether.client.renderer.entity.MoaRenderer;
import com.aetherteam.aether.entity.passive.Moa;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MoaGeoRenderer extends GeoEntityRenderer<Moa> {
    public MoaGeoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Model());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Moa moa) {
        return MoaRenderer.location(moa);
    }

    public static class Model extends AetherGeoModel<Moa> {

        private static final ResourceLocation FALLBACK = new ResourceLocation(Aether.MODID, "textures/entity/mobs/moa/blue_moa.png");
        private static final ResourceLocation GEO_LOCATION = new ResourceLocation(Aether.MODID, "geo/moa.geo.json");
        private static final ResourceLocation ANIM_LOCATION = new ResourceLocation(Aether.MODID, "animations/moa.animation.json");

        private float limbSwing, limbSwingAmount;

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
        public void applyMolangQueries(Moa animatable, double animTime) {
            super.applyMolangQueries(animatable, animTime);
            MolangParser molangParser = MolangParser.INSTANCE;
            Minecraft mc = Minecraft.getInstance();

            // Limb swing calculation
            molangParser.setMemoizedValue(AetherClient.LIMB_MOVEMENT, () -> {
                float limbSwingAmount = 0;
                float limbSwing = 0;
                boolean shouldSit = animatable.isPassenger() && (animatable.getVehicle() != null && animatable.getVehicle().shouldRiderSit());
                if (!shouldSit && animatable.isAlive()) {
                    limbSwingAmount = animatable.walkAnimation.speed(mc.getPartialTick());
                    limbSwing = animatable.walkAnimation.position(mc.getPartialTick());

                    if (animatable.isBaby())
                        limbSwing *= 3f;

                    if (limbSwingAmount > 1f)
                        limbSwingAmount = 1f;
                }
                return Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * Mth.RAD_TO_DEG;
            });

            molangParser.setMemoizedValue(AetherClient.HEAD_X_ROT, () -> Mth.lerp(mc.getPartialTick(), animatable.xRotO, animatable.getXRot()));
            molangParser.setMemoizedValue(AetherClient.HEAD_Y_ROT, () -> this.calculateHeadY(animatable, mc.getPartialTick()));

        }


    }


}
