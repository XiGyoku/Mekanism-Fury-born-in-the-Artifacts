package XiGyoku.furyborn.event.client;

import XiGyoku.furyborn.client.gui.RobyteOutOfAreaOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "furyborn", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FuryBornEventBusClientEvents {
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(
                "robyte_out_of_area_overlay",
                (gui, guiGraphics, partialTick, width, height) -> {
                    RobyteOutOfAreaOverlay.render(guiGraphics, partialTick);
                }
        );
    }
}