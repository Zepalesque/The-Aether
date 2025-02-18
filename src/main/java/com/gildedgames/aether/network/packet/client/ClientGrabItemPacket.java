package com.gildedgames.aether.network.packet.client;

import com.gildedgames.aether.network.AetherPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;

public record ClientGrabItemPacket(int playerID, ItemStack stack) implements AetherPacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.playerID);
        buf.writeItem(this.stack);
    }

    public static ClientGrabItemPacket decode(FriendlyByteBuf buf) {
        int playerID = buf.readInt();
        ItemStack stack = buf.readItem();
        return new ClientGrabItemPacket(playerID, stack);
    }

    @Override
    public void execute(Player playerEntity) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null && Minecraft.getInstance().player.level.getEntity(this.playerID) instanceof LocalPlayer localPlayer) {
            localPlayer.containerMenu.setCarried(this.stack);
        }
    }
}
