package XiGyoku.furyborn.entity.client;

import XiGyoku.furyborn.entity.RobyteAreaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class RobyteAreaRenderer extends EntityRenderer<RobyteAreaEntity> {
    private static final ResourceLocation PORTAL_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/end_portal.png");
    private final List<ResourceLocation> frames = new ArrayList<>();

    public RobyteAreaRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        for (int i = 1; i <= 21; i++) {
            frames.add(ResourceLocation.fromNamespaceAndPath("furyborn", "textures/effect/operation/operation_effect_" + i + ".png"));
        }
    }

    @Override
    public void render(RobyteAreaEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (!pEntity.isAlive()) return;
        long gameTime = pEntity.level().getGameTime();
        int currentFrame = (int) ((gameTime / 2) % 21);

        int enlightment = LightTexture.FULL_BRIGHT;
        float radius = 64.0f;
        float height = 64.0f;
        float tileSize = 4.0f;

        VertexConsumer innerConsumer = pBuffer.getBuffer(RenderType.entityTranslucent(frames.get(currentFrame)));
        renderBox(poseStack, innerConsumer, radius, height, tileSize, enlightment, 1.0f);
        VertexConsumer outerConsumer = pBuffer.getBuffer(RenderType.entitySolid(PORTAL_TEXTURE));
        poseStack.pushPose();

        float offset = 0.5f;
        float scaleXZ = (radius + offset) / radius;
        float scaleY = (height + offset * 2) / height;

        poseStack.translate(0, -offset, 0);
        poseStack.scale(scaleXZ, scaleY, scaleXZ);
        renderBox(poseStack, outerConsumer, radius, height, tileSize, enlightment, 1.0f);
        poseStack.popPose();
    }

    private void renderBox(PoseStack poseStack, VertexConsumer consumer, float radius, float height, float tileSize, int enlightment, float alpha) {
        for (int i = 0; i < 4; i++) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(i * 90.0F));
            poseStack.translate(0, 0, radius);
            Matrix4f matrix = poseStack.last().pose();
            for (float x = -radius; x < radius; x += tileSize) {
                for (float y = 0; y < height; y += tileSize) {
                    float x1 = x;
                    float x2 = x + tileSize;
                    float y1 = y;
                    float y2 = y + tileSize;
                    consumer.vertex(matrix, x1, y1, 0).color(1.0f, 1.0f, 1.0f, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, 0, 1).endVertex();
                    consumer.vertex(matrix, x1, y2, 0).color(1.0f, 1.0f, 1.0f, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, 0, 1).endVertex();
                    consumer.vertex(matrix, x2, y2, 0).color(1.0f, 1.0f, 1.0f, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, 0, 1).endVertex();
                    consumer.vertex(matrix, x2, y1, 0).color(1.0f, 1.0f, 1.0f, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, 0, 1).endVertex();
                }
            }
            poseStack.popPose();
        }
        poseStack.pushPose();
        Matrix4f matrixTop = poseStack.last().pose();
        for (float x = -radius; x < radius; x += tileSize) {
            for (float z = -radius; z < radius; z += tileSize) {
                float x1 = x;
                float x2 = x + tileSize;
                float z1 = z;
                float z2 = z + tileSize;
                consumer.vertex(matrixTop, x1, height, z2).color(1.0f, 1.0f, 1.0f, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, 1, 0).endVertex();
                consumer.vertex(matrixTop, x1, height, z1).color(1.0f, 1.0f, 1.0f, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, 1, 0).endVertex();
                consumer.vertex(matrixTop, x2, height, z1).color(1.0f, 1.0f, 1.0f, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, 1, 0).endVertex();
                consumer.vertex(matrixTop, x2, height, z2).color(1.0f, 1.0f, 1.0f, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, 1, 0).endVertex();
            }
        }
        poseStack.popPose();
        poseStack.pushPose();
        Matrix4f matrixBottom = poseStack.last().pose();
        for (float x = -radius; x < radius; x += tileSize) {
            for (float z = -radius; z < radius; z += tileSize) {
                float x1 = x;
                float x2 = x + tileSize;
                float z1 = z;
                float z2 = z + tileSize;
                consumer.vertex(matrixBottom, x1, 0, z1).color(1.0f, 1.0f, 1.0f, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, -1, 0).endVertex();
                consumer.vertex(matrixBottom, x1, 0, z2).color(1.0f, 1.0f, 1.0f, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, -1, 0).endVertex();
                consumer.vertex(matrixBottom, x2, 0, z2).color(1.0f, 1.0f, 1.0f, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, -1, 0).endVertex();
                consumer.vertex(matrixBottom, x2, 0, z1).color(1.0f, 1.0f, 1.0f, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(enlightment).normal(0, -1, 0).endVertex();
            }
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(RobyteAreaEntity robyteAreaEntity) {
        return new ResourceLocation("minecraft", "textures/particle/white_heart.png");
    }
}
