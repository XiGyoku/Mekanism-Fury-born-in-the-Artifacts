package XiGyoku.furyborn.client.entity;

import XiGyoku.furyborn.client.entity.DriveshiftParticleRenderer;
import XiGyoku.furyborn.event.PlayerDriveshiftEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;

public class SunRaiserDriveRenderer implements ICurioRenderer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("furyborn", "textures/item/sunraiser_drive.png");
    private final SunRaiserDriveModel<LivingEntity> model;

    public SunRaiserDriveRenderer() {
        this.model = new SunRaiserDriveModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(SunRaiserDriveModel.LAYER_LOCATION));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        matrixStack.pushPose();

        LivingEntity entity = slotContext.entity();

        float bodyYaw = Mth.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);

        Matrix4f bodyPose = new Matrix4f();
        bodyPose.rotationY((float) Math.toRadians(-bodyYaw));

        if (renderLayerParent.getModel() instanceof HumanoidModel) {
            ICurioRenderer.followBodyRotations(entity, (HumanoidModel<LivingEntity>) renderLayerParent.getModel());
        }

        List<SlotResult> equippedResults = CuriosApi.getCuriosHelper().findCurios(entity, stack.getItem());
        int totalEquipped = equippedResults.size();
        int renderIndex = 0;

        if (totalEquipped >= 2) {
            for (int i = 0; i < equippedResults.size(); i++) {
                SlotContext ctx = equippedResults.get(i).slotContext();
                if (ctx.identifier().equals(slotContext.identifier()) && ctx.index() == slotContext.index()) {
                    renderIndex = i;
                    break;
                }
            }

            matrixStack.translate(0.0, 0.0, 0.275);
            bodyPose.translate(0.0F, 0.0F, -0.175F);

            if (renderIndex % 2 == 0) {
                matrixStack.translate(-0.400, -0.125, -0.075);
                matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F));

                bodyPose.translate(-0.375F, -0.125F, 0.425F);
                bodyPose.rotateLocalY((float) Math.toRadians(270.0));
            } else {
                matrixStack.scale(-1.0F, 1.0F, 1.0F);
                matrixStack.translate(-0.400, -0.125, -0.075);
                matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F));

                bodyPose.translate(0.325F, -0.125F, 0.425F);
                bodyPose.rotateLocalY((float) Math.toRadians(270.0));
            }
        } else {
            matrixStack.translate(0.0, -0.125, 0.25);
            matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F));

            bodyPose.translate(0.00F, -0.100F, 0.25F);
            bodyPose.rotateLocalY((float) Math.toRadians(-90.0));
        }

        bodyPose.rotateLocalY((float) Math.toRadians(-90.0));

        matrixStack.scale(0.25F, 0.25F, 0.25F);

        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.entityTranslucent(TEXTURE), false, stack.hasFoil());
        this.model.renderToBuffer(matrixStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        int currentTick = entity.tickCount;
        String nbtKey = "SunRaiserLastSpawn_" + slotContext.index();

        if (entity.getPersistentData().getInt(nbtKey) != currentTick) {
            entity.getPersistentData().putInt(nbtKey, currentTick);

            double lerpX = Mth.lerp(partialTicks, entity.xo, entity.getX());
            double lerpY = Mth.lerp(partialTicks, entity.yo, entity.getY());
            double lerpZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());

            double bodyOffset = 1.4D;
            Vector3f worldOffset = new Vector3f((float) lerpX, (float) (lerpY + bodyOffset), (float) lerpZ);

            boolean isDriveshift = entity.getPersistentData().getBoolean("ExolumenAfterImage");

            Vector4f color1 = new Vector4f(0.0F, 1.0F, 0.0F, 1.0F);
            Vector4f color2 = new Vector4f(0.3F, 1.0F, 0.3F, 1.0F);
            float brightMult = isDriveshift ? 3.0F : 1.0F;

            if (currentTick % 2 == 0) {
                Vector3f ringCenter = new Vector3f(0.0F, 0.0F, 0.0F);
                int ringCount = isDriveshift ? 16 : 8;
                DriveshiftParticleRenderer.spawnOrientedOrbitalRing(bodyPose, worldOffset, ringCenter, 0.15F, 0.05F, ringCount, 0.1F, 0.2F, 0.5F * brightMult, 1.0F * brightMult, color1, color2, 0.05F);
            }

            if (currentTick % 20 == 0) {
                Vector3f waveCenter1 = new Vector3f(0.0F, 0.0F, 0.0F);
                float waveZ = isDriveshift ? 2.25F : 1.5F;
                Vector3f waveCenter2 = new Vector3f(0.0F, 0.0F, waveZ);
                int waveCount = isDriveshift ? 192 : 96;
                float endRadius = isDriveshift ? 0.75F : 0.5F;
                float waveSpeed = isDriveshift ? 0.06F : 0.04F;
                DriveshiftParticleRenderer.spawnOrientedCircleToCircleWave(bodyPose, worldOffset, waveCenter1, 0.15F, waveCenter2, endRadius, waveCount, waveSpeed, 0.6F * brightMult, 1.2F * brightMult, color1, color2, 0.06F);
            }

            if (isDriveshift) {
                if (currentTick % 2 == 0) {
                    Vector3f scatterCenter = new Vector3f(0.0F, 0.0F, 0.0F);
                    DriveshiftParticleRenderer.spawnOrientedCone(bodyPose, worldOffset, scatterCenter, -5.0F, 20, 0.1F, 0.8F * brightMult, 1.5F * brightMult, color1, color2, 0.30F);
                    DriveshiftParticleRenderer.spawnOrientedCone(bodyPose, worldOffset, scatterCenter, -1.0F, 30, 0.15F, 1.0F * brightMult, 2.0F * brightMult, color1, color2, 0.06F);
                }

                if (entity instanceof Player player) {
                    int driveCount = PlayerDriveshiftEvent.getDriveCount(player);
                    int xrossCount = PlayerDriveshiftEvent.getXrossCount(player);

                    if (driveCount >= 2 && currentTick % 5 == 0) {
                        Vector3f coneStart = new Vector3f(0.0F, 0.0F, 0.0F);
                        int scatterCount = (xrossCount >= 1) ? 120 : 40;
                        DriveshiftParticleRenderer.spawnOrientedCone(bodyPose, worldOffset, coneStart, -3.0F, scatterCount, 0.08F, 0.8F * brightMult, 1.5F * brightMult, color1, color2, 0.08F);
                    }
                }
            }
        }

        matrixStack.popPose();
    }
}