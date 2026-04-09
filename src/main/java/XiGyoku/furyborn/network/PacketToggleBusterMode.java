package XiGyoku.furyborn.network;

import XiGyoku.furyborn.item.ItemBusterThrower;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleBusterMode {
    public PacketToggleBusterMode() {}

    public PacketToggleBusterMode(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (!(stack.getItem() instanceof ItemBusterThrower)) {
                    stack = player.getItemInHand(InteractionHand.OFF_HAND);
                }
                if (stack.getItem() instanceof ItemBusterThrower) {
                    ItemBusterThrower.cycleBusterMode(stack);
                    int mode = ItemBusterThrower.getBusterMode(stack);
                    String langKey = mode == 2 ? "overcharge" : (mode == 1 ? "explosion" : "none");
                    net.minecraft.ChatFormatting color = mode == 2 ? ChatFormatting.LIGHT_PURPLE : (mode == 1 ? net.minecraft.ChatFormatting.RED : net.minecraft.ChatFormatting.AQUA);
                    Component prefix = Component.literal("[FuryBorn] ").withStyle(net.minecraft.ChatFormatting.AQUA);
                    Component modeName = Component.translatable("message.furyborn.buster_mode." + langKey).withStyle(color);
                    Component message = Component.translatable("message.furyborn.buster_mode", prefix, modeName).withStyle(net.minecraft.ChatFormatting.WHITE);
                    player.sendSystemMessage(message);
                }
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}