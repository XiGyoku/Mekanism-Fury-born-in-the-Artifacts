package XiGyoku.furyborn.client.event;

import XiGyoku.furyborn.client.entity.RobyteBitLaserModel;
import XiGyoku.furyborn.client.entity.SunRaiserDriveModel;
import XiGyoku.furyborn.client.gui.RobyteOutOfAreaOverlay;
import XiGyoku.furyborn.client.gui.DriveshiftTintOverlay;
import XiGyoku.furyborn.client.item.ModelBusterThrower;
import XiGyoku.furyborn.client.util.ExolumenControllerRenderer;
import XiGyoku.furyborn.client.util.HaloProjectorRenderer;
import XiGyoku.furyborn.blockentity.FuryBornBlockEntities;
import XiGyoku.furyborn.client.util.RobitAfterImageLayer;
import com.mojang.blaze3d.platform.InputConstants;
import mekanism.common.registries.MekanismEntityTypes;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "furyborn", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FuryBornModClientEvents {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(
                "robyte_out_of_area_overlay",
                (gui, guiGraphics, partialTick, width, height) -> {
                    RobyteOutOfAreaOverlay.render(guiGraphics, partialTick);
                }
        );
        event.registerAboveAll(
                "driveshift_tint_overlay",
                (gui, guiGraphics, partialTick, width, height) -> {
                    DriveshiftTintOverlay.render(guiGraphics, partialTick, width, height);
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

    public static final KeyMapping SHOOT_LASER_BIT = new KeyMapping(
            "key.furyborn.shoot_laser_bit",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.furyborn"
    );

    public static final KeyMapping TOGGLE_AFTERIMAGE = new KeyMapping(
            "key.furyborn.toggle_afterimage",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_T,
            "key.categories.furyborn"
    );

    public static final KeyMapping DRIVESHIFT_DASH = new KeyMapping(
            "key.furyborn.driveshift_dash",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.categories.furyborn"
    );

    public static final KeyMapping DRIVESHIFT_BACKSTAB = new KeyMapping(
            "key.furyborn.driveshift_backstab",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.furyborn"
    );

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RobyteBitLaserModel.LAYER_LOCATION, RobyteBitLaserModel::createBodyLayer);
        event.registerLayerDefinition(ModelBusterThrower.BUSTER_THROWER_LAYER, ModelBusterThrower::createLayerDefinition);
        event.registerLayerDefinition(SunRaiserDriveModel.LAYER_LOCATION, SunRaiserDriveModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                FuryBornBlockEntities.HALO_PROJECTOR.get(),
                HaloProjectorRenderer::new
        );
        event.registerBlockEntityRenderer(
                FuryBornBlockEntities.EXOLUMEN_CONTROLLER.get(),
                ExolumenControllerRenderer::new);
    }

    @SubscribeEvent
    public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderer<?> renderer = event.getRenderer(MekanismEntityTypes.ROBIT.get());

        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new RobitAfterImageLayer(livingRenderer));
        }
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_AFTERIMAGE);
        event.register(TOGGLE_BUSTER_MODE);
        event.register(SHOOT_LASER_BIT);
        event.register(DRIVESHIFT_DASH);
        event.register(DRIVESHIFT_BACKSTAB);
    }
}