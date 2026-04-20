package XiGyoku.furyborn.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import XiGyoku.furyborn.entity.RobyteEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import net.minecraft.util.Mth;

public class RobyteOrbitalRingLayer extends GeoRenderLayer<RobyteEntity> {
    public RobyteOrbitalRingLayer(GeoEntityRenderer<RobyteEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, RobyteEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        float deathScale = 1.0F;
        if (animatable.deathTime > 0) {
            deathScale = 1.0F - (((float) animatable.deathTime + partialTick) / 140.0F);
            if (deathScale < 0.0F) deathScale = 0.0F;
        }

        float currentScale = Mth.lerp(partialTick, animatable.prevRingScale, animatable.ringScale);

        if (deathScale > 0.0F && currentScale > 0.0F) {
            Matrix4f pose = new Matrix4f();
            float time = (float) animatable.tickCount + partialTick;
            pose.rotateX(time * 0.15F);
            pose.rotateY(time * 0.2F);
            pose.rotateZ(time * 0.1F);

            Vector3f worldOffset = new Vector3f((float)animatable.getX(), (float)(animatable.getY() + 2.5D), (float)animatable.getZ());
            Vector3f localCenter = new Vector3f(0.0F, 0.0F, 0.0F);

            DriveshiftParticleRenderer.spawnOrientedOrbitalRing(
                    pose, worldOffset, localCenter,
                    10.5F * deathScale * currentScale, 0.2F * currentScale, 250, 0.05F, 0.1F, 0.3F, 0.7F,
                    new Vector4f(0.0F, 1.0F, 0.0F, 1.0F),
                    new Vector4f(1.0F, 1.0F, 1.0F, 0.0F),
                    0.10F
            );
        }
    }
}