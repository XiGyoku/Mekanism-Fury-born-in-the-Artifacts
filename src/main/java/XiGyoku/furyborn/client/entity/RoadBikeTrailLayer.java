package XiGyoku.furyborn.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import XiGyoku.furyborn.entity.RoadBikeBitEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class RoadBikeTrailLayer extends GeoRenderLayer<RoadBikeBitEntity> {
    
    public RoadBikeTrailLayer(GeoEntityRenderer<RoadBikeBitEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, RoadBikeBitEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (animatable.trailPositions.size() < 2) return;

        poseStack.pushPose();
        
        float yaw = Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0F - yaw)));

        Matrix4f mat = poseStack.last().pose();
        Matrix3f nor = poseStack.last().normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());

        double lerpX = Mth.lerp(partialTick, animatable.xo, animatable.getX());
        double lerpY = Mth.lerp(partialTick, animatable.yo, animatable.getY());
        double lerpZ = Mth.lerp(partialTick, animatable.zo, animatable.getZ());

        float r = 0.0F, g = 1.0F, b = 1.0F;

        for (int i = 0; i < animatable.trailPositions.size() - 1; i++) {
            RoadBikeBitEntity.TrailPoint p1 = animatable.trailPositions.get(i);
            RoadBikeBitEntity.TrailPoint p2 = animatable.trailPositions.get(i + 1);

            float alpha1 = 1.0F - ((float) i / animatable.trailPositions.size());
            float alpha2 = 1.0F - ((float) (i + 1) / animatable.trailPositions.size());
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
}