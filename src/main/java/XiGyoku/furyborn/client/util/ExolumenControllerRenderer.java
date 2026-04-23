package XiGyoku.furyborn.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.api.text.EnumColor;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.common.lib.effect.BoltEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import XiGyoku.furyborn.blockentity.ExolumenControllerBlockEntity;
import XiGyoku.furyborn.blockentity.PortalAnimationState;
import net.minecraft.util.Mth;

public class ExolumenControllerRenderer implements BlockEntityRenderer<ExolumenControllerBlockEntity> {

    private final ModelEnergyCore fusionCoreModel;
    private final BoltRenderer boltRenderer = new BoltRenderer();

    public ExolumenControllerRenderer(BlockEntityRendererProvider.Context context) {
        this.fusionCoreModel = new ModelEnergyCore(context.getModelSet());
    }

    @Override
    public void render(ExolumenControllerBlockEntity entity, float partialTicks, PoseStack matrix, MultiBufferSource bufferSource, int light, int overlayLight) {
        PortalAnimationState state = entity.getCurrentState();
        BlockPos centerPos = entity.getPortalCenter();

        if (state == PortalAnimationState.IDLE || !entity.isMaster() || centerPos == null) return;

        int tick = entity.getAnimationTick();

        Vec3 absoluteCenter = new Vec3(centerPos.getX() + 0.5, centerPos.getY(), centerPos.getZ() + 0.5);
        Vec3 relativeCenter = absoluteCenter.subtract(Vec3.atLowerCornerOf(entity.getBlockPos()));
        Vec3 effectCenter = new Vec3(relativeCenter.x, 4.0, relativeCenter.z);

        if (state == PortalAnimationState.CHARGING) {
            renderBoltsFromAll16Coils(entity, partialTicks, matrix, bufferSource, absoluteCenter, effectCenter, tick);

            if (tick > 50) {
                float chargeProgress = (tick - 50) / 50.0f;
                renderFusionCore(matrix, bufferSource, partialTicks, effectCenter, 1.2f * chargeProgress, 1.0f, overlayLight);
            }
        }

        if (state == PortalAnimationState.FUSING) {
            renderFusionCore(matrix, bufferSource, partialTicks, effectCenter, 1.2f, 1.0f, overlayLight);
        } else if (state == PortalAnimationState.DROPPING) {
            float drop = tick / 40.0f;
            renderFusionCore(matrix, bufferSource, partialTicks, effectCenter.add(0, -4.0 * drop, 0), 1.2f, 1.0f, overlayLight);
        }
    }

    private void renderBoltsFromAll16Coils(ExolumenControllerBlockEntity entity, float partialTicks, PoseStack matrix, MultiBufferSource bufferSource, Vec3 absoluteCenter, Vec3 targetCenter, int tick) {
        float intensity = Math.min(1.0f, tick / 40.0f);
        int coilIndex = 0;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    Vec3 absStart = new Vec3(absoluteCenter.x + x, absoluteCenter.y + 1.0, absoluteCenter.z + z);
                    Vec3 relStart = absStart.subtract(Vec3.atLowerCornerOf(entity.getBlockPos()));

                    BoltEffect bolt = new BoltEffect(BoltEffect.BoltRenderInfo.ELECTRICITY, relStart, targetCenter, 10)
                            .size(0.06f * intensity).lifespan(3).spawn(BoltEffect.SpawnFunction.NO_DELAY);
                    boltRenderer.update(entity.getBlockPos().hashCode() + coilIndex, bolt, partialTicks);
                    coilIndex++;
                }
            }
        }
        boltRenderer.render(partialTicks, matrix, bufferSource);
    }

    private void renderFusionCore(PoseStack matrix, MultiBufferSource bufferSource, float partialTicks, Vec3 pos, float scale, float alpha, int overlay) {
        float ticks = Minecraft.getInstance().levelRenderer.getTicks() + partialTicks;
        VertexConsumer buffer = bufferSource.getBuffer(fusionCoreModel.RENDER_TYPE);

        matrix.pushPose();
        matrix.translate(pos.x, pos.y, pos.z);
        matrix.scale(scale * 2.0f, scale * 2.0f, scale * 2.0f);

        float s1 = 1 + 0.2f * Mth.sin(ticks * 0.2f);
        matrix.pushPose();
        matrix.scale(s1, s1, s1);
        matrix.mulPose(Axis.YP.rotationDegrees(ticks * 10));
        fusionCoreModel.render(matrix, buffer, LightTexture.FULL_BRIGHT, overlay, EnumColor.AQUA, alpha);
        matrix.popPose();

        matrix.popPose();
    }
}