package com.gildedgames.aether.block.utility;

import com.gildedgames.aether.blockentity.FreezerBlockEntity;

import com.gildedgames.aether.client.particle.AetherParticleTypes;
import com.gildedgames.aether.blockentity.AetherBlockEntityTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.MenuProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class FreezerBlock extends AbstractFurnaceBlock
{
	public FreezerBlock(Properties properties) {
		super(properties);
	}
	
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
		return new FreezerBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> blockEntityType) {
		return createFurnaceTicker(level, blockEntityType, AetherBlockEntityTypes.FREEZER.get());
	}
	
	@Override
	protected void openContainer(Level level, @Nonnull BlockPos pos, @Nonnull Player player) {
		if (!level.isClientSide) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof FreezerBlockEntity) {
				player.openMenu((MenuProvider) blockEntity);
			}
		}
	}
	
	@Override
	public void animateTick(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
		if (state.getValue(LIT)) {
			double x = pos.getX() + 0.5;
			double y = pos.getY() + 1.0 + (random.nextFloat() * 6.0) / 16.0;
			double z = pos.getZ() + 0.5;

			level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);

			for (int i = 0; i < 10; ++i) {
				level.addParticle(AetherParticleTypes.FROZEN.get(), x, y, z, 0.0, 0.0, 0.0);
			}

			level.playLocalSound(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
		}
	}
}
