package XiGyoku.furyborn.client.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PhotonRenderUtil {

    private static abstract class CustomRenderType extends RenderType {
        private CustomRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }

        public static final RenderType GLOWING_PHOTON = RenderType.create(
                "furyborn_glowing_photon",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)
        );
    }

    public static final RenderType GLOWING_PHOTON = CustomRenderType.GLOWING_PHOTON;

    public static void drawPhoton(PoseStack poseStack, VertexConsumer consumer, Vector3f pos, float size, Vector4f color, float brightness) {
        poseStack.pushPose();
        poseStack.translate(pos.x(), pos.y(), pos.z());

        Quaternionf cameraRotation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        poseStack.mulPose(cameraRotation);

        Matrix4f matrix4f = poseStack.last().pose();

        float halfSize = size / 2.0F;

        float r = color.x();
        float g = color.y();
        float b = color.z();
        float a = color.w() * brightness;

        consumer.vertex(matrix4f, -halfSize, -halfSize, 0).color(r, g, b, a).endVertex();
        consumer.vertex(matrix4f, halfSize, -halfSize, 0).color(r, g, b, a).endVertex();
        consumer.vertex(matrix4f, halfSize, halfSize, 0).color(r, g, b, a).endVertex();
        consumer.vertex(matrix4f, -halfSize, halfSize, 0).color(r, g, b, a).endVertex();

        poseStack.popPose();
    }

    public static Vector3f lerpVector(Vector3f start, Vector3f end, float delta) {
        return new Vector3f(
                Mth.lerp(delta, start.x(), end.x()),
                Mth.lerp(delta, start.y(), end.y()),
                Mth.lerp(delta, start.z(), end.z())
        );
    }

    public static Vector4f lerpColor(Vector4f start, Vector4f end, float delta) {
        return new Vector4f(
                Mth.lerp(delta, start.x(), end.x()),
                Mth.lerp(delta, start.y(), end.y()),
                Mth.lerp(delta, start.z(), end.z()),
                Mth.lerp(delta, start.w(), end.w())
        );
    }

    public static float calculateFade(float progress, float fadeFraction) {
        if (progress < fadeFraction) {
            return progress / fadeFraction;
        } else if (progress > 1.0F - fadeFraction) {
            float ratio = (1.0F - progress) / fadeFraction;
            return ratio * ratio;
        }
        return 1.0F;
    }
}