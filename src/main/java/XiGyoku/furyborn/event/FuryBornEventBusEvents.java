package XiGyoku.furyborn.event;

import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.entity.FuryBornEntityTypes;
import XiGyoku.furyborn.entity.RobyteEntity;
import XiGyoku.furyborn.entity.RobyteLaserEntity;
import XiGyoku.furyborn.item.ItemBusterThrower;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Furyborn.MODID)
public class FuryBornEventBusEvents {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            boolean isClient = player.level().isClientSide;

            boolean isHolding = player.getMainHandItem().getItem() instanceof ItemBusterThrower ||
                    player.getOffhandItem().getItem() instanceof ItemBusterThrower;

            List<RobyteLaserEntity> lasers = player.level().getEntitiesOfClass(
                    RobyteLaserEntity.class,
                    player.getBoundingBox().inflate(20.0D),
                    laser -> laser.getOwner() == player && laser.isOvercharge()
            );

            for (RobyteLaserEntity laser : lasers) {
                if (!isHolding || !player.isAlive()) {
                    if (!isClient) {
                        laser.discard();
                    }
                } else {
                    net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                    double targetX = player.getX() + look.x * 10.0D;
                    double targetY = (player.getEyeY() - 0.2D) + look.y * 10.0D;
                    double targetZ = player.getZ() + look.z * 10.0D;

                    double moveLerp = 0.4D;
                    float rotLerp = 0.4F;

                    laser.setPos(
                            net.minecraft.util.Mth.lerp(moveLerp, laser.getX(), targetX),
                            net.minecraft.util.Mth.lerp(moveLerp, laser.getY(), targetY),
                            net.minecraft.util.Mth.lerp(moveLerp, laser.getZ(), targetZ)
                    );
                    laser.setXRot(net.minecraft.util.Mth.rotLerp(rotLerp, laser.getXRot(), player.getXRot()));
                    laser.setYRot(net.minecraft.util.Mth.rotLerp(rotLerp, laser.getYRot(), player.getYRot()));
                    if (!isClient) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                                5, 3, false, false, false
                        ));
                    }
                }
                break;
            }
        }
    }
}