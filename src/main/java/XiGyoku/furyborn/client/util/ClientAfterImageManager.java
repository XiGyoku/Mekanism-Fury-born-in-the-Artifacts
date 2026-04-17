package XiGyoku.furyborn.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "furyborn", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientAfterImageManager {
    public static final Map<UUID, List<AfterImageData>> PAST_POSITIONS = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            UUID uuid = player.getUUID();
            if (player.getPersistentData().getBoolean("ExolumenAfterImage")) {
                PAST_POSITIONS.putIfAbsent(uuid, new ArrayList<>());
                List<AfterImageData> list = PAST_POSITIONS.get(uuid);

                float limbSwing = player.walkAnimation.position();
                float limbSwingAmount = player.walkAnimation.speed();
                float ageInTicks = player.tickCount;
                float netHeadYaw = player.getYHeadRot() - player.yBodyRot;
                float headPitch = player.getXRot();
                float bodyYaw = player.yBodyRot;
                boolean swinging = player.swinging;
                int swingTime = player.swingTime;

                list.add(0, new AfterImageData(player.position(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, bodyYaw, swinging, swingTime));

                if (list.size() > 10) {
                    list.remove(list.size() - 1);
                }
            } else {
                if (PAST_POSITIONS.containsKey(uuid)) {
                    List<AfterImageData> list = PAST_POSITIONS.get(uuid);
                    if (!list.isEmpty()) {
                        list.remove(list.size() - 1);
                    } else {
                        PAST_POSITIONS.remove(uuid);
                    }
                }
            }
        }
    }
}