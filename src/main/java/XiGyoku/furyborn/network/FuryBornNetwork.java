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
    }
}