package XiGyoku.furyborn.client.event;

import XiGyoku.furyborn.client.entity.RobyteBitLaserModel;
import XiGyoku.furyborn.client.gui.RobyteOutOfAreaOverlay;
import XiGyoku.furyborn.client.item.ModelBusterThrower;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

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

    public static final KeyMapping TOGGLE_BUSTER_MODE = new KeyMapping(
            "key.furyborn.toggle_buster_mode",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.furyborn"
    );

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RobyteBitLaserModel.LAYER_LOCATION, RobyteBitLaserModel::createBodyLayer);
        event.registerLayerDefinition(ModelBusterThrower.BUSTER_THROWER_LAYER, ModelBusterThrower::createLayerDefinition);
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_BUSTER_MODE);
    }
}