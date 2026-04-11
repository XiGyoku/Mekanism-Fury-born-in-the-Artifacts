package XiGyoku.furyborn.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class ColorUtil {

    public static int getPulsingColor(long time, int baseColor, int brightColor) {
        float sine = (Mth.sin((float) (time % 2000L) / 2000.0F * Mth.TWO_PI) + 1.0F) / 2.0F;

        int a1 = (baseColor >> 24) & 0xFF;
        int r1 = (baseColor >> 16) & 0xFF;
        int g1 = (baseColor >> 8) & 0xFF;
        int b1 = baseColor & 0xFF;

        int a2 = (brightColor >> 24) & 0xFF;
        int r2 = (brightColor >> 16) & 0xFF;
        int g2 = (brightColor >> 8) & 0xFF;
        int b2 = brightColor & 0xFF;

        int a = (int) (a1 + (a2 - a1) * sine);
        int r = (int) (r1 + (r2 - r1) * sine);
        int g = (int) (g1 + (g2 - g1) * sine);
        int b = (int) (b1 + (b2 - b1) * sine);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void drawEnergeticLine(GuiGraphics guiGraphics, int bgX, int bgY, int bgWidth, int bgHeight, long time) {
        int perimeter = bgWidth * 2 + bgHeight * 2;
        float speed = 0.15f;
        float currentPos = ((time % 100000L) * speed) % perimeter;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        int trailLength = 200;
        for (int i = 5; i < trailLength; i++) {
            float tailPos = currentPos - i;
            while (tailPos < 0) tailPos += perimeter;

            float ratio = 1.0f - ((float) i / trailLength);
            int alpha = (int) (255 * ratio * ratio);
            if (alpha <= 0) continue;

            int color = (alpha << 24) | 0xFFFFFF;
            drawGlowPoint(guiGraphics, bgX, bgY, bgWidth, bgHeight, tailPos, color, 0);
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static void drawGlowPoint(GuiGraphics guiGraphics, int bgX, int bgY, int bgWidth, int bgHeight, float pos, int color, int expand) {
        int x, y;
        float halfWidth = bgWidth / 2.0f;

        if (pos < halfWidth) {
            x = (int) (bgX + halfWidth - pos);
            y = bgY;
        } else if (pos < halfWidth + bgHeight) {
            x = bgX;
            y = (int) (bgY + (pos - halfWidth));
        } else if (pos < halfWidth + bgHeight + bgWidth) {
            x = (int) (bgX + (pos - (halfWidth + bgHeight)));
            y = bgY + bgHeight;
        } else if (pos < halfWidth + 2 * bgHeight + bgWidth) {
            x = bgX + bgWidth;
            y = (int) (bgY + bgHeight - (pos - (halfWidth + bgHeight + bgWidth)));
        } else {
            x = (int) (bgX + bgWidth - (pos - (halfWidth + 2 * bgHeight + bgWidth)));
            y = bgY;
        }

        guiGraphics.fill(x - expand, y - expand, x + expand + 1, y + expand + 1, color);
    }
}