package XiGyoku.furyborn.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import XiGyoku.furyborn.Furyborn;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public abstract class FontMixin {

    @Unique
    private static final String FURYBORN_MARKER = ":_FB_";

    @Unique
    private static final String FURYBORN_STARRY_MARKER = ":_FBS_";

    @Unique
    private static final ResourceLocation ZEN_OLD_MINCHO = new ResourceLocation(Furyborn.MODID, "zen_old_mincho");

    @Inject(method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I", at = @At("HEAD"), cancellable = true)
    private void furyborn$drawInBatchString(String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int packedLight, int backgroundColor, boolean p_273022_, CallbackInfoReturnable<Integer> cir) {
        if (text != null) {
            boolean hasFB = text.contains(FURYBORN_MARKER);
            boolean hasFBS = text.contains(FURYBORN_STARRY_MARKER);

            if (hasFB || hasFBS) {
                String cleanText = text.replace(FURYBORN_MARKER, "").replace(FURYBORN_STARRY_MARKER, "");
                Font font = (Font) (Object) this;
                int resultWidth = 0;

                if (hasFB) {
                    handleHaloText(cleanText, x, y, matrix, bufferSource, displayMode, packedLight);
                    resultWidth = 1;
                } else if (hasFBS) {
                    resultWidth = font.drawInBatch(cleanText, x, y, color, dropShadow, matrix, bufferSource, displayMode, packedLight, backgroundColor, p_273022_);
                }

                cir.setReturnValue(resultWidth);
                cir.cancel();
            }
        }
    }

    @Inject(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", at = @At("HEAD"), cancellable = true)
    private void furyborn$drawInBatchFormatted(FormattedCharSequence sequence, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int packedLight, int backgroundColor, CallbackInfoReturnable<Integer> cir) {
        if (sequence != null) {
            String text = formatSeqToString(sequence);
            boolean hasFB = text.contains(FURYBORN_MARKER);
            boolean hasFBS = text.contains(FURYBORN_STARRY_MARKER);

            if (hasFB || hasFBS) {
                String cleanText = text.replace(FURYBORN_MARKER, "").replace(FURYBORN_STARRY_MARKER, "");
                Font font = (Font) (Object) this;
                int resultWidth = 0;

                if (hasFB) {
                    handleHaloText(cleanText, x, y, matrix, bufferSource, displayMode, packedLight);
                    resultWidth = 1;
                } else if (hasFBS) {
                    resultWidth = font.drawInBatch(cleanText, x, y, color, dropShadow, matrix, bufferSource, displayMode, packedLight, backgroundColor);
                }

                cir.setReturnValue(resultWidth);
                cir.cancel();
            }
        }
    }

    @Inject(method = "drawInternal(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", at = @At("HEAD"), cancellable = true)
    private void furyborn$drawInternalFormatted(FormattedCharSequence sequence, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int packedLight, int backgroundColor, CallbackInfoReturnable<Integer> cir) {
        if (sequence != null) {
            String text = formatSeqToString(sequence);
            boolean hasFB = text.contains(FURYBORN_MARKER);
            boolean hasFBS = text.contains(FURYBORN_STARRY_MARKER);

            if (hasFB || hasFBS) {
                String cleanText = text.replace(FURYBORN_MARKER, "").replace(FURYBORN_STARRY_MARKER, "");
                Font font = (Font) (Object) this;
                int resultWidth = 0;

                if (hasFB) {
                    handleHaloText(cleanText, x, y, matrix, bufferSource, displayMode, packedLight);
                    resultWidth = 1;
                } else if (hasFBS) {
                    resultWidth = font.drawInBatch(cleanText, x, y, color, dropShadow, matrix, bufferSource, displayMode, packedLight, backgroundColor);
                }

                cir.setReturnValue(resultWidth);
                cir.cancel();
            }
        }
    }

    @Inject(method = "renderText(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F", at = @At("HEAD"), cancellable = true)
    private void furyborn$renderFormatted(FormattedCharSequence sequence, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int packedLight, int backgroundColor, CallbackInfoReturnable<Float> cir) {
        if (sequence != null) {
            String text = formatSeqToString(sequence);
            boolean hasFB = text.contains(FURYBORN_MARKER);
            boolean hasFBS = text.contains(FURYBORN_STARRY_MARKER);

            if (hasFB || hasFBS) {
                String cleanText = text.replace(FURYBORN_MARKER, "").replace(FURYBORN_STARRY_MARKER, "");
                Font font = (Font) (Object) this;
                float resultWidth = 0.0f;

                if (hasFB) {
                    handleHaloText(cleanText, x, y, matrix, bufferSource, displayMode, packedLight);
                    resultWidth = 1.0f;
                } else if (hasFBS) {
                    resultWidth = (float) font.width(cleanText);
                }

                cir.setReturnValue(resultWidth);
                cir.cancel();
            }
        }
    }

    @Unique
    private String formatSeqToString(FormattedCharSequence sequence) {
        StringBuilder sb = new StringBuilder();
        sequence.accept((i, style, codepoint) -> {
            sb.appendCodePoint(codepoint);
            return true;
        });
        return sb.toString();
    }

    @Unique
    private void handleHaloText(String str, float x, float y, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int packedLight) {
        Font font = (Font) (Object) this;
        long time = System.currentTimeMillis() / 50;

        float currentX = x - 1.0F;
        float currentY = y + 2.0F;

        int fullLight = 15728880;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Style baseStyle = Style.EMPTY.withFont(ZEN_OLD_MINCHO);

        for (int i = 0; i < str.length(); i++) {
            String charStr = String.valueOf(str.charAt(i));
            double wave = (Math.sin((time / 6.0) - (i * 0.4)) + 1.0) / 2.0;
            int r = (int) (0x66 + (0x99 * wave));
            int g = 0xFF;
            int b = (int) (0x66 + (0x99 * wave));
            int animatedColor = (0xFF << 24) | (r << 16) | (g << 8) | b;

            Style charStyle = baseStyle.withColor(net.minecraft.network.chat.TextColor.fromRgb(animatedColor & 0xFFFFFF));
            FormattedCharSequence charSeq = Component.literal(charStr).withStyle(charStyle).getVisualOrderText();

            font.drawInBatch(charSeq, currentX, currentY, animatedColor, false, matrix, bufferSource, displayMode, 0, fullLight);

            currentX += font.width(Component.literal(charStr).withStyle(baseStyle));
        }
    }
}