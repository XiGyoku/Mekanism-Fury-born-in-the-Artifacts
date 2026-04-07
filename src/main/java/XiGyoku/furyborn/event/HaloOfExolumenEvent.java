package XiGyoku.furyborn.event;

import XiGyoku.furyborn.item.HaloOfExolumenItem;
import XiGyoku.furyborn.sound.FuryBornSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "furyborn", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HaloOfExolumenEvent {
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (CuriosApi.getCuriosHelper().findEquippedCurio(stack -> stack.getItem() instanceof HaloOfExolumenItem, entity).isPresent()) {
            DamageSource source = event.getSource();

            if (source.getDirectEntity() instanceof Projectile) {
                event.setCanceled(true);
                return;
            }

            if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (CuriosApi.getCuriosHelper().findEquippedCurio(stack -> stack.getItem() instanceof HaloOfExolumenItem, entity).isPresent()) {
            event.setAmount(event.getAmount() * 0.5F);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        CuriosApi.getCuriosHelper().findFirstCurio(entity, stack -> stack.getItem() instanceof HaloOfExolumenItem).ifPresent(curio -> {
            event.setCanceled(true);
            entity.setHealth(entity.getMaxHealth());
            entity.removeAllEffects();
            for (int i = 0; i < 20; i++) {
                entity.level().addParticle(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY(), entity.getZ(), 0.0D, 0.0D, 0.0D);
            }
            entity.level().playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    FuryBornSounds.ROBYTE_BEAMEND.get(),
                    SoundSource.PLAYERS,
                    2.0F, 1.0F
            );
        });
    }
}
