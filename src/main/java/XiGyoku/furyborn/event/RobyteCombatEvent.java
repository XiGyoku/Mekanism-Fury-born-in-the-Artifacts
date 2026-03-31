package XiGyoku.furyborn.event;

import XiGyoku.furyborn.effect.FuryBornEffects;
import XiGyoku.furyborn.entity.RobyteEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "furyborn", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RobyteCombatEvent {
    @SubscribeEvent
    public static void onPlayerHurtByRobyte(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getSource().getEntity() instanceof RobyteEntity robyte) {
                if (!robyte.isDeadOrDying()) {
                    if (!player.hasEffect(FuryBornEffects.MONITORED.get())) {
                        player.addEffect(new MobEffectInstance(FuryBornEffects.MONITORED.get(), -1, 0, false, false, true));
                    }
                }
            }
        }
    }
}