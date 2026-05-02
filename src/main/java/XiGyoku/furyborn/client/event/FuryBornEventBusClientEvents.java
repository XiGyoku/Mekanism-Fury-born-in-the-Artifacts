package XiGyoku.furyborn.client.event;

import XiGyoku.furyborn.Config;
import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.blockentity.ExolumenControllerBlockEntity;
import XiGyoku.furyborn.blockentity.PortalAnimationState;
import XiGyoku.furyborn.client.entity.DriveshiftParticleRenderer;
import XiGyoku.furyborn.client.util.TargetMarkManager;
import XiGyoku.furyborn.entity.RoadBikeBitEntity;
import XiGyoku.furyborn.item.FuryBornItems;
import XiGyoku.furyborn.client.util.ColorUtil;
import XiGyoku.furyborn.client.util.GeometryHelper;
import XiGyoku.furyborn.client.util.PhotonRenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.common.entity.EntityRobit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
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

    private static ItemStack lastCapturedItem = ItemStack.EMPTY;
    private static TextColor lastCapturedTooltipColor = null;

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

    private static Vector4f getPlanetColor(int index, boolean hasCustomRarity, int customColorHex) {
        if (hasCustomRarity) {
            float r = ((customColorHex >> 16) & 0xFF) / 255.0f;
            float g = ((customColorHex >> 8) & 0xFF) / 255.0f;
            float b = (customColorHex & 0xFF) / 255.0f;
            return new Vector4f(r, g, b, 1.0f);
        }
        if (Config.TOOLTIP_HALO_UNIFY_PLANET_COLOR.get()) {
            return getUnifiedColor();
        }
        return COLORS_PLANETS[index % COLORS_PLANETS.length];
    }

    private static TextColor extractDynamicColor(Component component) {
        if (component == null) return null;

        if (component.getStyle() != null && component.getStyle().getColor() != null) {
            TextColor color = component.getStyle().getColor();
            if (!color.getClass().equals(TextColor.class)) {
                return color;
            }
        }

        if (component.getContents() instanceof TranslatableContents translatable) {
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Component argComp) {
                    TextColor color = extractDynamicColor(argComp);
                    if (color != null) return color;
                }
            }
        }

        for (Component sibling : component.getSiblings()) {
            TextColor color = extractDynamicColor(sibling);
            if (color != null) return color;
        }
        return null;
    }

    private static TextColor extractFirstColor(Component component) {
        if (component == null) return null;

        if (component.getStyle() != null && component.getStyle().getColor() != null) {
            return component.getStyle().getColor();
        }

        if (component.getContents() instanceof TranslatableContents translatable) {
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Component argComp) {
                    TextColor color = extractFirstColor(argComp);
                    if (color != null) return color;
                }
            }
        }

        for (Component sibling : component.getSiblings()) {
            TextColor color = extractFirstColor(sibling);
            if (color != null) return color;
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCaptureTooltipColor(ItemTooltipEvent event) {
        List<Component> tooltip = event.getToolTip();
        lastCapturedItem = event.getItemStack();

        if (!tooltip.isEmpty()) {
            Component firstLine = tooltip.get(0);
            TextColor dynamic = extractDynamicColor(firstLine);
            if (dynamic != null) {
                lastCapturedTooltipColor = dynamic;
            } else {
                lastCapturedTooltipColor = extractFirstColor(firstLine);
            }
        } else {
            lastCapturedTooltipColor = null;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        net.minecraft.world.item.Rarity rarity = stack.getRarity();

        if (XiGyoku.furyborn.item.FuryBornRarities.hasCustomColor(rarity)) {
            List<Component> tooltip = event.getToolTip();
            if (!tooltip.isEmpty()) {
                int hexColor = XiGyoku.furyborn.item.FuryBornRarities.getColor(rarity);
                Component originalName = tooltip.get(0);
                tooltip.set(0, originalName.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(hexColor))));
            }
        }
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

    public static void renderHaloBackground(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, long timeMs, boolean hasCustomRarity, int rarityColorHex) {
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
            Vector4f basePlanetColor = getPlanetColor(i, hasCustomRarity, rarityColorHex);

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
        ResourceLocation overlayTexture = TOOLTIP_OVERLAY;
        int tooltipWidth = 0;
        int tooltipHeight = components.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent comp : components) {
            int width = comp.getWidth(font);
            if (width > tooltipWidth) tooltipWidth = width;
            tooltipHeight += comp.getHeight();
        }
        int bgWidth = tooltipWidth + 10;
        int bgHeight = tooltipHeight + 10;
        int bgX = x + 10;
        int bgY = y - (bgHeight / 2);
        if (bgX + bgWidth > screenWidth) bgX = x - bgWidth - 10;
        if (bgY + bgHeight > screenHeight) bgY = screenHeight - bgHeight;
        if (bgY < 0) bgY = 0;
        int centerX = bgX + (bgWidth / 2);
        int centerY = bgY + (bgHeight / 2);
        int bgColor = 0xF0100010;
        long time = System.currentTimeMillis();

        int borderColorTop;
        int borderColorBottom;
        int energeticLineColor;
        boolean hasCustomRarity = false;
        int rarityColorHex = 0xFFFFFF;

        if (mode == TooltipMode.FURYBORN) {
            borderColorTop = ColorUtil.getPulsingColor(time, 0x8000FF00, 0xA055FF55);
            borderColorBottom = ColorUtil.getPulsingColor(time, 0x8000A000, 0xA033CC33);
            energeticLineColor = 0x55FF55;
        } else {
            hasCustomRarity = Config.TOOLTIP_FOLLOW_RARITY_COLOR.get();

            if (hasCustomRarity) {
                if (ItemStack.isSameItemSameTags(stack, lastCapturedItem) && lastCapturedTooltipColor != null) {
                    rarityColorHex = lastCapturedTooltipColor.getValue();
                } else if (XiGyoku.furyborn.item.FuryBornRarities.hasCustomColor(stack.getRarity())) {
                    rarityColorHex = XiGyoku.furyborn.item.FuryBornRarities.getColor(stack.getRarity());
                } else if (stack.getRarity().color != null && stack.getRarity().color.getColor() != null) {
                    rarityColorHex = stack.getRarity().color.getColor();
                } else {
                    hasCustomRarity = false;
                }
            }

            if (!hasCustomRarity && XiGyoku.furyborn.item.FuryBornRarities.hasCustomColor(stack.getRarity())) {
                rarityColorHex = XiGyoku.furyborn.item.FuryBornRarities.getColor(stack.getRarity());
                hasCustomRarity = true;
            }

            int r = (rarityColorHex >> 16) & 0xFF;
            int g = (rarityColorHex >> 8) & 0xFF;
            int b = rarityColorHex & 0xFF;

            int brightR = Math.min(255, r + 60);
            int brightG = Math.min(255, g + 60);
            int brightB = Math.min(255, b + 60);

            energeticLineColor = (brightR << 16) | (brightG << 8) | brightB;

            int colorTopBright = (0xA0 << 24) | (r << 16) | (g << 8) | b;
            int colorTopDark = (0x80 << 24) | ((r / 2) << 16) | ((g / 2) << 8) | (b / 2);

            int colorBottomBright = (0xA0 << 24) | ((r * 4 / 5) << 16) | ((g * 4 / 5) << 8) | (b * 4 / 5);
            int colorBottomDark = (0x80 << 24) | ((r * 2 / 5) << 16) | ((g * 2 / 5) << 8) | (b * 2 / 5);

            borderColorTop = ColorUtil.getPulsingColor(time, colorTopDark, colorTopBright);
            borderColorBottom = ColorUtil.getPulsingColor(time, colorBottomDark, colorBottomBright);
        }

        float overlayScale = 0.375F;
        int overlayActualWidth = (int) (64 * overlayScale);
        int overlayX = bgX + (bgWidth / 2) - (overlayActualWidth / 2);
        int splitX = 3;
        int splitY = 3;
        int tileWidth = bgWidth / splitX;
        int tileHeight = bgHeight / splitY;
        int overlayY = (int) (bgY - (11 * overlayScale)) + 2;
        int tooltipLeftOffset = 5;
        int tooltipTopOffset = 3;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 400);
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        guiGraphics.fillGradient(bgX, bgY, bgX + bgWidth, bgY + bgHeight, bgColor, bgColor);

        if (mode == TooltipMode.FURYBORN) {
            guiGraphics.fillGradient(bgX, bgY, overlayX, bgY + 1, borderColorTop, borderColorTop);
            guiGraphics.fillGradient(overlayX + overlayActualWidth, bgY, bgX + bgWidth, bgY + 1, borderColorTop, borderColorTop);
        } else {
            guiGraphics.fillGradient(bgX, bgY, bgX + bgWidth, bgY + 1, borderColorTop, borderColorTop);
        }

        guiGraphics.fillGradient(bgX, bgY + bgHeight - 1, bgX + bgWidth, bgY + bgHeight, borderColorBottom, borderColorBottom);
        guiGraphics.fillGradient(bgX, bgY, bgX + 1, bgY + bgHeight, borderColorTop, borderColorBottom);
        guiGraphics.fillGradient(bgX + bgWidth - 1, bgY, bgX + bgWidth, bgY + bgHeight, borderColorTop, borderColorBottom);

        ColorUtil.drawEnergeticLine(guiGraphics, bgX, bgY, bgWidth-1, bgHeight-1, time, energeticLineColor);

        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 0);
        renderHaloBackground(poseStack, buffer, time, hasCustomRarity, rarityColorHex);
        buffer.endBatch(RenderType.lightning());
        poseStack.popPose();

        if (mode == TooltipMode.FURYBORN) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 100);

            int frameIndex = (int) (((time % 100000L) / FRAME_DURATION) % ANIMATION_FRAMES);
            ResourceLocation animTexture = ANIM_FRAMES[frameIndex];

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.2F);

            for (int row = 0; row < splitY; row++) {
                for (int col = 0; col < splitX; col++) {
                    guiGraphics.blit(animTexture, bgX + tileWidth * col, bgY + tileHeight * row, 0, 0, tileWidth, tileHeight, tileWidth, tileHeight);
                }
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !Minecraft.getInstance().isPaused()) {
            DriveshiftParticleRenderer.tick();
            TargetMarkManager.tick();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            Vec3 cameraPos = event.getCamera().getPosition();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            TargetMarkManager.render(poseStack, event.getPartialTick());

            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            DriveshiftParticleRenderer.render(poseStack, bufferSource);
            bufferSource.endBatch(PhotonRenderUtil.GLOWING_PHOTON);

            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public static void onLivingRenderPre(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity() instanceof EntityRobit robit) {
            ExolumenControllerBlockEntity controller = ExolumenControllerBlockEntity.FUSING_ROBITS.get(robit.getUUID());
            if (controller != null && controller.getCurrentState() == PortalAnimationState.FUSING) {
                int tick = controller.getAnimationTick();
                float partialTick = event.getPartialTick();
                float progress = Math.min(1.0f, (tick + partialTick) / 40.0f);

                float scale = Math.max(0.001f, 1.0f - (progress * progress * progress));
                float spin = progress * progress * 1080.0f;

                PoseStack pose = event.getPoseStack();
                pose.pushPose();

                float centerY = 0.75f;
                pose.translate(0.0D, centerY, 0.0D);
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spin));
                pose.scale(scale, scale, scale);
                pose.translate(0.0D, -centerY, 0.0D);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingRenderPost(RenderLivingEvent.Post<?, ?> event) {
        if (event.getEntity() instanceof EntityRobit robit) {
            ExolumenControllerBlockEntity controller = ExolumenControllerBlockEntity.FUSING_ROBITS.get(robit.getUUID());
            if (controller != null && controller.getCurrentState() == PortalAnimationState.FUSING) {
                PoseStack pose = event.getPoseStack();
                pose.popPose();

                int tick = controller.getAnimationTick();
                float partialTick = event.getPartialTick();
                float progress = Math.min(1.0f, (tick + partialTick) / 40.0f);
                float baseAlpha = Math.max(0.0f, 1.0f - progress);
                float gb = Math.max(0.1f, 1.0f - (progress * 2.0f));

                LivingEntityRenderer renderer = (LivingEntityRenderer) event.getRenderer();
                EntityModel model = renderer.getModel();
                ResourceLocation texture = renderer.getTextureLocation(robit);
                RenderType type = RenderType.entityTranslucent(texture);
                VertexConsumer buffer = event.getMultiBufferSource().getBuffer(type);

                float centerY = 0.75f;

                for (int i = 1; i <= 3; i++) {
                    float delayProgress = Math.max(0.0f, (tick + partialTick - i * 2) / 40.0f);
                    if (delayProgress > 0) {
                        float dScale = Math.max(0.001f, 1.0f - (delayProgress * delayProgress * delayProgress));
                        float dSpin = delayProgress * delayProgress * 1080.0f;
                        float dAlpha = baseAlpha * (0.4f - (i * 0.1f));

                        pose.pushPose();
                        pose.translate(0.0D, centerY, 0.0D);
                        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(dSpin));
                        pose.scale(dScale, dScale, dScale);
                        pose.translate(0.0D, -centerY, 0.0D);
                        model.renderToBuffer(pose, buffer, event.getPackedLight(), net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 1.0f, gb, gb, Math.max(0, dAlpha));
                        pose.popPose();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (player.getVehicle() instanceof RoadBikeBitEntity bike) {
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            float bankAngle = Mth.lerp(event.getPartialTick(), bike.prevBankAngle, bike.currentBankAngle);
            float bodyYaw = Mth.lerp(event.getPartialTick(), player.yBodyRotO, player.yBodyRot);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-bankAngle));
            poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0F - bodyYaw)));
        }
    }

    @SubscribeEvent
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        if (event.getEntity().getVehicle() instanceof RoadBikeBitEntity) {
            event.getPoseStack().popPose();
        }
    }
}