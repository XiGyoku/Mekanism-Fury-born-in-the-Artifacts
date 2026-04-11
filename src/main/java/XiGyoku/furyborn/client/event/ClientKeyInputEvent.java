package XiGyoku.furyborn.client.event;

import XiGyoku.furyborn.item.ItemBusterThrower;
import XiGyoku.furyborn.network.FuryBornNetwork;
import XiGyoku.furyborn.network.PacketToggleBusterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "furyborn", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientKeyInputEvent {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        while (FuryBornModClientEvents.TOGGLE_BUSTER_MODE.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                
                if (mainHand.getItem() instanceof ItemBusterThrower || offHand.getItem() instanceof ItemBusterThrower) {
                    FuryBornNetwork.CHANNEL.sendToServer(new PacketToggleBusterMode());
                }
            }
        }
    }
}