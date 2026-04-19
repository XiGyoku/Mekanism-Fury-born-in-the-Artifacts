package XiGyoku.furyborn.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import net.minecraft.client.Minecraft;

public class TargetMarkRenderUtil {

    private static final ResourceLocation Z_MARK_TEXTURE = new ResourceLocation("furyborn", "textures/effect/pointer/z_mark.png");

    public static void renderZMark(PoseStack poseStack, float size, float alpha) {
        if (alpha <= 0.01F) return;

        poseStack.pushPose();

        Quaternionf cameraRotation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        poseStack.mulPose(cameraRotation);

        poseStack.scale(size, size, size);
        Matrix4f matrix4f = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, Z_MARK_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix4f, -0.5F, -0.5F, 0.0F).uv(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        bufferbuilder.vertex(matrix4f, 0.5F, -0.5F, 0.0F).uv(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        bufferbuilder.vertex(matrix4f, 0.5F, 0.5F, 0.0F).uv(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        bufferbuilder.vertex(matrix4f, -0.5F, 0.5F, 0.0F).uv(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}