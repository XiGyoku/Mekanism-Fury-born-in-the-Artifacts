package XiGyoku.furyborn.entity.AI;

import XiGyoku.furyborn.entity.RobyteEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.sounds.SoundEvents;
import java.util.EnumSet;

public class RobyteAttackGoal extends Goal {
    private final RobyteEntity mob;

    public RobyteAttackGoal(RobyteEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mob.getTarget() != null && mob.attackCooldown <= 0
                && mob.getAttackTick() == 0 && mob.getCannonTick() == 0 && mob.getTransamTick() == 0
                && mob.getAllRangeTick() == 0
                && mob.phaseTransitionTick == 0
                && !mob.hasEnteredFinalPhase();
    }

    @Override
    public void start() {
        mob.actionCount++;
        if (mob.actionCount % 5 == 0 && mob.transamCooldown <= 0) {
            mob.setTransamTick(1);
            mob.setTransamMode(true);
            mob.transamCooldown = 400;
        } else {
            boolean canUseAllRange = mob.getHealth() <= mob.getMaxHealth() * 0.7F && !mob.hasEnteredFinalPhase();
            int maxRandom = canUseAllRange ? 3 : 2;
            int r = mob.getRandom().nextInt(maxRandom);

            if (r == 0) {
                mob.setAttackTick(1);
            } else if (r == 1) {
                mob.setCannonTick(1);
            } else {
                mob.setAllRangeTick(1);
            }
        }
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        int tTick = mob.getTransamTick();
        int aTick = mob.getAttackTick();

        if (tTick > 0) {
            if (tTick <= 120) {
                mob.getNavigation().stop();
                if (mob.hurtTime == 0) {
                    mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.5D, 0.5D, 0.5D));
                }
                mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0D);
            } else {
                int spinTick = tTick - 120;
                if (spinTick > mob.ROTATION_START_DUR && spinTick <= mob.ROTATION_START_DUR + mob.ROTATION_LOOP_DUR) {
                    double dx = target.getX() - mob.getX();
                    double dy = (target.getY() + target.getEyeHeight() / 2.0D) - mob.getY();
                    double dz = target.getZ() - mob.getZ();
                    float targetYaw = (float)(Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
                    mob.setYRot(targetYaw);
                    mob.yHeadRot = targetYaw;
                    mob.yBodyRot = targetYaw;
                    net.minecraft.world.phys.Vec3 dashVec = new net.minecraft.world.phys.Vec3(dx, dy, dz).normalize().scale(0.6D);
                    if (mob.hurtTime == 0) {
                        mob.setDeltaMovement(dashVec);
                    }

                    if (spinTick % 5 == 0) {
                        mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(2.0D)).forEach(entity -> {
                            if (entity != mob && entity.isAlive()) {
                                entity.invulnerableTime = 0;
                                entity.hurtTime = 0;

                                if (entity instanceof Player player && player.isBlocking()) {
                                    player.disableShield(true);
                                }

                                float damage = mob.isRebellion() ? Float.MAX_VALUE : (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                                boolean hasHit = entity.hurt(mob.damageSources().mobAttack(mob), damage);

                                if (!hasHit) {
                                    hasHit = entity.hurt(mob.damageSources().indirectMagic(mob, mob), damage);
                                }

                                if (hasHit) {
                                    mob.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.5F);
                                    double d0 = entity.getX() - mob.getX();
                                    double d2 = entity.getZ() - mob.getZ();
                                    entity.knockback(0.4D, d0, d2);
                                }
                            }
                        });
                    }
                } else {
                    mob.getNavigation().stop();
                    if (mob.hurtTime == 0) {
                        mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.5D, 0.5D, 0.5D));
                    }
                    mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0D);
                }
            }
        } else if (aTick > mob.ROTATION_START_DUR && aTick <= mob.ROTATION_START_DUR + mob.ROTATION_LOOP_DUR) {
            double dx = target.getX() - mob.getX();
            double dy = (target.getY() + target.getEyeHeight() / 2.0D) - mob.getY();
            double dz = target.getZ() - mob.getZ();
            float targetYaw = (float)(Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
            mob.setYRot(targetYaw);
            mob.yHeadRot = targetYaw;
            mob.yBodyRot = targetYaw;
            net.minecraft.world.phys.Vec3 dashVec = new net.minecraft.world.phys.Vec3(dx, dy, dz).normalize().scale(0.3D);
            if (mob.hurtTime == 0) {
                mob.setDeltaMovement(dashVec);
            }

            if (aTick % 5 == 0) {
                mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(2.0D)).forEach(entity -> {
                    if (entity != mob && entity.isAlive()) {
                        float damage = mob.isRebellion() ? Float.MAX_VALUE : (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        boolean hasHit = entity.hurt(mob.damageSources().mobAttack(mob), damage);
                        if (hasHit) {
                            mob.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.5F);
                            double d0 = entity.getX() - mob.getX();
                            double d2 = entity.getZ() - mob.getZ();
                            entity.knockback(0.4D, d0, d2);
                        }
                    }
                });
            }
        }
        if (mob.getCannonTick() > 0 || mob.getAllRangeTick() > 0 || (aTick > 0 && (aTick <= mob.ROTATION_START_DUR || aTick > mob.ROTATION_START_DUR + mob.ROTATION_LOOP_DUR))) {
            mob.getNavigation().stop();
            if (mob.hurtTime == 0) {
                mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.5D, 0.5D, 0.5D));
            }
            mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0D);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return (mob.getAttackTick() > 0 || mob.getCannonTick() > 0 || mob.getTransamTick() > 0 || mob.getAllRangeTick() > 0) && mob.phaseTransitionTick == 0;
    }

    @Override
    public void stop() {
    }
}