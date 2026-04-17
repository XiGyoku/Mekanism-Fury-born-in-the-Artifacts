package XiGyoku.furyborn.client.event;

import XiGyoku.furyborn.client.util.AfterImageData;
import XiGyoku.furyborn.client.util.ClientAfterImageManager;
import XiGyoku.furyborn.client.util.ColorShiftingBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "furyborn", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientAfterImageRenderEvent {
    private static boolean isRenderingAfterImage = false;

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (isRenderingAfterImage) return;

        Player player = event.getEntity();
        float fadeProgress = player.getPersistentData().contains("DriveshiftFadeProgress") ? player.getPersistentData().getFloat("DriveshiftFadeProgress") : 0.0f;
        if (fadeProgress <= 0.0F) return;

        int greenTicks = player.getPersistentData().contains("DriveshiftGreenTicks") ? player.getPersistentData().getInt("DriveshiftGreenTicks") : 0;
        float r = 1.0F, g = 0.6F, b = 0.6F, mainAlpha = 1.0F, afterImageAlphaMult = 1.0F;

        if (greenTicks > 0) {
            float ratio = Math.min(1.0f, greenTicks / 10.0f);
            r = Mth.lerp(ratio, 1.0F, 0.0F);
            g = Mth.lerp(ratio, 0.6F, 1.0F);
            b = Mth.lerp(ratio, 0.6F, 0.0F);
            mainAlpha = Mth.lerp(ratio, 1.0F, 0.05F);
            afterImageAlphaMult = Mth.lerp(ratio, 1.0F, 0.1F);
        }

        isRenderingAfterImage = true;
        try {
            event.setCanceled(true);

            float oldYBodyRot = player.yBodyRot;
            float oldYBodyRotO = player.yBodyRotO;
            float oldYRot = player.getYRot();
            float oldYRotO = player.yRotO;
            float oldXRot = player.getXRot();
            float oldXRotO = player.xRotO;
            float oldYHeadRot = player.yHeadRot;
            float oldYHeadRotO = player.yHeadRotO;
            boolean oldSwinging = player.swinging;
            int oldSwingTime = player.swingTime;

            List<AfterImageData> past = ClientAfterImageManager.PAST_POSITIONS.get(player.getUUID());
            if (past != null && !past.isEmpty()) {
                double lerpX = Mth.lerp(event.getPartialTick(), player.xo, player.getX());
                double lerpY = Mth.lerp(event.getPartialTick(), player.yo, player.getY());
                double lerpZ = Mth.lerp(event.getPartialTick(), player.zo, player.getZ());

                for (int i = 3; i < Math.min(past.size(), 10); i += 3) {
                    AfterImageData data = past.get(i);
                    float alpha = (0.6F - (i * 0.03F)) * fadeProgress * afterImageAlphaMult;
                    if (alpha <= 0.0F) continue;

                    PoseStack ps = event.getPoseStack();
                    ps.pushPose();
                    ps.translate(data.position.x - lerpX, data.position.y - lerpY, data.position.z - lerpZ);

                    applyPose(player, data);
                    MultiBufferSource buf = new ColorShiftingBufferSource(event.getMultiBufferSource(), r, g, b, alpha);
                    event.getRenderer().render((AbstractClientPlayer) player, data.bodyYaw, event.getPartialTick(), ps, buf, 15728880);
                    ps.popPose();
                }
            }

            player.yBodyRot = oldYBodyRot;
            player.yBodyRotO = oldYBodyRotO;
            player.setYRot(oldYRot);
            player.yRotO = oldYRotO;
            player.setXRot(oldXRot);
            player.xRotO = oldXRotO;
            player.yHeadRot = oldYHeadRot;
            player.yHeadRotO = oldYHeadRotO;
            player.swinging = oldSwinging;
            player.swingTime = oldSwingTime;

            MultiBufferSource mainBuf = new ColorShiftingBufferSource(event.getMultiBufferSource(), Mth.lerp(fadeProgress, 1.0f, r), Mth.lerp(fadeProgress, 1.0f, g), Mth.lerp(fadeProgress, 1.0f, b), mainAlpha);
            event.getRenderer().render((AbstractClientPlayer) player, player.getYRot(), event.getPartialTick(), event.getPoseStack(), mainBuf, event.getPackedLight());

        } finally {
            isRenderingAfterImage = false;
        }
    }

    private static void applyPose(Player p, AfterImageData d) {
        p.yBodyRot = d.bodyYaw;
        p.yBodyRotO = d.bodyYaw;
        p.setYRot(d.netHeadYaw + d.bodyYaw);
        p.yRotO = p.getYRot();
        p.setXRot(d.headPitch);
        p.xRotO = d.headPitch;
        p.yHeadRot = p.getYRot();
        p.yHeadRotO = p.getYRot();
        p.swinging = d.swinging;
        p.swingTime = d.swingTime;
    }
}