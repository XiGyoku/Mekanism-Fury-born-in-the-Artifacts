package XiGyoku.furyborn.event;

import XiGyoku.furyborn.item.SunRaiserDriveItem;
import XiGyoku.furyborn.item.SystemXrossAliveItem;
import XiGyoku.furyborn.sound.FuryBornSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = "furyborn")
public class PlayerDriveshiftEvent {

    private static final UUID DRIVESHIFT_SPEED_ID = UUID.fromString("1f2d3e4c-5b6a-7f8e-9d0c-1b2a3f4e5d6c");
    private static final UUID DRIVESHIFT_DAMAGE_ID = UUID.fromString("2f3d4e5c-6b7a-8f9e-0d1c-2b3a4f5e6d7c");
    private static final UUID DRIVESHIFT_ARMOR_ID = UUID.fromString("3f4d5e6c-7b8a-9f0e-1d2c-3b4a5f6e7d8c");
    private static final String DRIVESHIFT_FADE_PROGRESS = "DriveshiftFadeProgress";

    public static int getDriveCount(Player player) {
        AtomicInteger count = new AtomicInteger(0);
        CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(handler -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.getItem() instanceof SunRaiserDriveItem) count.addAndGet(stack.getCount());
            }
        });
        return count.get();
    }

    public static int getXrossCount(Player player) {
        AtomicInteger count = new AtomicInteger(0);
        CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(handler -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.getItem() instanceof SystemXrossAliveItem) count.addAndGet(stack.getCount());
            }
        });
        return count.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        boolean isActive = player.getPersistentData().getBoolean("ExolumenAfterImage");

        int driveCount = getDriveCount(player);
        int xrossCount = getXrossCount(player);

        if (isActive && driveCount == 0) {
            player.getPersistentData().putBoolean("ExolumenAfterImage", false);
            isActive = false;
        }

        float progress = player.getPersistentData().contains(DRIVESHIFT_FADE_PROGRESS) ? player.getPersistentData().getFloat(DRIVESHIFT_FADE_PROGRESS) : 0.0f;
        if (isActive) {
            progress = Math.min(1.0f, progress + 0.1f);
        } else {
            progress = Math.max(0.0f, progress - 0.05f);
        }
        player.getPersistentData().putFloat(DRIVESHIFT_FADE_PROGRESS, progress);

        if (player.getPersistentData().contains("DriveshiftGreenTicks")) {
            int greenTicks = player.getPersistentData().getInt("DriveshiftGreenTicks");
            if (greenTicks > 0) player.getPersistentData().putInt("DriveshiftGreenTicks", greenTicks - 1);
        }

        double speedMod = (driveCount >= 2) ? 4.0 : 2.0;
        double combatMod = (driveCount >= 2) ? (xrossCount >= 1 ? 8.0 : 4.0) : 2.0;

        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);

        if (isActive) {
            player.resetAttackStrengthTicker();
            updateModifier(speed, DRIVESHIFT_SPEED_ID, speedMod);
            updateModifier(damage, DRIVESHIFT_DAMAGE_ID, combatMod);
            updateModifier(armor, DRIVESHIFT_ARMOR_ID, combatMod);
        } else {
            if (speed != null) speed.removeModifier(DRIVESHIFT_SPEED_ID);
            if (damage != null) damage.removeModifier(DRIVESHIFT_DAMAGE_ID);
            if (armor != null) armor.removeModifier(DRIVESHIFT_ARMOR_ID);
        }
    }

    private static void updateModifier(AttributeInstance attr, UUID uuid, double amount) {
        if (attr == null) return;
        AttributeModifier existing = attr.getModifier(uuid);
        if (existing == null || existing.getAmount() != amount) {
            attr.removeModifier(uuid);
            attr.addTransientModifier(new AttributeModifier(uuid, "Driveshift Boost", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getDirectEntity() instanceof Player player) {
            if (player.level().isClientSide) return;

            if (player.getPersistentData().getBoolean("ExolumenAfterImage")) {
                if (player.getPersistentData().getBoolean("DriveshiftMultiHitting")) return;

                int driveCount = getDriveCount(player);
                int xrossCount = getXrossCount(player);

                int extraHits = (driveCount >= 2) ? (xrossCount >= 1 ? 8 : 4) : 2;

                float damage = event.getAmount();
                LivingEntity target = event.getEntity();

                player.getPersistentData().putBoolean("DriveshiftMultiHitting", true);
                try {
                    for (int i = 0; i < extraHits; i++) {
                        target.invulnerableTime = 0;
                        target.hurt(event.getSource(), damage);
                    }
                    target.invulnerableTime = 0;
                } finally {
                    player.getPersistentData().putBoolean("DriveshiftMultiHitting", false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && player.getPersistentData().getBoolean("ExolumenAfterImage")) {
            if (player.level().random.nextFloat() < 0.9F) {
                event.setCanceled(true);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), FuryBornSounds.ROBYTE_TELEPORT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Projectile projectile && !event.getLevel().isClientSide()) {
            if (projectile.getOwner() instanceof Player player && player.getPersistentData().getBoolean("ExolumenAfterImage")) {
                if (projectile.getPersistentData().getBoolean("DriveshiftCloned")) return;
                projectile.getPersistentData().putBoolean("DriveshiftCloned", true);

                int driveCount = getDriveCount(player);
                int xrossCount = getXrossCount(player);
                int extra = (driveCount >= 2) ? (xrossCount >= 1 ? 8 : 4) : 2;

                for (int i = 0; i < extra; i++) {
                    Entity clone = projectile.getType().create(event.getLevel());
                    if (clone instanceof Projectile clonedProj) {
                        clonedProj.copyPosition(projectile);
                        clonedProj.setOwner(player);
                        Vec3 delta = projectile.getDeltaMovement();
                        clonedProj.setDeltaMovement(delta.add(
                                (player.level().random.nextDouble() - 0.5) * 0.2,
                                (player.level().random.nextDouble() - 0.5) * 0.2,
                                (player.level().random.nextDouble() - 0.5) * 0.2
                        ));
                        clonedProj.getPersistentData().putBoolean("DriveshiftCloned", true);
                        event.getLevel().addFreshEntity(clonedProj);
                    }
                }
            }
        }
    }
}