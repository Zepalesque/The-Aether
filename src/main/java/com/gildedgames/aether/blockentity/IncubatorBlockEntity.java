package com.gildedgames.aether.blockentity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gildedgames.aether.Aether;
import com.gildedgames.aether.inventory.menu.IncubatorMenu;

import com.gildedgames.aether.AetherTags;
import com.gildedgames.aether.recipe.AetherRecipeTypes;
import com.gildedgames.aether.recipe.recipes.item.IncubationRecipe;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class IncubatorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible
{
	private static final int[] SLOTS_NS = {0};
	private static final int[] SLOTS_EW = {1};
	protected NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
	private int litTime;
	private int incubationProgress;
	private int incubationTotalTime;
	protected final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> IncubatorBlockEntity.this.litTime;
				case 1 -> IncubatorBlockEntity.this.incubationProgress;
				case 2 -> IncubatorBlockEntity.this.incubationTotalTime;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0 -> IncubatorBlockEntity.this.litTime = value;
				case 1 -> IncubatorBlockEntity.this.incubationProgress = value;
				case 2 -> IncubatorBlockEntity.this.incubationTotalTime = value;
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	};
	private static final Map<Item, Integer> incubatingMap = new LinkedHashMap<>();
	private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();
	private final RecipeManager.CachedCheck<Container, IncubationRecipe> quickCheck;

	public IncubatorBlockEntity(BlockPos pos, BlockState state) {
		this(pos, state, AetherRecipeTypes.INCUBATION.get());
	}

	public IncubatorBlockEntity(BlockPos blockPos, BlockState blockState, RecipeType<IncubationRecipe> recipeType) {
		super(AetherBlockEntityTypes.INCUBATOR.get(), blockPos, blockState);
		this.quickCheck = RecipeManager.createCheck(recipeType);
	}

	@Nonnull
	@Override
	protected AbstractContainerMenu createMenu(int id, @Nonnull Inventory playerInventory) {
		return new IncubatorMenu(id, playerInventory, this, this.dataAccess);
	}

	@Nonnull
	@Override
	protected Component getDefaultName() {
		return Component.translatable("menu." + Aether.MODID + ".incubator");
	}

	public static Map<Item, Integer> getIncubatingMap() {
		return incubatingMap;
	}

	private static void addItemTagIncubatingTime(TagKey<Item> itemTag, int burnTime) {
		for(Holder<Item> holder : Registry.ITEM.getTagOrEmpty(itemTag)) {
			incubatingMap.put(holder.value(), burnTime);
		}
	}

	public static void addItemIncubatingTime(ItemLike itemProvider, int burnTime) {
		Item item = itemProvider.asItem();
		incubatingMap.put(item, burnTime);
	}

	private boolean isLit() {
		return this.litTime > 0;
	}

	@Override
	public void load(@Nonnull CompoundTag tag) {
		super.load(tag);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, this.items);
		this.litTime = tag.getInt("LitTime");
		this.incubationProgress = tag.getInt("IncubationProgress");
		this.incubationTotalTime = tag.getInt("IncubationTotalTime");
		CompoundTag compoundtag = tag.getCompound("RecipesUsed");
		for (String string : compoundtag.getAllKeys()) {
			this.recipesUsed.put(new ResourceLocation(string), compoundtag.getInt(string));
		}
	}

	@Override
	public void saveAdditional(@Nonnull CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("LitTime", this.litTime);
		tag.putInt("IncubationProgress", this.incubationProgress);
		tag.putInt("IncubationTotalTime", this.incubationTotalTime);
		ContainerHelper.saveAllItems(tag, this.items);
		CompoundTag compoundTag = new CompoundTag();
		this.recipesUsed.forEach((location, integer) -> compoundTag.putInt(location.toString(), integer));
		tag.put("RecipesUsed", compoundTag);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, IncubatorBlockEntity blockEntity) {
		boolean flag = blockEntity.isLit();
		boolean flag1 = false;

		if (blockEntity.isLit()) {
			--blockEntity.litTime;
		}

		ItemStack itemstack = blockEntity.items.get(1);
		boolean flag2 = !blockEntity.items.get(0).isEmpty();
		boolean flag3 = !itemstack.isEmpty();
		if (blockEntity.isLit() || flag3 && flag2) {
			IncubationRecipe recipe;
			if (flag2) {
				recipe = blockEntity.quickCheck.getRecipeFor(blockEntity, level).orElse(null);
			} else {
				recipe = null;
			}

			if (!blockEntity.isLit() && blockEntity.canIncubate(recipe, blockEntity.items)) {
				blockEntity.litTime = blockEntity.getBurnDuration(itemstack);
				if (blockEntity.isLit()) {
					flag1 = true;
					if (itemstack.hasCraftingRemainingItem()) {
						blockEntity.items.set(1, itemstack.getCraftingRemainingItem());
					} else if (flag3) {
						itemstack.shrink(1);
						if (itemstack.isEmpty()) {
							blockEntity.items.set(1, itemstack.getCraftingRemainingItem());
						}
					}
				}
			}

			if (blockEntity.isLit() && blockEntity.canIncubate(recipe, blockEntity.items)) {
				++blockEntity.incubationProgress;
				if (blockEntity.incubationProgress == blockEntity.incubationTotalTime) {
					blockEntity.incubationProgress = 0;
					blockEntity.incubationTotalTime = getTotalIncubationTime(level, blockEntity);
					if (blockEntity.incubate(recipe, blockEntity.items)) {
						blockEntity.setRecipeUsed(recipe);
					}
					flag1 = true;
				}
			} else {
				blockEntity.incubationProgress = 0;
			}
		} else if (!blockEntity.isLit() && blockEntity.incubationProgress > 0) {
			blockEntity.incubationProgress = Mth.clamp(blockEntity.incubationProgress - 2, 0, blockEntity.incubationTotalTime);
		}

		if (flag != blockEntity.isLit()) {
			flag1 = true;
			state = state.setValue(AbstractFurnaceBlock.LIT, blockEntity.isLit());
			level.setBlock(pos, state, 3);
		}

		if (flag1) {
			setChanged(level, pos, state);
		}
	}

	private boolean canIncubate(@Nullable IncubationRecipe recipe, NonNullList<ItemStack> stacks) {
		return !stacks.get(0).isEmpty() && recipe != null;
	}

	private boolean incubate(@Nullable IncubationRecipe recipe, NonNullList<ItemStack> stacks) {
		if (recipe != null && this.canIncubate(recipe, stacks)) {
			ItemStack itemStack = stacks.get(0);
			EntityType<?> entityType = recipe.getEntity();
			BlockPos spawnPos = this.worldPosition.above();
			if (this.getLevel() != null && !this.getLevel().isClientSide() && this.getLevel() instanceof ServerLevel serverLevel) {
				CompoundTag tag = recipe.getTag();
				Component customName = itemStack.hasCustomHoverName() ? itemStack.getHoverName() : null;
				entityType.spawn(serverLevel, tag, customName, null, spawnPos, MobSpawnType.TRIGGERED, true, false);
			}
			itemStack.shrink(1);
			return true;
		} else {
			return false;
		}
	}

	protected int getBurnDuration(ItemStack fuelStack) {
		if (fuelStack.isEmpty() || !getIncubatingMap().containsKey(fuelStack.getItem())) {
			return 0;
		} else {
			return getIncubatingMap().get(fuelStack.getItem());
		}
	}

	private static int getTotalIncubationTime(Level level, IncubatorBlockEntity blockEntity) {
		return blockEntity.quickCheck.getRecipeFor(blockEntity, level).map(IncubationRecipe::getIncubationTime).orElse(5700);
	}

	@Nonnull
	public int[] getSlotsForFace(@Nonnull Direction side) {
		if (side == Direction.NORTH || side == Direction.SOUTH) {
			return SLOTS_NS;
		} else {
			return SLOTS_EW;
		}
	}

	public boolean canPlaceItemThroughFace(int index, @Nonnull ItemStack stack, @Nullable Direction direction) {
		 if (index == 0) {
			 return stack.is(AetherTags.Items.MOA_EGGS);
		} else {
			 return this.getBurnDuration(stack) > 0;
		}
	}

	@Override
	public boolean canTakeItemThroughFace(int index, @Nonnull ItemStack stack, @Nonnull Direction direction) {
		return false;
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.items) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Nonnull
	@Override
	public ItemStack getItem(int index) {
		return this.items.get(index);
	}

	@Nonnull
	@Override
	public ItemStack removeItem(int index, int count) {
		return ContainerHelper.removeItem(this.items, index, count);
	}

	@Nonnull
	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ContainerHelper.takeItem(this.items, index);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		ItemStack itemstack = this.items.get(index);
		boolean flag = !stack.isEmpty() && stack.sameItem(itemstack) && ItemStack.tagMatches(stack, itemstack);
		this.items.set(index, stack);
		if (stack.getCount() > this.getMaxStackSize()) {
			stack.setCount(this.getMaxStackSize());
		}

		if (index == 0 && !flag) {
			this.incubationTotalTime = getTotalIncubationTime(this.level, this);
			this.incubationProgress = 0;
			this.setChanged();
		}
	}

	public boolean stillValid(@Nonnull Player player) {
		if (this.level.getBlockEntity(this.worldPosition) != this) {
			return false;
		} else {
			return player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
		}
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	public void setRecipeUsed(@Nullable Recipe<?> recipe) {
		if (recipe != null) {
			ResourceLocation resourcelocation = recipe.getId();
			this.recipesUsed.addTo(resourcelocation, 1);
		}
	}

	@Nullable
	@Override
	public Recipe<?> getRecipeUsed() {
		return null;
	}

	@Override
	public void awardUsedRecipes(@Nonnull Player player) {
	}

	@Override
	public void fillStackedContents(@Nonnull StackedContents helper) {
		for(ItemStack itemstack : this.items) {
			helper.accountStack(itemstack);
		}
	}

	LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (facing == Direction.NORTH)
				return handlers[0].cast();
			else if (facing == Direction.SOUTH)
				return handlers[1].cast();
			else if (facing == Direction.EAST)
				return handlers[2].cast();
			else
				return handlers[3].cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		for (LazyOptional<? extends IItemHandler> handler : this.handlers) {
			handler.invalidate();
		}
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		this.handlers = SidedInvWrapper.create(this, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
	}
}
