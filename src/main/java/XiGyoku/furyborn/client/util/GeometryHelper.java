package XiGyoku.furyborn.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class GeometryHelper {
    private static final int CIRCLE_STEPS = 64;
    private static final int SPHERE_PARALLELS = 16;
    private static final int SPHERE_MERIDIANS = 32;

    public static void drawSphere(PoseStack poseStack, VertexConsumer consumer, float radius, Vector4f color, int light) {
        drawNoiseSphere(poseStack, consumer, radius, color, 0.0F, light);
    }

    public static void drawNoiseSphere(PoseStack poseStack, VertexConsumer consumer, float radius, Vector4f color, float noiseAmount, int light) {
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f matrix4f = lastPose.pose();
        Matrix3f matrix3f = lastPose.normal();

        for (int i = 0; i < SPHERE_PARALLELS; i++) {
            float theta1 = (float) i * Mth.PI / SPHERE_PARALLELS;
            float theta2 = (float) (i + 1) * Mth.PI / SPHERE_PARALLELS;
            float sinTheta1 = Mth.sin(theta1), cosTheta1 = Mth.cos(theta1);
            float sinTheta2 = Mth.sin(theta2), cosTheta2 = Mth.cos(theta2);
            for (int j = 0; j < SPHERE_MERIDIANS; j++) {
                float phi1 = (float) j * 2.0F * Mth.PI / SPHERE_MERIDIANS;
                float phi2 = (float) (j + 1) * 2.0F * Mth.PI / SPHERE_MERIDIANS;
                float sinPhi1 = Mth.sin(phi1), cosPhi1 = Mth.cos(phi1);
                float sinPhi2 = Mth.sin(phi2), cosPhi2 = Mth.cos(phi2);

                Vector4f c1 = getRandomizedColor(color, noiseAmount);
                Vector4f c2 = getRandomizedColor(color, noiseAmount);
                Vector4f c3 = getRandomizedColor(color, noiseAmount);
                Vector4f c4 = getRandomizedColor(color, noiseAmount);

                addVertex(consumer, matrix4f, matrix3f, radius * sinTheta1 * cosPhi1, radius * sinTheta1 * sinPhi1, radius * cosTheta1, c1, 0, 0, light);
                addVertex(consumer, matrix4f, matrix3f, radius * sinTheta2 * cosPhi1, radius * sinTheta2 * sinPhi1, radius * cosTheta2, c2, 0, 1, light);
                addVertex(consumer, matrix4f, matrix3f, radius * sinTheta2 * cosPhi2, radius * sinTheta2 * sinPhi2, radius * cosTheta2, c3, 1, 1, light);
                addVertex(consumer, matrix4f, matrix3f, radius * sinTheta1 * cosPhi2, radius * sinTheta1 * sinPhi2, radius * cosTheta1, c4, 1, 0, light);
            }
        }
    }

    private static Vector4f getRandomizedColor(Vector4f baseColor, float noiseAmount) {
        if (noiseAmount == 0.0F) return baseColor;
        float noise = ((float) Math.random() - 0.2F) * noiseAmount;
        return new Vector4f(
                Mth.clamp(baseColor.x() + noise, 0.0F, 1.0F),
                Mth.clamp(baseColor.y() + noise, 0.0F, 1.0F),
                Mth.clamp(baseColor.z() + noise, 0.0F, 1.0F),
                baseColor.w()
        );
    }

    public static void drawRing(PoseStack poseStack, VertexConsumer consumer, float radius, float thickness, Vector4f color, int light) {
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f matrix4f = lastPose.pose();
        Matrix3f matrix3f = lastPose.normal();

        float innerRadius = radius - thickness / 2.0F;
        float outerRadius = radius + thickness / 2.0F;

        for (int i = 0; i < CIRCLE_STEPS; i++) {
            float angle1 = (float) i * 2.0F * Mth.PI / CIRCLE_STEPS;
            float angle2 = (float) (i + 1) * 2.0F * Mth.PI / CIRCLE_STEPS;
            float cos1 = Mth.cos(angle1), sin1 = Mth.sin(angle1);
            float cos2 = Mth.cos(angle2), sin2 = Mth.sin(angle2);

            addVertex(consumer, matrix4f, matrix3f, cos1 * innerRadius, sin1 * innerRadius, 0, color, 0, 0, light);
            addVertex(consumer, matrix4f, matrix3f, cos1 * outerRadius, sin1 * outerRadius, 0, color, 1, 1, light);
            addVertex(consumer, matrix4f, matrix3f, cos2 * outerRadius, sin2 * outerRadius, 0, color, 1, 1, light);
            addVertex(consumer, matrix4f, matrix3f, cos2 * innerRadius, sin2 * innerRadius, 0, color, 0, 0, light);
        }
    }

    public static void drawSolidCircle(PoseStack poseStack, VertexConsumer consumer, float radius, Vector4f color, int light) {
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f matrix4f = lastPose.pose();
        Matrix3f matrix3f = lastPose.normal();

        for (int i = 0; i < CIRCLE_STEPS; i++) {
            float angle1 = (float) i * 2.0F * Mth.PI / CIRCLE_STEPS;
            float angle2 = (float) (i + 1) * 2.0F * Mth.PI / CIRCLE_STEPS;
            float cos1 = Mth.cos(angle1), sin1 = Mth.sin(angle1);
            float cos2 = Mth.cos(angle2), sin2 = Mth.sin(angle2);

            addVertex(consumer, matrix4f, matrix3f, 0, 0, 0, color, 0.5F, 0.5F, light);
            addVertex(consumer, matrix4f, matrix3f, cos1 * radius, sin1 * radius, 0, color, cos1 * 0.5F + 0.5F, sin1 * 0.5F + 0.5F, light);
            addVertex(consumer, matrix4f, matrix3f, cos2 * radius, sin2 * radius, 0, color, cos2 * 0.5F + 0.5F, sin2 * 0.5F + 0.5F, light);
            addVertex(consumer, matrix4f, matrix3f, cos2 * radius, sin2 * radius, 0, color, cos2 * 0.5F + 0.5F, sin2 * 0.5F + 0.5F, light);
        }
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f mat, Matrix3f nor, float x, float y, float z, Vector4f color, float u, float v, int light) {
        float nx = x; float ny = y; float nz = z;
        float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0) { nx /= len; ny /= len; nz /= len; } else { nz = 1; }

        consumer.vertex(mat, x, y, z)
                .color(color.x(), color.y(), color.z(), color.w())
                .uv(u, v)
                .overlayCoords(0, 0)
                .uv2(light)
                .normal(nor, nx, ny, nz)
                .endVertex();
    }
}