package XiGyoku.furyborn.entity.client;

import XiGyoku.furyborn.effect.FuryBornEffects;
import XiGyoku.furyborn.entity.RobyteEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class RobyteAreaEntity extends Entity {
    private UUID bossId = null;

    public RobyteAreaEntity(EntityType<?> pEntityType, Level pLevel)
    {
        super(pEntityType, pLevel);
        this.noCulling = true;
    }

    public void setRobyte(RobyteEntity robyte) {
        this.bossId = robyte.getUUID();
    }

    public boolean isPlayerInsideArea(Player player) {
        double radius = 64.0;
        double height = 64.0;
        double minX = this.getX() - radius;
        double maxX = this.getX() + radius;
        double minY = this.getY();
        double maxY = this.getY() + height;
        double minZ = this.getZ() - radius;
        double maxZ = this.getZ() + radius;

        return player.getX() >= minX && player.getX() <= maxX &&
                player.getY() >= minY && player.getY() <= maxY &&
                player.getZ() >= minZ && player.getZ() <= maxZ;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.bossId != null) {
                Entity boss = serverLevel.getEntity(this.bossId);
                if (boss != null) {
                    if (boss.isRemoved() || !boss.isAlive()) {
                        clearMonitoredFromAll(serverLevel);
                        this.discard();
                        return;
                    }
                }
            } else {
                if (this.tickCount > 10) {
                    this.discard();
                    return;
                }
            }

            for (Player player : serverLevel.players()) {
                if (player.isAlive() && !player.isCreative() && !player.isSpectator()) {
                    boolean isInsideArea = isPlayerInsideArea(player);
                    if (isInsideArea) {
                        if (!player.hasEffect(FuryBornEffects.MONITORED.get())) {
                            player.addEffect(new MobEffectInstance(FuryBornEffects.MONITORED.get(), -1, 0, false, false, true));
                        }
                    }
                    else if (player.hasEffect(FuryBornEffects.MONITORED.get())) {
                        boolean isSafeInAnotherArea = false;
                        List<RobyteAreaEntity> otherAreas = serverLevel.getEntitiesOfClass(
                                RobyteAreaEntity.class,
                                player.getBoundingBox().inflate(128.0D)
                        );

                        for (RobyteAreaEntity otherArea : otherAreas) {
                            if (otherArea.isPlayerInsideArea(player)) {
                                isSafeInAnotherArea = true;
                                break;
                            }
                        }
                        if (!isSafeInAnotherArea) {
                            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
                            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
                            if (player.tickCount % 20 == 0) {
                                player.hurt(serverLevel.damageSources().magic(), 2.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    private void clearMonitoredFromAll(ServerLevel level) {
        for (Player player : level.players()) {
            if (player.hasEffect(FuryBornEffects.MONITORED.get())) {
                player.removeEffect(FuryBornEffects.MONITORED.get());
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("BossId")) {
            this.bossId = compoundTag.getUUID("BossId");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.bossId != null) {
            compoundTag.putUUID("BossId", this.bossId);
        }
    }
}
