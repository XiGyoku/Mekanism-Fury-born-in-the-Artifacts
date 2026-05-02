package XiGyoku.furyborn.client.entity;

import XiGyoku.furyborn.entity.RoadBikeBitEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RoadBikeBitRenderer extends GeoEntityRenderer<RoadBikeBitEntity> {
    public RoadBikeBitRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RoadBikeBitModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(RoadBikeBitEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0f, -0.25f, 0.0f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}