package XiGyoku.furyborn.entity;

import XiGyoku.furyborn.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import XiGyoku.furyborn.sound.FuryBornSounds;

import javax.annotation.Nullable;
import java.util.UUID;

public class RobyteLaserEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(RobyteLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> MAX_LIFE = SynchedEntityData.defineId(RobyteLaserEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(RobyteLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> EXPLOSIVE = SynchedEntityData.defineId(RobyteLaserEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> OVERCHARGE = SynchedEntityData.defineId(RobyteLaserEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DO_BAD_ATTACK = SynchedEntityData.defineId(RobyteLaserEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable private UUID ownerUUID;
    @Nullable private Entity cachedOwner;

    private boolean isMuted = false;

    public void setMuted(boolean muted) { this.isMuted = muted; }

    public RobyteLaserEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(RADIUS, 0.2F);
        this.entityData.define(MAX_LIFE, 40);
        this.entityData.define(DAMAGE, Config.ROBYTE_LASER_DAMAGE.get().floatValue());
        this.entityData.define(EXPLOSIVE, false);
        this.entityData.define(OVERCHARGE, false);
        this.entityData.define(DO_BAD_ATTACK, false);
    }

    public void setRadius(float r) { this.entityData.set(RADIUS, r); }

    public float getRadius() { return this.entityData.get(RADIUS); }

    public void setMaxLife(int ticks) { this.entityData.set(MAX_LIFE, ticks); }

    public int getMaxLife() { return this.entityData.get(MAX_LIFE); }

    public void setDamage(float damage) { this.entityData.set(DAMAGE, damage); }

    public float getDamage() { return this.entityData.get(DAMAGE); }

    public void setOvercharge(boolean overcharge) {
        this.entityData.set(OVERCHARGE, overcharge);
    }

    public boolean isOvercharge() {
        return this.entityData.get(OVERCHARGE);
    }

    public void setExplosive(boolean explosive) {
        this.entityData.set(EXPLOSIVE, explosive);
    }

    public boolean isExplosive() {
        return this.entityData.get(EXPLOSIVE);
    }

    public boolean getBadAttack() { return this.entityData.get(DO_BAD_ATTACK); }

    public void setBadAttack(boolean bad) { this.entityData.set(DO_BAD_ATTACK, bad); }

    public void setOwner(@Nullable Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) return this.cachedOwner;
        if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
            return this.cachedOwner;
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        if (this.tickCount == 1 && !this.isMuted) {
            this.playSound(FuryBornSounds.ROBYTE_BEAMING.get(), 0.25F, 1.0F);
        }

        if (this.tickCount >= getMaxLife()) {
            this.discard();
            return;
        }

        float growDuration = 5.0F;
        float radiusScale = Mth.clamp((float) this.tickCount / growDuration, 0.0F, 1.0F);
        float currentHitRadius = this.getRadius() * radiusScale;

        Vec3 start = this.position();
        Vec3 dir = this.getLookAngle();
        double length = Config.BUSTER_THROWER_LASER_LENGTH.get().floatValue();
        Vec3 end = start.add(dir.scale(length));

        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                start, end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this
        );
        net.minecraft.world.phys.BlockHitResult blockHit = this.level().clip(context);

        if (blockHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            end = blockHit.getLocation();

            if ((this.isExplosive() || this.isOvercharge()) && this.tickCount % 10 == 0) {
                float explosionRadius = currentHitRadius * 3.0F;

                if (this.isOvercharge()) {
                    float destroyRadius = currentHitRadius * 1.5F;
                    AABB destroyBox = new AABB(
                            end.x - destroyRadius, end.y - destroyRadius, end.z - destroyRadius,
                            end.x + destroyRadius, end.y + destroyRadius, end.z + destroyRadius
                    );
                    BlockPos.betweenClosedStream(destroyBox).forEach(pos -> {
                        BlockPos immutablePos = pos.immutable();
                        if (!this.level().getBlockState(immutablePos).isAir() &&
                                this.level().getBlockState(immutablePos).getDestroySpeed(this.level(), immutablePos) >= 0) {
                            this.level().destroyBlock(immutablePos, false, this);
                        }
                    });
                } else {
                    this.level().explode(
                            this.getOwner() != null ? this.getOwner() : this,
                            end.x, end.y, end.z,
                            explosionRadius,
                            Level.ExplosionInteraction.BLOCK
                    );

                    if (this.level() instanceof ServerLevel serverLevel) {
                        int particleCount = (int) (explosionRadius * 20);
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                                end.x, end.y, end.z, particleCount / 5,
                                explosionRadius * 0.5, explosionRadius * 0.5, explosionRadius * 0.3, 0.1D);
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                                end.x, end.y, end.z, particleCount,
                                explosionRadius * 0.5, explosionRadius * 0.5, explosionRadius * 0.4, 0.3D);
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                                end.x, end.y, end.z, particleCount,
                                explosionRadius * 0.5, explosionRadius * 0.5, explosionRadius * 0.5, 0.1D);
                    }
                }
            }
        }

        AABB searchBox = new AABB(start, end).inflate(currentHitRadius + 1.0D);
        Entity owner = this.getOwner();

        Entity bitOwnerEntity = null;
        UUID bitOwnerUUID = null;
        if (owner instanceof RobyteBitLaserEntity bitOwner) {
            bitOwnerEntity = bitOwner.getOwner();
            bitOwnerUUID = bitOwner.getOwnerUUID();
        }

        for (Entity target : this.level().getEntities(this, searchBox, e -> e instanceof LivingEntity && !e.isSpectator())) {

            if (target.is(owner) || (this.ownerUUID != null && target.getUUID().equals(this.ownerUUID))) continue;
            if (bitOwnerEntity != null && (target.is(bitOwnerEntity) || (bitOwnerUUID != null && target.getUUID().equals(bitOwnerUUID)))) continue;

            AABB targetBox = target.getBoundingBox().inflate(currentHitRadius);
            if (targetBox.clip(start, end).isPresent()) {
                DamageSource source = this.level().damageSources().indirectMagic(this, owner != null ? owner : this);
                LivingEntity livingTargetCasted = (LivingEntity) target;
                Vec3 previousMotion = target.getDeltaMovement();
                target.invulnerableTime = 0;
                float appliedDamage = this.getDamage() * (this.isExplosive() ? 2.0F : 1.0F);
                if (this.getBadAttack()) {
                    float currentHealth = livingTargetCasted.getHealth();
                    float newHealth = Math.max(0.0F, currentHealth - appliedDamage);
                    livingTargetCasted.setHealth(newHealth);
                    livingTargetCasted.hurt(source, appliedDamage);
                }
                target.hurt(source, this.getDamage() * (this.isExplosive() ? 2.0F : 1.0F));
                target.setDeltaMovement(previousMotion);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.contains("Radius")) setRadius(nbt.getFloat("Radius"));
        if (nbt.contains("MaxLife")) setMaxLife(nbt.getInt("MaxLife"));
        if (nbt.contains("Damage")) setDamage(nbt.getFloat("Damage"));
        if (nbt.contains("Explosive")) setExplosive(nbt.getBoolean("Explosive"));
        if (nbt.contains("Overcharge")) setOvercharge(nbt.getBoolean("Overcharge"));
        if (nbt.contains("DoBadAttack")) setBadAttack(nbt.getBoolean("DoBadAttack"));
        if (nbt.hasUUID("Owner")) this.ownerUUID = nbt.getUUID("Owner");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putFloat("Radius", getRadius());
        nbt.putInt("MaxLife", getMaxLife());
        nbt.putFloat("Damage", getDamage());
        nbt.putBoolean("Explosive", isExplosive());
        nbt.putBoolean("Overcharge", isOvercharge());
        nbt.putBoolean("DoBadAttack", getBadAttack());
        if (this.ownerUUID != null) nbt.putUUID("Owner", this.ownerUUID);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}