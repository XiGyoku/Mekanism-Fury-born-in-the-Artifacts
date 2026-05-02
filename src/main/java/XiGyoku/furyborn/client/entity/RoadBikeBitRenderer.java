package XiGyoku.furyborn.client.entity;

import XiGyoku.furyborn.entity.RoadBikeBitEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RoadBikeBitRenderer extends GeoEntityRenderer<RoadBikeBitEntity> {
    public RoadBikeBitRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RoadBikeBitModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(RoadBikeBitEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.trailPositions.size() >= 2) {
            poseStack.pushPose();
            Matrix4f mat = poseStack.last().pose();
            Matrix3f nor = poseStack.last().normal();
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());

            double lerpX = Mth.lerp(partialTick, entity.xo, entity.getX());
            double lerpY = Mth.lerp(partialTick, entity.yo, entity.getY());
            double lerpZ = Mth.lerp(partialTick, entity.zo, entity.getZ());

            float r = 0.1F, g = 1.0F, b = 0.2F;

            for (int i = 0; i < entity.trailPositions.size() - 1; i++) {
                RoadBikeBitEntity.TrailPoint p1 = entity.trailPositions.get(i);
                RoadBikeBitEntity.TrailPoint p2 = entity.trailPositions.get(i + 1);

                float alpha1 = 1.0F - ((float) i / entity.trailPositions.size());
                float alpha2 = 1.0F - ((float) (i + 1) / entity.trailPositions.size());
                alpha1 *= 0.8F;
                alpha2 *= 0.8F;

                float x1L = (float) (p1.left.x - lerpX);
                float y1L = (float) (p1.left.y - lerpY);
                float z1L = (float) (p1.left.z - lerpZ);

                float x1R = (float) (p1.right.x - lerpX);
                float y1R = (float) (p1.right.y - lerpY);
                float z1R = (float) (p1.right.z - lerpZ);

                float x2L = (float) (p2.left.x - lerpX);
                float y2L = (float) (p2.left.y - lerpY);
                float z2L = (float) (p2.left.z - lerpZ);

                float x2R = (float) (p2.right.x - lerpX);
                float y2R = (float) (p2.right.y - lerpY);
                float z2R = (float) (p2.right.z - lerpZ);

                consumer.vertex(mat, x1L, y1L, z1L).color(r, g, b, alpha1).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 1, 0).endVertex();
                consumer.vertex(mat, x1R, y1R, z1R).color(r, g, b, alpha1).uv(1, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 1, 0).endVertex();
                consumer.vertex(mat, x2R, y2R, z2R).color(r, g, b, alpha2).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 1, 0).endVertex();
                consumer.vertex(mat, x2L, y2L, z2L).color(r, g, b, alpha2).uv(0, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, 1, 0).endVertex();

                consumer.vertex(mat, x2L, y2L, z2L).color(r, g, b, alpha2).uv(0, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, -1, 0).endVertex();
                consumer.vertex(mat, x2R, y2R, z2R).color(r, g, b, alpha2).uv(1, 1).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, -1, 0).endVertex();
                consumer.vertex(mat, x1R, y1R, z1R).color(r, g, b, alpha1).uv(1, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, -1, 0).endVertex();
                consumer.vertex(mat, x1L, y1L, z1L).color(r, g, b, alpha1).uv(0, 0).overlayCoords(0, 0).uv2(packedLight).normal(nor, 0, -1, 0).endVertex();
            }
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(0.0f, -0.25f, 0.0f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}