package com.gildedgames.aether.block.dungeon;

import java.util.function.Supplier;

import com.gildedgames.aether.client.AetherSoundEvents;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class TrappedBlock extends Block
{
	private final Supplier<EntityType<?>> entityTypeSupplier;
	private final Supplier<? extends BlockState> untrappedVariantSupplier;
	
	public TrappedBlock(Supplier<EntityType<?>> entityTypeSupplier, Supplier<? extends BlockState> untrappedVariantSupplier, Properties properties) {
		super(properties);
		this.entityTypeSupplier = entityTypeSupplier;
		this.untrappedVariantSupplier = untrappedVariantSupplier;
	}

	@Override
	public void stepOn(Level world, BlockPos pos, BlockState state, Entity entityIn) {
		RandomSource random = RandomSource.create();
		if (entityIn instanceof Player) {
			world.setBlockAndUpdate(pos, untrappedVariantSupplier.get());
			if (!world.isClientSide) {
				EntityType<?> entityType = entityTypeSupplier.get();
				Entity entity = entityType.create(world);
				entity.absMoveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
				if (entity instanceof Mob) {
					((Mob)entity).finalizeSpawn((ServerLevel)world, world.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.TRIGGERED, (SpawnGroupData)null, (CompoundTag)null);
				}
				world.addFreshEntity(entity);
			}
			world.playSound(null, pos, AetherSoundEvents.BLOCK_DUNGEON_TRAP_TRIGGER.get(), SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
		}
	}
}
