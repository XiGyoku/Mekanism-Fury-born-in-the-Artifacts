package XiGyoku.furyborn.client.screen;

import XiGyoku.furyborn.blockentity.HaloProjectorBlockEntity;
import net.minecraft.client.Minecraft;

public class ClientScreenHelper {
    public static void openHaloProjectorScreen(HaloProjectorBlockEntity blockEntity) {
        Minecraft.getInstance().setScreen(new HaloProjectorScreen(blockEntity));
    }
}