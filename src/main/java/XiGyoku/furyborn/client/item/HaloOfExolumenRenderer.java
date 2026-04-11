package XiGyoku.furyborn.client.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import XiGyoku.furyborn.client.util.GeometryHelper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class HaloOfExolumenRenderer implements ICurioRenderer {
    private static final Vector4f COLOR_SUN = new Vector4f(1.0F, 0.2F, 0.0F, 1.0F);
    private static final Vector4f COLOR_ORBIT = new Vector4f(1.0F, 1.0F, 1.0F, 0.4F);
    private static final Vector4f COLOR_HAZE = new Vector4f(0.1F, 0.0F, 0.2F, 0.5F);
    private static final Vector4f[] COLORS_PLANETS = new Vector4f[]{
            new Vector4f(0.2F, 0.8F, 1.0F, 1.0F),
            new Vector4f(0.2F, 1.0F, 0.3F, 1.0F),
            new Vector4f(1.0F, 1.0F, 0.2F, 1.0F),
            new Vector4f(1.0F, 0.5F, 0.0F, 1.0F),
            new Vector4f(0.8F, 0.2F, 1.0F, 1.0F)
    };

    private static final float SCALE_GLOBAL = 0.5F;
    private static final float RADIUS_SUN = 0.4F;
    private static final float HAZE_SIZE = 0.50F;
    private static final float[] ORBIT_RADII = new float[]{ 1.0F, 1.5F, 2.0F, 2.5F, 3.0F };
    private static final float[] PLANET_RADII = new float[]{ 0.15F, 0.18F, 0.2F, 0.22F, 0.25F };
    private static final float[] ORBIT_SPEEDS = new float[]{ 2.5F, 1.8F, 1.4F, 1.1F, 0.9F };

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer,
            int light, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch) {

        LivingEntity entity = slotContext.entity();
        poseStack.pushPose();
        ICurioRenderer.translateIfSneaking(poseStack, entity);
        ICurioRenderer.rotateIfSneaking(poseStack, entity);

        poseStack.translate(XiGyoku.furyborn.Config.haloOffsetX, XiGyoku.furyborn.Config.haloOffsetY, XiGyoku.furyborn.Config.haloOffsetZ);
        poseStack.scale(SCALE_GLOBAL, SCALE_GLOBAL, SCALE_GLOBAL);

        poseStack.mulPose(Axis.ZP.rotationDegrees(XiGyoku.furyborn.Config.haloRotZ));
        poseStack.mulPose(Axis.YP.rotationDegrees(XiGyoku.furyborn.Config.haloRotY));
        poseStack.mulPose(Axis.XP.rotationDegrees(XiGyoku.furyborn.Config.haloRotX));
        RenderSystem.disableCull();

        poseStack.pushPose();
        poseStack.translate(0, 0, -0.01F);
        VertexConsumer hazeConsumer = renderTypeBuffer.getBuffer(RenderType.lightning());
        GeometryHelper.drawSolidCircle(poseStack, hazeConsumer, HAZE_SIZE, COLOR_HAZE, light);
        poseStack.popPose();

        VertexConsumer orbitConsumer = renderTypeBuffer.getBuffer(RenderType.lightning());
        for (float radius : ORBIT_RADII) {
            GeometryHelper.drawRing(poseStack, orbitConsumer, radius, 0.02F, COLOR_ORBIT, light);
        }

        VertexConsumer sunConsumer = renderTypeBuffer.getBuffer(RenderType.lightning());
        GeometryHelper.drawNoiseSphere(poseStack, sunConsumer, RADIUS_SUN, COLOR_SUN, 0.8F, light);

        VertexConsumer planetConsumer = renderTypeBuffer.getBuffer(RenderType.lightning());
        for (int i = 0; i < 5; i++) {
            float radius = ORBIT_RADII[i];
            float speed = ORBIT_SPEEDS[i];
            float basePlanetSize = PLANET_RADII[i];
            Vector4f basePlanetColor = COLORS_PLANETS[i];

            int trailSteps = 20;
            float trailLengthTicks = 15.0F;

            Matrix4f mat = poseStack.last().pose();
            Matrix3f nor = poseStack.last().normal();

            for (int t = 0; t < trailSteps; t++) {
                float ratio1 = 1.0F - ((float) t / trailSteps);
                float ratio2 = 1.0F - ((float) (t + 1) / trailSteps);

                float time1 = ageInTicks - (t * (trailLengthTicks / trailSteps));
                float time2 = ageInTicks - ((t + 1) * (trailLengthTicks / trailSteps));

                float angle1 = (time1 * 0.1F * speed) % (Mth.PI * 2.0F);
                float angle2 = (time2 * 0.1F * speed) % (Mth.PI * 2.0F);

                float w1 = basePlanetSize * ratio1;
                float w2 = basePlanetSize * ratio2;

                float cos1 = Mth.cos(angle1);
                float sin1 = Mth.sin(angle1);
                float cos2 = Mth.cos(angle2);
                float sin2 = Mth.sin(angle2);

                Vector4f c1 = new Vector4f(basePlanetColor.x() * ratio1, basePlanetColor.y() * ratio1, basePlanetColor.z() * ratio1, basePlanetColor.w() * ratio1);
                Vector4f c2 = new Vector4f(basePlanetColor.x() * ratio2, basePlanetColor.y() * ratio2, basePlanetColor.z() * ratio2, basePlanetColor.w() * ratio2);

                planetConsumer.vertex(mat, cos1 * (radius - w1), sin1 * (radius - w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(0, 0).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, 1).endVertex();
                planetConsumer.vertex(mat, cos1 * (radius + w1), sin1 * (radius + w1), 0).color(c1.x(), c1.y(), c1.z(), c1.w()).uv(1, 1).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, 1).endVertex();
                planetConsumer.vertex(mat, cos2 * (radius + w2), sin2 * (radius + w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(1, 1).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, 1).endVertex();
                planetConsumer.vertex(mat, cos2 * (radius - w2), sin2 * (radius - w2), 0).color(c2.x(), c2.y(), c2.z(), c2.w()).uv(0, 0).overlayCoords(0, 0).uv2(light).normal(nor, 0, 0, 1).endVertex();
            }

            poseStack.pushPose();
            float currentAngle = (ageInTicks * 0.1F * speed) % (Mth.PI * 2.0F);
            poseStack.translate(Mth.cos(currentAngle) * radius, Mth.sin(currentAngle) * radius, 0.0F);
            GeometryHelper.drawSphere(poseStack, planetConsumer, basePlanetSize, basePlanetColor, light);
            poseStack.popPose();
        }
        RenderSystem.enableCull();
        poseStack.popPose();
    }
}