package XiGyoku.furyborn.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class RobyteBitLaserEntity extends Entity {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(RobyteLaserEntity.class, EntityDataSerializers.FLOAT);
    private Vec3 startPos;
    private Vec3 targetPos;
    private int lifeTicks = 0;
    private final int moveDuration = 30;
    private final int stopDuration = 200;

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    private LivingEntity targetEntity;

    public RobyteBitLaserEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DAMAGE, 1.0F);
    }

    public void setOwner(Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setDamage(float damage) { this.entityData.set(DAMAGE, damage); }

    public float getDamage() { return this.entityData.get(DAMAGE); }

    public void setTarget(LivingEntity target) {
        this.targetEntity = target;
    }

    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
            return this.cachedOwner;
        }
        return null;
    }

    @Override
    public void tick() {
        this.xo = this.getX(); this.yo = this.getY(); this.zo = this.getZ();
        this.xRotO = this.getXRot(); this.yRotO = this.getYRot();
        super.tick();
        if (this.startPos == null) this.startPos = this.position();
        if (this.targetPos == null) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            this.targetPos = this.startPos.add(Math.cos(angle) * 5.0, (this.random.nextDouble() * 4.0 - 2.0), Math.sin(angle) * 5.0);
        }
        if (lifeTicks < moveDuration) {
            smoothLookAtTarget();
            float t = (float) lifeTicks / moveDuration;
            double curX = Mth.lerp(t, this.startPos.x, this.targetPos.x);
            double curZ = Mth.lerp(t, this.startPos.z, this.targetPos.z);
            double curY = Mth.lerp(t, this.startPos.y, this.targetPos.y) + Math.sin(t * Math.PI) * 2.0;
            this.setPos(curX, curY, curZ);
        }
        if (!this.level().isClientSide && lifeTicks == moveDuration) {
            this.playSound(XiGyoku.furyborn.sound.FuryBornSounds.ROBYTE_BEAMSTART.get(), 1.0F, 1.0F);
        }
        if (!this.level().isClientSide && lifeTicks == (moveDuration + 20)) {
            RobyteLaserEntity laser = new RobyteLaserEntity(FuryBornEntityTypes.ROBYTE_LASER.get(), this.level());
            double spawnY = this.getY() + 0.30D;
            laser.setPos(this.getX(), spawnY, this.getZ());

            laser.setYRot(this.getYRot());
            laser.setXRot(this.getXRot());
            laser.yRotO = this.getYRot();
            laser.xRotO = this.getXRot();

            laser.setRadius(0.15F);
            laser.setMaxLife(200);
            laser.setDamage(this.getDamage());
            laser.setOwner(this);
            this.level().addFreshEntity(laser);
        }

        if (lifeTicks >= moveDuration + stopDuration) {
            if (!this.level().isClientSide) this.discard();
        }
        lifeTicks++;
    }

    private void smoothLookAtTarget() {
        if (this.targetEntity != null && this.targetEntity.isAlive()) {
            Vec3 pivot = new Vec3(this.getX(), this.getY() + 0.30D, this.getZ());
            Vec3 diff = this.targetEntity.getEyePosition().subtract(pivot);

            float targetYaw = (float) (-Mth.atan2(diff.x, diff.z) * (180F / Math.PI));
            float targetPitch = (float) (-Mth.atan2(diff.y, diff.horizontalDistance()) * (180F / Math.PI));

            this.setYRot(Mth.rotLerp(0.15F, this.getYRot(), targetYaw));
            this.setXRot(Mth.lerp(0.15F, this.getXRot(), targetPitch));
        }
    }

    @Override protected void readAdditionalSaveData(CompoundTag nbt) {
        lifeTicks = nbt.getInt("LifeTicks");
        if (nbt.contains("Damage")) setDamage(nbt.getFloat("Damage"));
        if (nbt.hasUUID("Owner")) {
            this.ownerUUID = nbt.getUUID("Owner");
        }
    }
    @Override protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("LifeTicks", lifeTicks);
        nbt.putFloat("Damage", getDamage());
        if (this.ownerUUID != null) {
            nbt.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(100.0D);
    }
}