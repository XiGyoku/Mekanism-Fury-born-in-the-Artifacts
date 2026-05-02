package XiGyoku.furyborn.network;

import XiGyoku.furyborn.Furyborn;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class FuryBornNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Furyborn.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, PacketToggleBusterMode.class,
                PacketToggleBusterMode::toBytes,
                PacketToggleBusterMode::new,
                PacketToggleBusterMode::handle);
        CHANNEL.registerMessage(id++, PacketShootLaserBit.class,
                PacketShootLaserBit::toBytes,
                PacketShootLaserBit::new,
                PacketShootLaserBit::handle);
        CHANNEL.registerMessage(id++, PacketToggleAfterImage.class,
                PacketToggleAfterImage::toBytes,
                PacketToggleAfterImage::new,
                PacketToggleAfterImage::handle);
        CHANNEL.registerMessage(id++, PacketDirectionalDash.class,
                PacketDirectionalDash::toBytes,
                PacketDirectionalDash::new,
                PacketDirectionalDash::handle);
        CHANNEL.registerMessage(id++, PacketTargetTeleport.class,
                PacketTargetTeleport::toBytes,
                PacketTargetTeleport::new,
                PacketTargetTeleport::handle);
        CHANNEL.registerMessage(id++, PacketToggleAfterImage.class,
                PacketToggleAfterImage::toBytes,
                PacketToggleAfterImage::new,
                PacketToggleAfterImage::handle);
        CHANNEL.registerMessage(id++, PacketSyncAfterImage.class,
                PacketSyncAfterImage::toBytes,
                PacketSyncAfterImage::new,
                PacketSyncAfterImage::handle);
        CHANNEL.registerMessage(id++, PacketUpdateHaloProjector.class,
                PacketUpdateHaloProjector::toBytes,
                PacketUpdateHaloProjector::new,
                PacketUpdateHaloProjector::handle);
        CHANNEL.registerMessage(id++, PacketBikeRebellion.class,
                PacketBikeRebellion::toBytes,
                PacketBikeRebellion::new,
                PacketBikeRebellion::handle
        );
    }
}