package XiGyoku.furyborn.client.event;

import XiGyoku.furyborn.Config;
import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.item.FuryBornItems;
import XiGyoku.furyborn.client.util.ColorUtil;
import XiGyoku.furyborn.client.util.GeometryHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FuryBornEventBusClientEvents {
    private enum TooltipMode {
        NORMAL,
        FURYBORN,
        STARRY
    }

    private static volatile Set<Item> cachedFuryBornItems = null;

    private static final List<RegistryObject<Item>> FURYBORN_REGISTRY_OBJECTS = List.of(
            FuryBornItems.HALO_OF_EXOLUMEN
    );

    private static final int ANIMATION_FRAMES = 21;
    private static final int FRAME_DURATION = 10;

    private static final Vector4f COLOR_ORBIT = new Vector4f(1.0F, 1.0F, 1.0F, 0.4F);
    private static final Vector4f[] COLORS_PLANETS = new Vector4f[]{
            new Vector4f(0.2F, 0.8F, 1.0F, 1.0F),
            new Vector4f(0.2F, 1.0F, 0.3F, 1.0F),
            new Vector4f(1.0F, 1.0F, 0.2F, 1.0F),
            new Vector4f(1.0F, 0.5F, 0.0F, 1.0F),
            new Vector4f(0.8F, 0.2F, 1.0F, 1.0F)
    };

    private static final float[] PLANET_RADII = new float[]{ 0.15F, 0.18F, 0.2F, 0.22F, 0.25F };
    private static final float[] ORBIT_SPEEDS = new float[]{ 2.5F, 1.8F, 1.4F, 1.1F, 0.9F };

    private static final String FURYBORN_STARRY_MARKER = ":_FBS_";

    private static final ResourceLocation TOOLTIP_OVERLAY = new ResourceLocation(Furyborn.MODID, "textures/gui/tooltip/tooltip_overlay.png");
    private static final ResourceLocation[] ANIM_FRAMES = new ResourceLocation[ANIMATION_FRAMES];

    static {
        for (int i = 0; i < ANIMATION_FRAMES; i++) {
            ANIM_FRAMES[i] = new ResourceLocation(Furyborn.MODID, "textures/effect/operation/operation_effect_" + (i + 1) + ".png");
        }
    }

    private static Set<Item> buildCache() {
        Set<Item> set = new HashSet<>();
        for (RegistryObject<Item> ro : FURYBORN_REGISTRY_OBJECTS) {
            if (ro.isPresent()) set.add(ro.get());
        }
        return Collections.unmodifiableSet(set);
    }

    private static Set<Item> getFuryBornItemsCache() {
        Set<Item> local = cachedFuryBornItems;
        if (local == null) {
            synchronized (FuryBornEventBusClientEvents.class) {
                local = cachedFuryBornItems;
                if (local == null) {
                    local = buildCache();
                    cachedFuryBornItems = local;
                }
            }
        }
        return local;
    }

    private static TooltipMode getTooltipMode(ItemStack stack) {
        if (stack.isEmpty()) return TooltipMode.NORMAL;

        String displayName = stack.getHoverName().getString();
        if (displayName.contains(FURYBORN_STARRY_MARKER)) return TooltipMode.STARRY;

        if (getFuryBornItemsCache().contains(stack.getItem())) return TooltipMode.FURYBORN;

        return TooltipMode.NORMAL;
    }

    private static float getOrbitRadius(int index) {
        if (index == 0) return 1.0F;
        return 1.0F + (index * Config.TOOLTIP_HALO_ORBIT_SPACING.get().floatValue());
    }

    private static float getPlanetRadius(int index) {
        if (index < PLANET_RADII.length) return PLANET_RADII[index];
        return PLANET_RADII[PLANET_RADII.length - 1] + (index - PLANET_RADII.length + 1) * 0.02F;
    }

    private static float getOrbitSpeed(int index) {
        float speed;
        if (index < ORBIT_SPEEDS.length) {
            speed = ORBIT_SPEEDS[index];
        } else {
            speed = (float) (ORBIT_SPEEDS[ORBIT_SPEEDS.length - 1] * Math.pow(0.8, index - ORBIT_SPEEDS.length + 1));
        }
        return speed * Config.TOOLTIP_HALO_ROTATION_SPEED_MULTIPLIER.get().floatValue();
    }

    private static Vector4f getUnifiedColor() {
        String hex = Config.TOOLTIP_HALO_UNIFIED_PLANET_COLOR.get();
        try {
            if (hex.startsWith("#")) hex = hex.substring(1);
            int color = (int) Long.parseLong(hex, 16);
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            return new Vector4f(r, g, b, 1.0f);
        } catch (Exception e) {
            return new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private static Vector4f getPlanetColor(int index) {
        if (Config.TOOLTIP_HALO_UNIFY_PLANET_COLOR.get()) {
            return getUnifiedColor();
        }
        return COLORS_PLANETS[index % COLORS_PLANETS.length];
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
        ItemStack stack = event.getItemStack();
        TooltipMode mode = getTooltipMode(stack);
        if (mode == TooltipMode.NORMAL) return;

        event.setCanceled(true);

        renderCustomTooltip(
                event.getGraphics(),
                event.getFont(),
                event.getX(),
                event.getY(),
                event.getScreenWidth(),
                event.getScreenHeight(),
                event.getComponents(),
                stack,
                mode
        );
    }

    public static void renderHaloBackground(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, long timeMs) {
        float ticks = (timeMs % 1000000L) / 50.0F;
        int packedLight = 15728880;
        float haloGuiScale = 120.0F * Config.TOOLTIP_HALO_SCALE.get().floatValue();

        poseStack.pushPose();

        boolean individualRotation = Config.TOOLTIP_HALO_INDIVIDUAL_ROTATION.get();
        int orbitCount = Config.TOOLTIP_HALO_ORBIT_COUNT.get();

        if (!individualRotation) {
            float angleX = (timeMs % 12000L) / 12000.0F * 360.0F;
            float angleY = (timeMs % 16000L) / 16000.0F * 360.0F;
            float angleZ = (timeMs % 20000L) / 20000.0F * 360.0F;

            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angleX));
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angleY));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angleZ));
        }

        poseStack.scale(haloGuiScale, haloGuiScale, haloGuiScale);

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());

        for (int i = 0; i < orbitCount; i++) {
            poseStack.pushPose();

            if (individualRotation) {
                double speedMod = 1.0 + (i * 0.15);
                double scaledTime = timeMs * speedMod;

                float angleX = (float) ((scaledTime % 12000.0) / 12000.0 * 360.0);
                float angleY = (float) ((scaledTime % 16000.0) / 16000.0 * 360.0);
                float angleZ = (float) ((scaledTime % 20000.0) / 20000.0 * 360.0);

                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angleX));
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angleY));
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angleZ));
            }

            float radius = getOrbitRadius(i);
            GeometryHelper.drawRing(poseStack, consumer, radius, 0.02F, COLOR_ORBIT, packedLight);

            float speed = getOrbitSpeed(i);
            float basePlanetSize = getPlanetRadius(i);
            Vector4f basePlanetColor = getPlanetColor(i);

            int trailSteps = 20;
            float trailLengthTicks = 15.0F;

            Matrix4f mat = poseStack.last().pose();
            Matrix3f nor = poseStack.last().normal();

            for (int t = 0; t < trailSteps; t++) {
                float ratio1 = 1.0F - ((float) t / trailSteps);
                float ratio2 = 1.0F - ((float) (t + 1) / trailSteps);

                float time1 = ticks - (t * (trailLengthTicks / trailSteps));
                float time2 = ticks - ((t + 1) * (trailLengthTicks / trailSteps));

                float angle1 = (time1 * 0.1F * speed) % (Mth.PI * 2.0F);
                float angle2 = (time2 * 0.1F * speed) % (Mth.PI * 2.0F);

                float w1 = basePlanetSize * ratio1;
                float w2 = basePlanetSize * ratio2;

                float cos1 = Mth.cos(angle1);
                float sin1 = Mth.sin(angle1);
                float cos2 = Mth.cos(angle2);
                float sin2 = Mth.sin(angle2);

                Vector4f c1 = new Vector4f(basePlanetColor.x() * ratio1, basePlanetColor.y() * ratio1, basePlanetColor.z() * ratio1, basePlanetColor.w() * ratio1);
                Vector4f c2 = new Vector4f(basePlanetColor.x() * ratio2, basePlanetColor.y() * ratio2, basePlanetColor.z() * ratio2, basePlanetColor.w() * ratio2);

                consumer.vertex(mat, cos1 * (radius - w1), sin1 * (radius - w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();
                consumer.vertex(mat, cos1 * (radius + w1), sin1 * (radius + w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();
                consumer.vertex(mat, cos2 * (radius + w2), sin2 * (radius + w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();
                consumer.vertex(mat, cos2 * (radius - w2), sin2 * (radius - w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();

                consumer.vertex(mat, cos2 * (radius - w2), sin2 * (radius - w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, -1).endVertex();
                consumer.vertex(mat, cos2 * (radius + w2), sin2 * (radius + w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, -1).endVertex();
                consumer.vertex(mat, cos1 * (radius + w1), sin1 * (radius + w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, -1).endVertex();
                consumer.vertex(mat, cos1 * (radius - w1), sin1 * (radius - w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, -1).endVertex();
            }

            poseStack.pushPose();
            float currentAngle = (ticks * 0.1F * speed) % (Mth.PI * 2.0F);
            poseStack.translate(Mth.cos(currentAngle) * radius, Mth.sin(currentAngle) * radius, 0.0F);
            GeometryHelper.drawSphere(poseStack, consumer, basePlanetSize, basePlanetColor, packedLight);
            poseStack.popPose();

            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();

        poseStack.popPose();
    }

    private static void renderCustomTooltip(
            GuiGraphics guiGraphics,
            Font font,
            int x,
            int y,
            int screenWidth,
            int screenHeight,
            List<ClientTooltipComponent> components,
            ItemStack stack,
            TooltipMode mode
    ) {
        int tooltipWidth = 0;
        int tooltipHeight = components.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent comp : components) {
            int width = comp.getWidth(font);
            if (width > tooltipWidth) tooltipWidth = width;
            tooltipHeight += comp.getHeight();
        }
        int bgWidth = tooltipWidth + 6;
        int bgHeight = tooltipHeight + 10;

        int bgX = x + 10;
        int bgY = y - (bgHeight / 2);

        if (bgX + bgWidth > screenWidth) bgX = x - bgWidth - 10;
        if (bgY + bgHeight > screenHeight) bgY = screenHeight - bgHeight;
        if (bgY < 0) bgY = 0;

        int centerX = bgX + (bgWidth / 2);
        int centerY = bgY + (bgHeight / 2);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 400);

        long time = System.currentTimeMillis();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        int bgColor = 0xF0100010;
        int borderColorTop = ColorUtil.getPulsingColor(time, 0x8000FF00, 0xA055FF55);
        int borderColorBottom = ColorUtil.getPulsingColor(time, 0x8000A000, 0xA033CC33);

        float overlayScale = 0.375F;
        int overlayActualWidth = (int) (64 * overlayScale);
        int overlayX = bgX + (bgWidth / 2) - (overlayActualWidth / 2);

        guiGraphics.fillGradient(bgX, bgY, bgX + bgWidth, bgY + bgHeight, bgColor, bgColor);
        guiGraphics.fillGradient(bgX, bgY, overlayX, bgY + 1, borderColorTop, borderColorTop);
        guiGraphics.fillGradient(overlayX + overlayActualWidth, bgY, bgX + bgWidth, bgY + 1, borderColorTop, borderColorTop);
        guiGraphics.fillGradient(bgX, bgY + bgHeight - 1, bgX + bgWidth, bgY + bgHeight, borderColorBottom, borderColorBottom);
        guiGraphics.fillGradient(bgX, bgY, bgX + 1, bgY + bgHeight, borderColorTop, borderColorBottom);
        guiGraphics.fillGradient(bgX + bgWidth - 1, bgY, bgX + bgWidth, bgY + bgHeight, borderColorTop, borderColorBottom);

        ColorUtil.drawEnergeticLine(guiGraphics, bgX, bgY, bgWidth, bgHeight, time);

        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 0);
        renderHaloBackground(poseStack, buffer, time);
        buffer.endBatch(RenderType.lightning());
        poseStack.popPose();

        if (mode == TooltipMode.FURYBORN) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 100);

            int frameIndex = (int) (((time % 100000L) / FRAME_DURATION) % ANIMATION_FRAMES);
            ResourceLocation animTexture = ANIM_FRAMES[frameIndex];

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.2F);
            guiGraphics.blit(animTexture, bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            ResourceLocation overlayTexture = TOOLTIP_OVERLAY;
            float overlayScale = 0.375F;
            int overlayX = bgX + (bgWidth / 2) - (int)((64 * overlayScale) / 2);
            int overlayY = (int) (bgY - (11 * overlayScale)) + 2;

            poseStack.pushPose();
            poseStack.translate(overlayX, overlayY, 0);
            poseStack.scale(overlayScale, overlayScale, 1.0F);
            guiGraphics.blit(overlayTexture, 0, 0, 0, 0, 64, 30, 64, 64);
            poseStack.popPose();

            RenderSystem.disableBlend();
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(0, 0, 250);
        int tooltipLeftOffset = 5;
        int tooltipTopOffset = 3;
        int offsetY = bgY + tooltipTopOffset;

        for (ClientTooltipComponent comp : components) {
            if (comp instanceof ClientTextTooltip textTooltip) {
                textTooltip.renderText(font, bgX + tooltipLeftOffset, offsetY, poseStack.last().pose(), buffer);
            }
            offsetY += comp.getHeight();
        }
        buffer.endBatch();

        offsetY = bgY + tooltipTopOffset;
        for (ClientTooltipComponent comp : components) {
            if (!(comp instanceof ClientTextTooltip)) {
                comp.renderImage(font, bgX + tooltipLeftOffset - 3, offsetY, guiGraphics);
            }
            offsetY += comp.getHeight();
        }
        poseStack.popPose();

        poseStack.popPose();
    }
}