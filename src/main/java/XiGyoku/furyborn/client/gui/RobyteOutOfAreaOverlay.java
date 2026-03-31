package XiGyoku.furyborn.client.gui;

import XiGyoku.furyborn.effect.FuryBornEffects;
import XiGyoku.furyborn.entity.client.RobyteAreaEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class RobyteOutOfAreaOverlay {
    private static final List<ResourceLocation> FRAMES = new ArrayList<>();
    static {
        for (int i = 1; i <= 21; i++) {
            FRAMES.add(ResourceLocation.fromNamespaceAndPath("furyborn", "textures/effect/operation/operation_effect_" + i + ".png"));
        }
    }

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.level == null) return;
        if (!player.hasEffect(FuryBornEffects.MONITORED.get())) return;
        boolean isSafe = false;
        List<RobyteAreaEntity> areas = mc.level.getEntitiesOfClass(
                RobyteAreaEntity.class,
                player.getBoundingBox().inflate(128.0D)
        );
        for (RobyteAreaEntity area : areas) {
            if (area.isPlayerInsideArea(player)) {
                isSafe = true;
                break;
            }
        }
        if (isSafe) return;

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        long gameTime = mc.level.getGameTime();
        int currentFrame = (int) ((gameTime / 2) % 21);
        ResourceLocation texture = FRAMES.get(currentFrame);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.3F);
        guiGraphics.blit(texture, 0, 0, 0, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}