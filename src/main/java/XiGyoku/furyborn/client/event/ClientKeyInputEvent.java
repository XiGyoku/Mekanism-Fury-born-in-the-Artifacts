package XiGyoku.furyborn.client.event;

import XiGyoku.furyborn.entity.RoadBikeBitEntity;
import XiGyoku.furyborn.item.HaloOfExolumenItem;
import XiGyoku.furyborn.item.ItemBusterThrower;
import XiGyoku.furyborn.item.SunRaiserDriveItem;
import XiGyoku.furyborn.item.SystemXrossAliveItem;
import XiGyoku.furyborn.network.FuryBornNetwork;
import XiGyoku.furyborn.network.PacketBikeRebellion;
import XiGyoku.furyborn.network.PacketDirectionalDash;
import XiGyoku.furyborn.network.PacketShootLaserBit;
import XiGyoku.furyborn.network.PacketTargetTeleport;
import XiGyoku.furyborn.network.PacketToggleAfterImage;
import XiGyoku.furyborn.network.PacketToggleBusterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "furyborn", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientKeyInputEvent {

    private static boolean wasRebellionPressed = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && mc.player.getVehicle() instanceof RoadBikeBitEntity bike) {
            boolean isPressed = FuryBornModClientEvents.TOGGLE_REBELLION.isDown();
            if (isPressed != wasRebellionPressed) {
                wasRebellionPressed = isPressed;
                bike.isRebellionKeyPressed = isPressed;
                FuryBornNetwork.CHANNEL.sendToServer(new PacketBikeRebellion(isPressed));
            }
        }

        while (FuryBornModClientEvents.TOGGLE_BUSTER_MODE.consumeClick()) {
            Player player = mc.player;
            if (player != null) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();

                if (mainHand.getItem() instanceof ItemBusterThrower || offHand.getItem() instanceof ItemBusterThrower) {
                    FuryBornNetwork.CHANNEL.sendToServer(new PacketToggleBusterMode());
                }
            }
        }
        while (FuryBornModClientEvents.SHOOT_LASER_BIT.consumeClick()) {
            Player player = mc.player;
            if (player != null) {
                if (CuriosApi.getCuriosHelper().findEquippedCurio(stack -> stack.getItem() instanceof HaloOfExolumenItem, player).isPresent()) {
                    FuryBornNetwork.CHANNEL.sendToServer(new PacketShootLaserBit());
                }
            }
        }
        while (FuryBornModClientEvents.TOGGLE_AFTERIMAGE.consumeClick()) {
            Player player = mc.player;
            if (player != null) {
                if (CuriosApi.getCuriosHelper().findEquippedCurio(stack -> stack.getItem() instanceof SunRaiserDriveItem, player).isPresent()) {
                    boolean currentState = player.getPersistentData().getBoolean("ExolumenAfterImage");
                    player.getPersistentData().putBoolean("ExolumenAfterImage", !currentState);
                    FuryBornNetwork.CHANNEL.sendToServer(new PacketToggleAfterImage());
                }
            }
        }
        while (FuryBornModClientEvents.DRIVESHIFT_DASH.consumeClick()) {
            Player player = mc.player;
            if (player != null && player.getPersistentData().getBoolean("ExolumenAfterImage")) {
                int[] driveCount = {0};
                int[] xrossCount = {0};
                CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(handler -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack curioStack = handler.getStackInSlot(i);
                        if (curioStack.getItem() instanceof SunRaiserDriveItem) driveCount[0] += curioStack.getCount();
                        if (curioStack.getItem() instanceof SystemXrossAliveItem) xrossCount[0] += curioStack.getCount();
                    }
                });

                if (driveCount[0] >= 2 && xrossCount[0] >= 1) {
                    boolean f = mc.options.keyUp.isDown();
                    boolean b = mc.options.keyDown.isDown();
                    boolean l = mc.options.keyLeft.isDown();
                    boolean r = mc.options.keyRight.isDown();
                    boolean u = mc.options.keyJump.isDown();
                    boolean d = mc.options.keyShift.isDown();
                    player.getPersistentData().putInt("DriveshiftGreenTicks", 40);
                    FuryBornNetwork.CHANNEL.sendToServer(new PacketDirectionalDash(f, b, l, r, u, d));
                }
            }
        }
        while (FuryBornModClientEvents.DRIVESHIFT_BACKSTAB.consumeClick()) {
            Player player = mc.player;
            if (player != null && player.getPersistentData().getBoolean("ExolumenAfterImage")) {
                int[] driveCount = {0};
                int[] xrossCount = {0};
                CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(handler -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack curioStack = handler.getStackInSlot(i);
                        if (curioStack.getItem() instanceof SunRaiserDriveItem) driveCount[0] += curioStack.getCount();
                        if (curioStack.getItem() instanceof SystemXrossAliveItem) xrossCount[0] += curioStack.getCount();
                    }
                });

                if (driveCount[0] >= 2 && xrossCount[0] >= 1) {
                    player.getPersistentData().putInt("DriveshiftGreenTicks", 40);
                    FuryBornNetwork.CHANNEL.sendToServer(new PacketTargetTeleport());
                }
            }
        }
    }
}