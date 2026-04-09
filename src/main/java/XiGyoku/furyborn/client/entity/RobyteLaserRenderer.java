package XiGyoku.furyborn.client.entity;

import XiGyoku.furyborn.entity.RobyteLaserEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RobyteLaserRenderer extends EntityRenderer<RobyteLaserEntity> {
    public RobyteLaserRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(RobyteLaserEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

        float age = entity.tickCount + partialTicks;
        float maxLife = (float) entity.getMaxLife();
        float targetRadius = entity.getRadius();

        float growDuration = 5.0F;
        float radiusScale = Mth.clamp(age / growDuration, 0.0F, 1.0F);
        float currentRadius = targetRadius * radiusScale;

        float fadeDuration = 10.0F;
        float remainingLife = maxLife - age;
        float alphaScale = Mth.clamp(remainingLife / fadeDuration, 0.0F, 1.0F);
        int currentAlpha = (int) (200 * alphaScale);

        if (currentAlpha > 0 && currentRadius > 0.0F) {
            float length = 200.0F;
            int segments = 16;

            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lightning());

            Matrix4f posMatrix = poseStack.last().pose();
            Matrix3f normalMatrix = poseStack.last().normal();

            int[] color = entity.isExplosive() ? new int[]{255, 255, 0} : new int[]{0, 255, 0};
            color = entity.isOvercharge() ? new int[]{255, 51, 153} : color;

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) i / segments * (float) Math.PI * 2.0F;
                float angle2 = (float) (i + 1) / segments * (float) Math.PI * 2.0F;

                float x1 = (float) Math.cos(angle1) * currentRadius;
                float z1 = (float) Math.sin(angle1) * currentRadius;
                float x2 = (float) Math.cos(angle2) * currentRadius;
                float z2 = (float) Math.sin(angle2) * currentRadius;

                addVertex(vertexConsumer, posMatrix, normalMatrix, x1, z1, 0, color[0], color[1], color[2], currentAlpha);
                addVertex(vertexConsumer, posMatrix, normalMatrix, x1, z1, length, color[0], color[1], color[2], currentAlpha);
                addVertex(vertexConsumer, posMatrix, normalMatrix, x2, z2, length, color[0], color[1], color[2], currentAlpha);
                addVertex(vertexConsumer, posMatrix, normalMatrix, x2, z2, 0, color[0], color[1], color[2], currentAlpha);
            }
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f posMatrix, Matrix3f normalMatrix, float x, float z, float y, int r, int g, int b, int a) {
        consumer.vertex(posMatrix, x, z, y)
                .color(r, g, b, a)
                .normal(normalMatrix, 0, 1, 0)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(RobyteLaserEntity entity) {
        return null;
    }

    @Override
    public boolean shouldRender(RobyteLaserEntity pEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }
}