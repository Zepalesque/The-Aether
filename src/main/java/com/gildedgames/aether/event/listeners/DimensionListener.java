package com.gildedgames.aether.event.listeners;

import com.gildedgames.aether.event.events.PlacementBanEvent;
import com.gildedgames.aether.event.events.PlacementConvertEvent;
import com.gildedgames.aether.event.hooks.DimensionHooks;
import com.gildedgames.aether.data.resources.AetherDimensions;
import com.gildedgames.aether.world.AetherLevelData;
import com.gildedgames.aether.capability.time.AetherTime;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.core.Direction;

@Mod.EventBusSubscriber
public class DimensionListener {
    @SubscribeEvent
    public static void checkBanned(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos blockPos = event.getPos();
        Direction direction = event.getFace();
        ItemStack itemStack = event.getItemStack();
        BlockState blockState = level.getBlockState(blockPos);
        event.setCanceled(DimensionHooks.checkInteractionBanned(player, level, blockPos, direction, itemStack, blockState));
    }

    @SubscribeEvent
    public static void onNeighborNotified(BlockEvent.NeighborNotifyEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        BlockPos blockPos = event.getPos();
        DimensionHooks.checkExistenceBanned(levelAccessor, blockPos);
        DimensionHooks.sendIcestoneFreezableUpdateEvent(levelAccessor, blockPos);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onConvert(PlacementConvertEvent event) {
        LevelAccessor levelAccessor = event.getWorld();
        BlockPos blockPos = event.getPos();
        if (!event.isCanceled()) {
            DimensionHooks.banOrConvert(levelAccessor, blockPos);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBanned(PlacementBanEvent.SpawnParticles event) {
        LevelAccessor levelAccessor = event.getWorld();
        BlockPos blockPos = event.getPos();
        if (!event.isCanceled()) {
            DimensionHooks.banOrConvert(levelAccessor, blockPos);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        Level level = event.level;
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            DimensionHooks.tickTime(level);
            DimensionHooks.fallFromAether(level);
        }
    }

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        ResourceKey<Level> dimension = event.getDimension();
        DimensionHooks.dimensionTravel(entity, dimension);
    }

    @SubscribeEvent
    public static void onPlayerTraveling(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        DimensionHooks.travelling(player);
    }

    /**
     * Initializes the Aether level data for time separate from the overworld.
     * serverLevelData and levelData are access transformed.
     */
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimensionType().effectsLocation().equals(AetherDimensions.AETHER_DIMENSION_TYPE.location())) {
            AetherTime.get(level).ifPresent(cap -> {
                AetherLevelData levelData = new AetherLevelData(level.getServer().getWorldData(), level.getServer().getWorldData().overworldData(), cap.getDayTime());
                level.serverLevelData = levelData;
                level.levelData = levelData;
            });
        }
    }

    /**
     * Resets the weather cycle if players finish sleeping in an Aether dimension.
     */
    @SubscribeEvent
    public static void onSleepFinish(SleepFinishedTimeEvent event) {
        ServerLevel level = (ServerLevel) event.getLevel();
        if (level.dimensionType().effectsLocation().equals(AetherDimensions.AETHER_DIMENSION_TYPE.location())) {
            level.serverLevelData.setRainTime(0);
            level.serverLevelData.setRaining(false);
            level.serverLevelData.setThunderTime(0);
            level.serverLevelData.setThundering(false);
        }
    }
}
