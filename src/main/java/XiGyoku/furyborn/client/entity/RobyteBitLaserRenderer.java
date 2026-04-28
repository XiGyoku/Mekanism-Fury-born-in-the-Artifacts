package XiGyoku.furyborn.client.entity;

import XiGyoku.furyborn.entity.RobyteBitLaserEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RobyteBitLaserRenderer extends EntityRenderer<RobyteBitLaserEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("furyborn", "textures/entity/robyte_bit_laser.png");
    private final RobyteBitLaserModel<RobyteBitLaserEntity> model;

    public RobyteBitLaserRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RobyteBitLaserModel<>(context.bakeLayer(RobyteBitLaserModel.LAYER_LOCATION));
    }

    @Override
    public void render(RobyteBitLaserEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.translate(0.0D, 0.30D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch - 90.0F));
        poseStack.scale(-2.0F, -2.0F, 2.0F);
        poseStack.translate(0.0D, -1.4375D, 0.0D);

        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();

        double lerpX = Mth.lerp(partialTicks, entity.xo, entity.getX());
        double lerpY = Mth.lerp(partialTicks, entity.yo, entity.getY());
        double lerpZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());
        Vector3f worldOffset = new Vector3f((float) lerpX, (float) lerpY + 0.30F, (float) lerpZ);

        Matrix4f effectPose = new Matrix4f();
        effectPose.rotationY((float) Math.toRadians(180.0F - yaw));
        effectPose.rotateX((float) Math.toRadians(-pitch));

        Vector3f localCenter = new Vector3f(0.0F, 0.0F, 0.5F);

        DriveshiftParticleRenderer.spawnOrientedOrbitalRing(
                effectPose, worldOffset, localCenter,
                0.2F, 0.02F, 2, 0.1F, 0.2F, 0.3F, 0.7F,
                new Vector4f(0.0F, 1.0F, 0.0F, 1.0F),
                new Vector4f(1.0F, 1.0F, 1.0F, 0.0F),
                0.05F
        );

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RobyteBitLaserEntity entity) {
        return TEXTURE;
    }
}