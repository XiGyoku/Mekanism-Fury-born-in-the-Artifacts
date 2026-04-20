package XiGyoku.furyborn.network;

import XiGyoku.furyborn.event.PlayerDriveshiftEvent;
import XiGyoku.furyborn.sound.FuryBornSounds;
import mekanism.api.Action;
import mekanism.api.math.FloatingLong;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketToggleAfterImage {

    private static final FloatingLong ACTIVATION_COST = FloatingLong.createConst(1_000_000_000L);

    public PacketToggleAfterImage() {}

    public PacketToggleAfterImage(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                boolean currentState = player.getPersistentData().getBoolean("ExolumenAfterImage");
                boolean newState = !currentState;

                if (newState && !player.isCreative() && !PlayerDriveshiftEvent.checkAndConsumeEnergy(player, ACTIVATION_COST, Action.SIMULATE)) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), FuryBornSounds.ROBYTE_TELEPORT.get(), SoundSource.PLAYERS, 1.0F, 0.05F);
                    FuryBornNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PacketSyncAfterImage(currentState));
                    return;
                }

                player.getPersistentData().putBoolean("ExolumenAfterImage", newState);

                Level level = player.level();
                level.playSound(null, player.getX(), player.getY(), player.getZ(), FuryBornSounds.ROBYTE_TELEPORT.get(), SoundSource.PLAYERS, 0.1F, 1.0F);
                FuryBornNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PacketSyncAfterImage(newState));
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}