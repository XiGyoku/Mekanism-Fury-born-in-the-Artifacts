package XiGyoku.furyborn.event;

import XiGyoku.furyborn.entity.BikeAnimState;
import XiGyoku.furyborn.entity.RoadBikeBitEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "furyborn")
public class BikeEvents {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity target = event.getEntity();
        if (target instanceof Player player && player.getVehicle() instanceof RoadBikeBitEntity bike) {
            BikeAnimState state = bike.getBikeState();
            if (state == BikeAnimState.REBELLION_START || state == BikeAnimState.REBELLION_LOOP) {
                event.setCanceled(true);
            }
        }
        else if (target instanceof RoadBikeBitEntity bike) {
            BikeAnimState state = bike.getBikeState();
            if (state == BikeAnimState.REBELLION_START || state == BikeAnimState.REBELLION_LOOP) {
                event.setCanceled(true);
            }
        }
    }
}