package XiGyoku.furyborn.network;

import XiGyoku.furyborn.entity.RoadBikeBitEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketBikeRebellion {
    private final boolean isPressed;

    public PacketBikeRebellion(boolean isPressed) {
        this.isPressed = isPressed;
    }

    public PacketBikeRebellion(FriendlyByteBuf buf) {
        this.isPressed = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isPressed);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player != null && player.getVehicle() instanceof RoadBikeBitEntity bike) {
                bike.isRebellionKeyPressed = this.isPressed;
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}