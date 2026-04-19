package XiGyoku.furyborn.client.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import XiGyoku.furyborn.client.util.GeometryHelper;
import XiGyoku.furyborn.entity.RobyteEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class RobyteHaloLayer extends GeoRenderLayer<RobyteEntity> {
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

    private static final float SCALE_GLOBAL = 0.5F;
    private static final float RADIUS_SUN = 0.4F;
    private static final float HAZE_SIZE = 0.50F;
    private static final float[] ORBIT_RADII = new float[]{ 1.0F, 1.5F, 2.0F, 2.5F, 3.0F };
    private static final float[] PLANET_RADII = new float[]{ 0.15F, 0.18F, 0.2F, 0.22F, 0.25F };
    private static final float[] ORBIT_SPEEDS = new float[]{ 2.5F, 1.8F, 1.4F, 1.1F, 0.9F };

    public RobyteHaloLayer(GeoEntityRenderer<RobyteEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, RobyteEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!animatable.isRebellion()) return;

        float deathScale = 1.0F;
        if (animatable.deathTime > 0) {
            float deathProgress = ((float) animatable.deathTime + partialTick) / 140.0F;
            if (deathProgress >= 1.0F) return;
            deathScale = 1.0F - deathProgress;
        }

        poseStack.pushPose();
        RenderSystem.disableCull();
        poseStack.translate(0.0D, 2.5D, 0.0D);
        poseStack.scale(deathScale, deathScale, deathScale);

        renderOriginalHalo(poseStack, animatable, bufferSource, packedLight);
        renderGiantHalo(poseStack, animatable, bufferSource, packedLight);

        RenderSystem.enableCull();
        poseStack.popPose();
    }

    private void renderOriginalHalo(PoseStack poseStack, RobyteEntity animatable, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(SCALE_GLOBAL, SCALE_GLOBAL, SCALE_GLOBAL);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));

        poseStack.pushPose();
        poseStack.translate(0, 0, -0.01F);
        VertexConsumer hazeConsumer = bufferSource.getBuffer(RenderType.lightning());
        GeometryHelper.drawSolidCircle(poseStack, hazeConsumer, HAZE_SIZE, COLOR_HAZE, packedLight);
        poseStack.popPose();

        VertexConsumer orbitConsumer = bufferSource.getBuffer(RenderType.lightning());
        for (float radius : ORBIT_RADII) {
            GeometryHelper.drawRing(poseStack, orbitConsumer, radius, 0.02F, COLOR_ORBIT, packedLight);
        }

        VertexConsumer sunConsumer = bufferSource.getBuffer(RenderType.lightning());
        GeometryHelper.drawNoiseSphere(poseStack, sunConsumer, RADIUS_SUN, COLOR_SUN, 0.8F, packedLight);

        VertexConsumer planetConsumer = bufferSource.getBuffer(RenderType.lightning());
        for (int i = 0; i < 5; i++) {
            float radius = ORBIT_RADII[i];
            float speed = ORBIT_SPEEDS[i];
            float basePlanetSize = PLANET_RADII[i];
            Vector4f basePlanetColor = COLORS_PLANETS[i];

            int trailSteps = 20;
            float trailLengthTicks = 15.0F;

            org.joml.Matrix4f mat = poseStack.last().pose();
            org.joml.Matrix3f nor = poseStack.last().normal();

            for (int t = 0; t < trailSteps; t++) {
                float ratio1 = 1.0F - ((float) t / trailSteps);
                float ratio2 = 1.0F - ((float) (t + 1) / trailSteps);

                float time1 = animatable.tickCount - (t * (trailLengthTicks / trailSteps));
                float time2 = animatable.tickCount - ((t + 1) * (trailLengthTicks / trailSteps));

                float angle1 = (time1 * 0.1F * speed) % (net.minecraft.util.Mth.PI * 2.0F);
                float angle2 = (time2 * 0.1F * speed) % (net.minecraft.util.Mth.PI * 2.0F);

                float w1 = basePlanetSize * ratio1;
                float w2 = basePlanetSize * ratio2;

                float cos1 = net.minecraft.util.Mth.cos(angle1);
                float sin1 = net.minecraft.util.Mth.sin(angle1);
                float cos2 = net.minecraft.util.Mth.cos(angle2);
                float sin2 = net.minecraft.util.Mth.sin(angle2);

                Vector4f c1 = new Vector4f(basePlanetColor.x() * ratio1, basePlanetColor.y() * ratio1, basePlanetColor.z() * ratio1, basePlanetColor.w() * ratio1);
                Vector4f c2 = new Vector4f(basePlanetColor.x() * ratio2, basePlanetColor.y() * ratio2, basePlanetColor.z() * ratio2, basePlanetColor.w() * ratio2);

                planetConsumer.vertex(mat, cos1 * (radius - w1), sin1 * (radius - w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();
                planetConsumer.vertex(mat, cos1 * (radius + w1), sin1 * (radius + w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();
                planetConsumer.vertex(mat, cos2 * (radius + w2), sin2 * (radius + w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();
                planetConsumer.vertex(mat, cos2 * (radius - w2), sin2 * (radius - w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();
            }

            poseStack.pushPose();
            float currentAngle = (animatable.tickCount * 0.1F * speed) % (net.minecraft.util.Mth.PI * 2.0F);
            poseStack.translate(net.minecraft.util.Mth.cos(currentAngle) * radius, net.minecraft.util.Mth.sin(currentAngle) * radius, 0.0F);
            GeometryHelper.drawSphere(poseStack, planetConsumer, basePlanetSize, basePlanetColor, packedLight);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void renderGiantHalo(PoseStack poseStack, RobyteEntity animatable, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        float giantScale = SCALE_GLOBAL * 4.2F;
        poseStack.scale(giantScale, giantScale, giantScale);

        int orbitCount = 3;
        VertexConsumer lightningConsumer = bufferSource.getBuffer(RenderType.lightning());

        for (int i = 0; i < orbitCount; i++) {
            poseStack.pushPose();

            double speedMod = 1.0 + (i * 0.15);
            double scaledTicks = (animatable.tickCount * 0.5) * speedMod;

            float angleX = (float) ((scaledTicks % 240.0) / 240.0 * 360.0);
            float angleY = (float) ((scaledTicks % 320.0) / 320.0 * 360.0);
            float angleZ = (float) ((scaledTicks % 400.0) / 400.0 * 360.0);

            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angleX));
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angleY));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angleZ));

            float radius;
            if (i < ORBIT_RADII.length) {
                radius = ORBIT_RADII[i];
            } else {
                radius = ORBIT_RADII[ORBIT_RADII.length - 1] + (i - ORBIT_RADII.length + 1) * 0.5F;
            }

            GeometryHelper.drawRing(poseStack, lightningConsumer, radius, 0.02F, COLOR_ORBIT, packedLight);

            float speed;
            if (i < ORBIT_SPEEDS.length) {
                speed = ORBIT_SPEEDS[i];
            } else {
                speed = (float) (ORBIT_SPEEDS[ORBIT_SPEEDS.length - 1] * Math.pow(0.8, i - ORBIT_SPEEDS.length + 1));
            }
            speed *= 0.5F;

            float basePlanetSize;
            if (i < PLANET_RADII.length) {
                basePlanetSize = PLANET_RADII[i];
            } else {
                basePlanetSize = PLANET_RADII[PLANET_RADII.length - 1] + (i - PLANET_RADII.length + 1) * 0.02F;
            }

            Vector4f basePlanetColor = COLORS_PLANETS[i % COLORS_PLANETS.length];

            int trailSteps = 20;
            float trailLengthTicks = 15.0F;

            org.joml.Matrix4f mat = poseStack.last().pose();
            org.joml.Matrix3f nor = poseStack.last().normal();

            for (int t = 0; t < trailSteps; t++) {
                float ratio1 = 1.0F - ((float) t / trailSteps);
                float ratio2 = 1.0F - ((float) (t + 1) / trailSteps);

                float time1 = animatable.tickCount - (t * (trailLengthTicks / trailSteps));
                float time2 = animatable.tickCount - ((t + 1) * (trailLengthTicks / trailSteps));

                float angle1 = (time1 * 0.1F * speed) % (net.minecraft.util.Mth.PI * 2.0F);
                float angle2 = (time2 * 0.1F * speed) % (net.minecraft.util.Mth.PI * 2.0F);

                float w1 = basePlanetSize * ratio1;
                float w2 = basePlanetSize * ratio2;

                float cos1 = net.minecraft.util.Mth.cos(angle1);
                float sin1 = net.minecraft.util.Mth.sin(angle1);
                float cos2 = net.minecraft.util.Mth.cos(angle2);
                float sin2 = net.minecraft.util.Mth.sin(angle2);

                Vector4f c1 = new Vector4f(basePlanetColor.x() * ratio1, basePlanetColor.y() * ratio1, basePlanetColor.z() * ratio1, basePlanetColor.w() * ratio1);
                Vector4f c2 = new Vector4f(basePlanetColor.x() * ratio2, basePlanetColor.y() * ratio2, basePlanetColor.z() * ratio2, basePlanetColor.w() * ratio2);

                lightningConsumer.vertex(mat, cos1 * (radius - w1), sin1 * (radius - w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();
                lightningConsumer.vertex(mat, cos1 * (radius + w1), sin1 * (radius + w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();
                lightningConsumer.vertex(mat, cos2 * (radius + w2), sin2 * (radius + w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();
                lightningConsumer.vertex(mat, cos2 * (radius - w2), sin2 * (radius - w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, 1).endVertex();

                lightningConsumer.vertex(mat, cos2 * (radius - w2), sin2 * (radius - w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, -1).endVertex();
                lightningConsumer.vertex(mat, cos2 * (radius + w2), sin2 * (radius + w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, -1).endVertex();
                lightningConsumer.vertex(mat, cos1 * (radius + w1), sin1 * (radius + w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, -1).endVertex();
                lightningConsumer.vertex(mat, cos1 * (radius - w1), sin1 * (radius - w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 0, -1).endVertex();
            }

            poseStack.pushPose();
            float currentAngle = (animatable.tickCount * 0.1F * speed) % (net.minecraft.util.Mth.PI * 2.0F);
            poseStack.translate(net.minecraft.util.Mth.cos(currentAngle) * radius, net.minecraft.util.Mth.sin(currentAngle) * radius, 0.0F);
            GeometryHelper.drawSphere(poseStack, lightningConsumer, basePlanetSize, basePlanetColor, packedLight);
            poseStack.popPose();

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}