package XiGyoku.furyborn.client.util;

import XiGyoku.furyborn.blockentity.HaloProjectorBlockEntity;
import XiGyoku.furyborn.client.util.GeometryHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class HaloProjectorRenderer implements BlockEntityRenderer<HaloProjectorBlockEntity> {

    private static final Vector4f COLOR_SUN = new Vector4f(1.0F, 0.2F, 0.0F, 1.0F);
    private static final Vector4f COLOR_ORBIT = new Vector4f(1.0F, 1.0F, 1.0F, 0.4F);
    private static final Vector4f COLOR_HAZE = new Vector4f(0.1F, 0.0F, 0.2F, 0.5F);
    private static final Vector4f[] COLORS_PLANETS = new Vector4f[]{
            new Vector4f(0.2F, 0.8F, 1.0F, 1.0F),
            new Vector4f(0.2F, 1.0F, 0.3F, 1.0F),
            new Vector4f(1.0F, 1.0F, 0.2F, 1.0F),
            new Vector4f(1.0F, 0.5F, 0.0F, 1.0F),
            new Vector4f(0.8F, 0.2F, 1.0F, 1.0F)
    };
    private static final float[] PLANET_RADII = new float[]{ 0.15F, 0.18F, 0.2F, 0.22F, 0.25F };
    private static final float[] ORBIT_SPEEDS = new float[]{ 2.5F, 1.8F, 1.4F, 1.1F, 0.9F };

    public HaloProjectorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HaloProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float target = blockEntity.isActive() ? 1.0f : 0.0f;

        if (blockEntity.currentRenderScale < 0) {
            blockEntity.currentRenderScale = target;
        } else {
            float animationSpeed = 0.05f;
            if (blockEntity.currentRenderScale < target) {
                blockEntity.currentRenderScale = Math.min(target, blockEntity.currentRenderScale + animationSpeed);
            } else if (blockEntity.currentRenderScale > target) {
                blockEntity.currentRenderScale = Math.max(target, blockEntity.currentRenderScale - animationSpeed);
            }
        }

        if (blockEntity.currentRenderScale <= 0.001f) return;

        poseStack.pushPose();
        RenderSystem.disableCull();

        poseStack.translate(0.5 + blockEntity.offsetX, 0.5 + blockEntity.offsetY, 0.5 + blockEntity.offsetZ);
        float animatedScale = blockEntity.scale * blockEntity.currentRenderScale;
        poseStack.scale(animatedScale, animatedScale, animatedScale);

        poseStack.mulPose(Axis.ZP.rotationDegrees(blockEntity.rotZ));
        poseStack.mulPose(Axis.YP.rotationDegrees(blockEntity.rotY));
        poseStack.mulPose(Axis.XP.rotationDegrees(blockEntity.rotX));

        float ageInTicks = (float)blockEntity.getLevel().getGameTime() + partialTick;

        renderHalo(poseStack, bufferSource, packedLight,
                blockEntity.sunScale, blockEntity.hazeScale, blockEntity.orbitCount, blockEntity.orbitSpacing,
                blockEntity.rotationSpeedMultiplier, blockEntity.individualRotation, blockEntity.unifyPlanetColor,
                blockEntity.unifiedPlanetColor, ageInTicks);

        RenderSystem.enableCull();
        poseStack.popPose();
    }

    public static void renderHalo(PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light,
                                  float sunScale, float hazeScale, int orbitCount, float orbitSpacing,
                                  float rotationSpeedMultiplier, boolean individualRotation, boolean unifyPlanetColor,
                                  int unifiedPlanetColor, float ageInTicks) {

        poseStack.pushPose();
        poseStack.translate(0, 0, -0.01F);
        VertexConsumer hazeConsumer = renderTypeBuffer.getBuffer(RenderType.lightning());
        GeometryHelper.drawSolidCircle(poseStack, hazeConsumer, hazeScale, COLOR_HAZE, light);
        poseStack.popPose();

        VertexConsumer sunConsumer = renderTypeBuffer.getBuffer(RenderType.lightning());
        GeometryHelper.drawNoiseSphere(poseStack, sunConsumer, sunScale, COLOR_SUN, 0.8F, light);

        VertexConsumer lightningConsumer = renderTypeBuffer.getBuffer(RenderType.lightning());

        float rUni = ((unifiedPlanetColor >> 16) & 0xFF) / 255.0f;
        float gUni = ((unifiedPlanetColor >> 8) & 0xFF) / 255.0f;
        float bUni = (unifiedPlanetColor & 0xFF) / 255.0f;
        Vector4f unifiedColorVec = new Vector4f(rUni, gUni, bUni, 1.0f);

        for (int i = 0; i < orbitCount; i++) {
            poseStack.pushPose();

            if (individualRotation) {
                double speedMod = 1.0 + (i * 0.15);
                double scaledTicks = ageInTicks * speedMod;
                poseStack.mulPose(Axis.XP.rotationDegrees((float) (scaledTicks * 1.5 % 360)));
                poseStack.mulPose(Axis.YP.rotationDegrees((float) (scaledTicks * 1.1 % 360)));
                poseStack.mulPose(Axis.ZP.rotationDegrees((float) (scaledTicks * 0.9 % 360)));
            }

            float radius = i == 0 ? 1.0F : 1.0F + (i * orbitSpacing);
            GeometryHelper.drawRing(poseStack, lightningConsumer, radius, 0.02F, COLOR_ORBIT, light);

            float speed = i < ORBIT_SPEEDS.length ? ORBIT_SPEEDS[i] : (float) (ORBIT_SPEEDS[ORBIT_SPEEDS.length - 1] * Math.pow(0.8, i - ORBIT_SPEEDS.length + 1));
            speed *= rotationSpeedMultiplier;

            float basePlanetSize = i < PLANET_RADII.length ? PLANET_RADII[i] : PLANET_RADII[PLANET_RADII.length - 1] + (i - PLANET_RADII.length + 1) * 0.02F;
            Vector4f basePlanetColor = unifyPlanetColor ? unifiedColorVec : COLORS_PLANETS[i % COLORS_PLANETS.length];

            Matrix4f mat = poseStack.last().pose();
            Matrix3f nor = poseStack.last().normal();

            for (int t = 0; t < 20; t++) {
                float ratio1 = 1.0F - ((float) t / 20);
                float ratio2 = 1.0F - ((float) (t + 1) / 20);
                float time1 = ageInTicks - (t * 0.75f);
                float time2 = ageInTicks - ((t + 1) * 0.75f);
                float angle1 = (time1 * 0.1F * speed);
                float angle2 = (time2 * 0.1F * speed);
                float w1 = basePlanetSize * ratio1;
                float w2 = basePlanetSize * ratio2;
                float cos1 = (float)Math.cos(angle1);
                float sin1 = (float)Math.sin(angle1);
                float cos2 = (float)Math.cos(angle2);
                float sin2 = (float)Math.sin(angle2);

                Vector4f c1 = new Vector4f(basePlanetColor.x() * ratio1, basePlanetColor.y() * ratio1, basePlanetColor.z() * ratio1, basePlanetColor.w() * ratio1);
                Vector4f c2 = new Vector4f(basePlanetColor.x() * ratio2, basePlanetColor.y() * ratio2, basePlanetColor.z() * ratio2, basePlanetColor.w() * ratio2);

                lightningConsumer.vertex(mat, cos1 * (radius - w1), sin1 * (radius - w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(0, 0).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, 1).endVertex();
                lightningConsumer.vertex(mat, cos1 * (radius + w1), sin1 * (radius + w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(1, 1).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, 1).endVertex();
                lightningConsumer.vertex(mat, cos2 * (radius + w2), sin2 * (radius + w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(1, 1).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, 1).endVertex();
                lightningConsumer.vertex(mat, cos2 * (radius - w2), sin2 * (radius - w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(0, 0).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, 1).endVertex();

                lightningConsumer.vertex(mat, cos2 * (radius - w2), sin2 * (radius - w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(0, 0).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, -1).endVertex();
                lightningConsumer.vertex(mat, cos2 * (radius + w2), sin2 * (radius + w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(1, 1).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, -1).endVertex();
                lightningConsumer.vertex(mat, cos1 * (radius + w1), sin1 * (radius + w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(1, 1).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, -1).endVertex();
                lightningConsumer.vertex(mat, cos1 * (radius - w1), sin1 * (radius - w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(0, 0).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, -1).endVertex();
            }

            poseStack.pushPose();
            float currentAngle = (ageInTicks * 0.1F * speed);
            poseStack.translate((float)Math.cos(currentAngle) * radius, (float)Math.sin(currentAngle) * radius, 0.0F);
            GeometryHelper.drawSphere(poseStack, lightningConsumer, basePlanetSize, basePlanetColor, light);
            poseStack.popPose();

            poseStack.popPose();
        }
    }
}