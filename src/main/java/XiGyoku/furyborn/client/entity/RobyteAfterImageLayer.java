package XiGyoku.furyborn.client.entity;

import XiGyoku.furyborn.entity.RobyteEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class RobyteAfterImageLayer extends GeoRenderLayer<RobyteEntity> {
    public RobyteAfterImageLayer(GeoEntityRenderer<RobyteEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, RobyteEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (animatable.isTransamMode() && !animatable.pastPositions.isEmpty()) {
            RenderType translucentType = RenderType.entityTranslucent(getTextureResource(animatable));
            net.minecraft.world.phys.Vec3 current = animatable.getPosition(partialTick);
            
            for (int i = 0; i < animatable.pastPositions.size(); i++) {
                poseStack.pushPose();
                net.minecraft.world.phys.Vec3 past = animatable.pastPositions.get(i);
                poseStack.translate(past.x - current.x, past.y - current.y, past.z - current.z);
                float alpha = Math.max(0.0f, 0.5f - (i * 0.1f));
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, translucentType, bufferSource.getBuffer(translucentType), partialTick, packedLight, packedOverlay, 1.0f, 0.2f, 0.2f, alpha);
                poseStack.popPose();
            }
        }
    }
}