package com.aetherteam.aether.client.renderer.geo;

import com.aetherteam.aether.Aether;
import com.aetherteam.aether.client.renderer.entity.MoaRenderer;
import com.aetherteam.aether.entity.passive.Moa;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MoaGeoRenderer extends GeoEntityRenderer<Moa> {
    public MoaGeoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Model());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Moa moa) {
        return MoaRenderer.location(moa);
    }


    @Override
    public float getMotionAnimThreshold(Moa animatable) {
        return 0.001F;
    }


    public static class Model extends GeoModel<Moa> {

        // TODO: Easter egg like paradise lost maybe lol
        private static final ResourceLocation FALLBACK = new ResourceLocation(Aether.MODID, "textures/entity/mobs/moa/blue_moa.png");
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

    }
}
