package com.gildedgames.aether.client.gui.screen.inventory;

import com.gildedgames.aether.Aether;
import com.gildedgames.aether.client.gui.screen.inventory.recipebook.FreezerRecipeBookComponent;
import com.gildedgames.aether.inventory.menu.FreezerMenu;

import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class FreezerScreen extends AbstractAetherFurnaceScreen<FreezerMenu> {
	private static final ResourceLocation FREEZER_GUI_TEXTURES = new ResourceLocation(Aether.MODID, "textures/gui/menu/freezer.png");
	
	public FreezerScreen(FreezerMenu menu, Inventory inventory, Component title) {
		super(menu, new FreezerRecipeBookComponent(), inventory, title, FREEZER_GUI_TEXTURES);
	}
}
