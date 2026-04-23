package XiGyoku.furyborn.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.common.entity.EntityRobit;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

public class RobitAfterImageLayer<M extends EntityModel<EntityRobit>> extends RenderLayer<EntityRobit, M> {

    private static final Map<EntityRobit, LinkedList<Vec3>> pastPositionsMap = new WeakHashMap<>();
    private static final Map<EntityRobit, Integer> fusingStartTickMap = new WeakHashMap<>();

    public RobitAfterImageLayer(RenderLayerParent<EntityRobit, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EntityRobit robit, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean isFusing = robit.hasEffect(MobEffects.INVISIBILITY) && robit.isOnFire();

        if (!isFusing) {
            pastPositionsMap.remove(robit);
            fusingStartTickMap.remove(robit);
            return;
        }

        if (!fusingStartTickMap.containsKey(robit)) fusingStartTickMap.put(robit, robit.tickCount);
        int fusingTicks = robit.tickCount - fusingStartTickMap.get(robit);
        float baseAlpha = Math.max(0.0f, 1.0f - (fusingTicks / 80.0f));

        LinkedList<Vec3> pastPositions = pastPositionsMap.computeIfAbsent(robit, k -> new LinkedList<>());
        Vec3 currentPos = robit.getPosition(partialTick);

        if (pastPositions.isEmpty() || pastPositions.getFirst().distanceToSqr(currentPos) > 0.005) {
            pastPositions.addFirst(currentPos);
            if (pastPositions.size() > 8) pastPositions.removeLast();
        }

        ResourceLocation texture = getTextureLocation(robit);
        RenderType translucentType = RenderType.entityTranslucent(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(translucentType);

        poseStack.pushPose();
        getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 1.0f, 0.1f, 0.1f, baseAlpha);
        poseStack.popPose();

        for (int i = 0; i < pastPositions.size(); i++) {
            poseStack.pushPose();
            Vec3 past = pastPositions.get(i);
            poseStack.translate(past.x - currentPos.x, past.y - currentPos.y, past.z - currentPos.z);
            float alpha = (0.4f - (i * 0.05f)) * baseAlpha;
            getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 1.0f, 0.3f, 0.3f, Math.max(0, alpha));
            poseStack.popPose();
        }
    }
}