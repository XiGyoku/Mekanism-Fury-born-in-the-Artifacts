package XiGyoku.furyborn.entity;

import XiGyoku.furyborn.client.sound.ClientSoundHelper;
import XiGyoku.furyborn.effect.FuryBornEffects;
import XiGyoku.furyborn.entity.AI.RobyteAttackGoal;
import XiGyoku.furyborn.sound.FuryBornSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import XiGyoku.furyborn.entity.RobyteBitLaserEntity;

import java.util.List;

public class RobyteEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ATTACK_TICK = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CANNON_TICK = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ENTERED_FINAL_PHASE = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TRANSAM_TICK = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_TRANSAM = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FINAL_LASER_TICK = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> FINAL_LASER_X = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FINAL_LASER_Y = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FINAL_LASER_Z = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FINAL_LASER_YAW = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FINAL_LASER_PITCH = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> ALL_RANGE_TICK = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> ALL_RANGE_TRACK_YAW = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ALL_RANGE_TRACK_PITCH = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_REBELLION = SynchedEntityData.defineId(RobyteEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public int attackCooldown = 0;
    public int phaseTransitionTick = 0;

    public final int ROTATION_START_DUR = 20;
    public final int ROTATION_LOOP_DUR = 200;
    public final int ROTATION_END_DUR = 20;
    public final int ROTATION_TOTAL_ATTACK_DUR = ROTATION_START_DUR + ROTATION_LOOP_DUR + ROTATION_END_DUR;
    public final int PHASE_TRANSITION_DUR = 60;
    public final int CANNON_DUR = 280;

    public final int ALL_RANGE_START_DUR = 60;
    public final int ALL_RANGE_LOOP_DUR = 200;
    public final int ALL_RANGE_END_DUR = 80;
    public final int ALL_RANGE_TOTAL_DUR = ALL_RANGE_START_DUR + ALL_RANGE_LOOP_DUR + ALL_RANGE_END_DUR;

    public final int TRANSAM_LOOP_DUR = 400;
    public final int TRANSAM_TOTAL_ATTACK_DUR = ROTATION_START_DUR + TRANSAM_LOOP_DUR + ROTATION_END_DUR;

    private boolean hasSummonedArea = false;
    private RobyteAreaEntity cachedArea = null;
    public int teleportCooldown = 0;
    private boolean hasStartedClientBgm = false;

    public int transamCooldown = 0;
    public int actionCount = 0;

    private final java.util.List<RobyteLaserEntity> allRangeSmallLasers = new java.util.ArrayList<>();
    private final java.util.List<Float> smallLaserInitialYaws = new java.util.ArrayList<>();
    private final java.util.List<Float> smallLaserInitialPitches = new java.util.ArrayList<>();
    private RobyteLaserEntity allRangeBigLaser = null;

    public int transamTeleportCount = 0;
    public final java.util.List<net.minecraft.world.phys.Vec3> pastPositions = new java.util.ArrayList<>();

    public RobyteEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, pLevel);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACK_TICK, 0);
        this.entityData.define(CANNON_TICK, 0);
        this.entityData.define(ENTERED_FINAL_PHASE, false);
        this.entityData.define(TRANSAM_TICK, 0);
        this.entityData.define(IS_TRANSAM, false);
        this.entityData.define(FINAL_LASER_TICK, 0);
        this.entityData.define(FINAL_LASER_X, 0.0F);
        this.entityData.define(FINAL_LASER_Y, 0.0F);
        this.entityData.define(FINAL_LASER_Z, 0.0F);
        this.entityData.define(FINAL_LASER_YAW, 0.0F);
        this.entityData.define(FINAL_LASER_PITCH, 0.0F);
        this.entityData.define(ALL_RANGE_TICK, 0);
        this.entityData.define(ALL_RANGE_TRACK_YAW, 0.0F);
        this.entityData.define(ALL_RANGE_TRACK_PITCH, 0.0F);
        this.entityData.define(IS_REBELLION, false);
    }

    public int getAttackTick() { return this.entityData.get(ATTACK_TICK); }
    public void setAttackTick(int tick) { this.entityData.set(ATTACK_TICK, tick); }
    public int getCannonTick() { return this.entityData.get(CANNON_TICK); }
    public void setCannonTick(int tick) { this.entityData.set(CANNON_TICK, tick); }
    public boolean hasEnteredFinalPhase() { return this.entityData.get(ENTERED_FINAL_PHASE); }
    public void setEnteredFinalPhase(boolean phase) { this.entityData.set(ENTERED_FINAL_PHASE, phase); }
    public int getTransamTick() { return this.entityData.get(TRANSAM_TICK); }
    public void setTransamTick(int tick) { this.entityData.set(TRANSAM_TICK, tick); }
    public boolean isTransamMode() { return this.entityData.get(IS_TRANSAM); }
    public void setTransamMode(boolean mode) { this.entityData.set(IS_TRANSAM, mode); }
    public int getFinalLaserTick() { return this.entityData.get(FINAL_LASER_TICK); }
    public void setFinalLaserTick(int tick) { this.entityData.set(FINAL_LASER_TICK, tick); }
    public float getFinalLaserX() { return this.entityData.get(FINAL_LASER_X); }
    public void setFinalLaserX(float v) { this.entityData.set(FINAL_LASER_X, v); }
    public float getFinalLaserY() { return this.entityData.get(FINAL_LASER_Y); }
    public void setFinalLaserY(float v) { this.entityData.set(FINAL_LASER_Y, v); }
    public float getFinalLaserZ() { return this.entityData.get(FINAL_LASER_Z); }
    public void setFinalLaserZ(float v) { this.entityData.set(FINAL_LASER_Z, v); }
    public float getFinalLaserYaw() { return this.entityData.get(FINAL_LASER_YAW); }
    public void setFinalLaserYaw(float v) { this.entityData.set(FINAL_LASER_YAW, v); }
    public float getFinalLaserPitch() { return this.entityData.get(FINAL_LASER_PITCH); }
    public void setFinalLaserPitch(float v) { this.entityData.set(FINAL_LASER_PITCH, v); }
    public int getAllRangeTick() { return this.entityData.get(ALL_RANGE_TICK); }
    public void setAllRangeTick(int tick) { this.entityData.set(ALL_RANGE_TICK, tick); }
    public float getAllRangeTrackYaw() { return this.entityData.get(ALL_RANGE_TRACK_YAW); }
    public void setAllRangeTrackYaw(float v) { this.entityData.set(ALL_RANGE_TRACK_YAW, v); }
    public float getAllRangeTrackPitch() { return this.entityData.get(ALL_RANGE_TRACK_PITCH); }
    public void setAllRangeTrackPitch(float v) { this.entityData.set(ALL_RANGE_TRACK_PITCH, v); }

    public boolean isRebellion() { return this.entityData.get(IS_REBELLION); }

    public void setRebellion(boolean mode) {
        this.entityData.set(IS_REBELLION, mode);
        if (mode) {
            this.setHealth(Integer.MAX_VALUE);
            this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(40.0);
            this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(5.0);
            this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED).setBaseValue(30.0);
        }
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (this.isRebellion()) {
            this.spawnAtLocation(XiGyoku.furyborn.item.FuryBornItems.HALO_OF_EXOLUMEN.get());
        }
    }

    public static AttributeSupplier createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 2000.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ATTACK_SPEED, 1.0f)
                .add(Attributes.FOLLOW_RANGE, 128.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FLYING_SPEED, 3.0)
                .build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RobyteAttackGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomFlyingGoal(this, 1.5D));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private <E extends RobyteEntity> PlayState spawnPredicate(AnimationState<E> state) {
        if (this.tickCount < 40) {
            state.getController().setAnimation(RawAnimation.begin().thenPlay("getup"));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private <E extends RobyteEntity> PlayState phasePredicate(AnimationState<E> state) {
        if (!this.isDeadOrDying() && this.phaseTransitionTick > 0) {
            state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("FinalAttackStart"));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private <E extends RobyteEntity> PlayState deathPredicate(AnimationState<E> state) {
        if (this.isDeadOrDying()) {
            if (this.hasEnteredFinalPhase()) {
                state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("Defeated"));
                return PlayState.CONTINUE;
            }
        }
        return PlayState.STOP;
    }

    private <E extends RobyteEntity> PlayState predicate(AnimationState<E> state) {
        if (this.tickCount < 40 || this.phaseTransitionTick > 0 || this.isDeadOrDying()) return PlayState.STOP;
        int currentAllRangeTick = this.getAllRangeTick();
        if (currentAllRangeTick > 0) {
            if (currentAllRangeTick <= ALL_RANGE_START_DUR) {
                state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("AllRangeAttackStart"));
            } else if (currentAllRangeTick <= ALL_RANGE_START_DUR + ALL_RANGE_LOOP_DUR) {
                state.getController().setAnimation(RawAnimation.begin().thenLoop("AllRangeAttack"));
            } else {
                state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("AllRangeAttackEnd"));
            }
            return PlayState.CONTINUE;
        }

        if (this.getCannonTick() > 0) {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("CannonAttack"));
            return PlayState.CONTINUE;
        }

        int currentTransamTick = this.getTransamTick();
        if (currentTransamTick > 0) {
            if (currentTransamTick <= 120) {
                state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("RotationAttackStart"));
            } else {
                int spinTick = currentTransamTick - 120;
                if (spinTick <= ROTATION_START_DUR) {
                    state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("RotationAttackStart"));
                } else if (spinTick <= ROTATION_START_DUR + TRANSAM_LOOP_DUR) {
                    state.getController().setAnimation(RawAnimation.begin().thenLoop("RotationAttack"));
                } else {
                    state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("RotationAttackEnd"));
                }
            }
            return PlayState.CONTINUE;
        }

        int currentAttackTick = this.getAttackTick();
        if (currentAttackTick > 0) {
            if (currentAttackTick <= ROTATION_START_DUR) {
                state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("RotationAttackStart"));
            } else if (currentAttackTick <= ROTATION_START_DUR + ROTATION_LOOP_DUR) {
                state.getController().setAnimation(RawAnimation.begin().thenLoop("RotationAttack"));
            } else {
                state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("RotationAttackEnd"));
            }
            return PlayState.CONTINUE;
        }

        if (state.isMoving()) {
            state.getController().setAnimation(RawAnimation.begin().thenLoop(this.hasEnteredFinalPhase() ? "FinalAttackWalking" : "Walking"));
        } else {
            state.getController().setAnimation(RawAnimation.begin().thenLoop(this.hasEnteredFinalPhase() ? "FinalAttackIdle" : "idle"));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 10, this::predicate));
        controllers.add(new AnimationController<>(this, "spawn_controller", 0, this::spawnPredicate));
        controllers.add(new AnimationController<>(this, "phase_controller", 10, this::phasePredicate));
        controllers.add(new AnimationController<>(this, "death_controller", 0, this::deathPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    protected float getSoundVolume() {
        return 0.7F;
    }

    private void spawnFlameBeamParticles(net.minecraft.world.phys.Vec3 start, float yaw, float pitch, double radius, double distance, int particlesPerTick, int tick) {
        float f = net.minecraft.util.Mth.cos(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
        float f1 = net.minecraft.util.Mth.sin(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
        float f2 = -net.minecraft.util.Mth.cos(-pitch * ((float)Math.PI / 180F));
        float f3 = net.minecraft.util.Mth.sin(-pitch * ((float)Math.PI / 180F));
        net.minecraft.world.phys.Vec3 look = new net.minecraft.world.phys.Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));

        net.minecraft.world.phys.Vec3 up = new net.minecraft.world.phys.Vec3(0, 1, 0);
        if (Math.abs(look.y) > 0.99) up = new net.minecraft.world.phys.Vec3(1, 0, 0);
        net.minecraft.world.phys.Vec3 right = look.cross(up).normalize();
        net.minecraft.world.phys.Vec3 beamUp = right.cross(look).normalize();

        for (int i = 0; i < particlesPerTick; i++) {
            double angle = (tick * 2.0) + (i * Math.PI * 2 / particlesPerTick);
            net.minecraft.world.phys.Vec3 offset = right.scale(Math.cos(angle) * radius).add(beamUp.scale(Math.sin(angle) * radius));
            net.minecraft.world.phys.Vec3 particlePos = start.add(look.scale(distance)).add(offset);
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void tick() {
        super.tick();
        int multiplier = this.isRebellion() ? 10 : 1;

        if (this.level().isClientSide() && !this.isDeadOrDying()) {
            if (this.isTransamMode()) {
                this.pastPositions.add(0, this.position());
                if (this.pastPositions.size() > 5) {
                    this.pastPositions.remove(this.pastPositions.size() - 1);
                }
            } else if (!this.pastPositions.isEmpty()) {
                this.pastPositions.clear();
            }
            if (!this.hasStartedClientBgm) {
                ClientSoundHelper.playRobyteBgm(this);
                this.hasStartedClientBgm = true;
            }

            if (this.isTransamMode()) {
                for (int i = 0; i < 1; i++) {
                    double px = this.getX() + (this.random.nextDouble() - 0.5) * 4.0;
                    double py = this.getY() + this.random.nextDouble() * 3.0;
                    double pz = this.getZ() + (this.random.nextDouble() - 0.5) * 4.0;
                    this.level().addParticle(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0.0D, 0.0D, 0.0D);
                }
            }

            int tTick = this.getTransamTick();
            if (tTick > 0 && tTick <= 120) {
                double radius = 10.0D;
                double beamLength = 100.0D;
                double progress = (double) tTick / 120.0;
                double currentZDist = progress * beamLength;
                net.minecraft.world.phys.Vec3 start = this.getEyePosition();

                for (int angleDeg = 0; angleDeg < 360; angleDeg += 30) {
                    this.spawnFlameBeamParticles(start, (float)angleDeg, -15.0F, radius, currentZDist, 5, tTick);
                }
            }

            int fTick = this.getFinalLaserTick();
            if (fTick > 280 && fTick <= 400) {
                int pTick = fTick - 280;
                double radius = 30.0D;
                double beamLength = 100.0D;
                double progress = (double) pTick / 120.0;
                double currentZDist = progress * beamLength;
                net.minecraft.world.phys.Vec3 start = new net.minecraft.world.phys.Vec3(this.getFinalLaserX(), this.getFinalLaserY(), this.getFinalLaserZ());

                this.spawnFlameBeamParticles(start, this.getFinalLaserYaw(), this.getFinalLaserPitch(), radius, currentZDist, 10, pTick);
            }

            int arTick = this.getAllRangeTick();
            if (arTick > 0 && arTick <= ALL_RANGE_START_DUR) {
                double beamLength = 100.0D;
                double progress = (double) arTick / ALL_RANGE_START_DUR;
                double currentZDist = progress * beamLength;
                net.minecraft.world.phys.Vec3 start = new net.minecraft.world.phys.Vec3(this.getX(), this.getY() + 0.5D, this.getZ());
                this.spawnFlameBeamParticles(start, this.getAllRangeTrackYaw(), this.getAllRangeTrackPitch(), 5.0D, currentZDist, 10, arTick);
                for (int pitch = -60; pitch <= 60; pitch += 30) {
                    for (int yaw = 0; yaw < 360; yaw += 45) {
                        this.spawnFlameBeamParticles(start, (float)yaw, (float)pitch, 1.0D, currentZDist, 2, arTick);
                    }
                }
            }
        }

        if (!this.level().isClientSide()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            if (!this.hasSummonedArea) {
                RobyteAreaEntity areaEntity = new RobyteAreaEntity(FuryBornEntityTypes.ROBYTE_AREA.get(), this.level());
                areaEntity.moveTo(this.getX(), this.getY() - 2.0f, this.getZ(), this.getYRot(), this.getXRot());
                areaEntity.setRobyte(this);
                this.level().addFreshEntity(areaEntity);
                this.hasSummonedArea = true;
                this.cachedArea = areaEntity;
            } else if (this.cachedArea == null) {
                for (RobyteAreaEntity area : this.level().getEntitiesOfClass(RobyteAreaEntity.class, this.getBoundingBox().inflate(128.0D))) {
                    if (this.getUUID().equals(area.getRobyteId())) {
                        this.cachedArea = area;
                        break;
                    }
                }
            }

            if (this.tickCount >= 40) {
                if (this.teleportCooldown > 0) {
                    this.teleportCooldown--;
                }
                if (this.transamCooldown > 0) {
                    this.transamCooldown--;
                }
                LivingEntity target = this.getTarget();
                if (target instanceof Player player && this.teleportCooldown <= 0 && !this.isDeadOrDying() && this.getAllRangeTick() == 0) {
                    if (this.cachedArea != null && this.cachedArea.isEntityInsideArea(this) && this.cachedArea.isEntityInsideArea(player)) {

                        double targetX, targetY, targetZ;

                        if (this.isTransamMode()) {
                            double randomAngle = this.random.nextDouble() * 2.0 * Math.PI;
                            double distance = 12.0;
                            targetX = player.getX() + Math.cos(randomAngle) * distance;
                            targetY = player.getY();
                            targetZ = player.getZ() + Math.sin(randomAngle) * distance;

                            this.teleportTo(targetX, targetY, targetZ);
                            this.lookAt(player, 30.0F, 30.0F);

                            this.transamTeleportCount++;
                            if (this.transamTeleportCount % 4 == 0) {
                                this.teleportCooldown = 400;
                            } else {
                                this.teleportCooldown = 50;
                            }
                        } else {
                            float yRot = player.getYRot();
                            double offsetX = Math.sin(Math.toRadians(yRot)) * 6.0;
                            double offsetZ = -Math.cos(Math.toRadians(yRot)) * 6.0;

                            targetX = player.getX() + offsetX;
                            targetY = player.getY() + 1.0;
                            targetZ = player.getZ() + offsetZ;

                            this.teleportTo(targetX, targetY, targetZ);
                            this.lookAt(player, 30.0F, 30.0F);
                            this.setYRot(player.getYRot());

                            this.teleportCooldown = 400;
                        }

                        this.playSound(FuryBornSounds.ROBYTE_TELEPORT.get(), 1.0F, 1.0F);
                    }
                }
                if (!this.isDeadOrDying() && this.cachedArea != null) {
                    if (!this.cachedArea.isEntityInsideArea(this)) {
                        double dx = this.cachedArea.getX() - this.getX();
                        double dz = this.cachedArea.getZ() - this.getZ();
                        double length = Math.sqrt(dx * dx + dz * dz);
                        if (length > 0) {
                            double pushForce = 0.5;
                            this.setDeltaMovement(this.getDeltaMovement().add(dx / length * pushForce, 0, dz / length * pushForce));
                        }
                        if (this.getY() > this.cachedArea.getY() + 60.0) {
                            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.05, 0));
                        } else if (this.getY() < this.cachedArea.getY() - 2.0) {
                            this.setDeltaMovement(this.getDeltaMovement().add(0, 0.05, 0));
                        }
                    }
                }
                if (!this.isDeadOrDying() && !this.hasEnteredFinalPhase() && this.getHealth() <= this.getMaxHealth() * 0.2F) {
                    this.setEnteredFinalPhase(true);
                    this.phaseTransitionTick = 1;
                    this.setAttackTick(0);
                    this.setCannonTick(0);
                    this.setTransamTick(0);
                    this.setAllRangeTick(0);
                    this.setTransamMode(false);
                    this.setFinalLaserTick(0);
                    this.transamTeleportCount = 0;
                    this.bossEvent.setColor(BossEvent.BossBarColor.GREEN);
                    this.bossEvent.setOverlay(BossEvent.BossBarOverlay.NOTCHED_6);

                    for (RobyteLaserEntity laser : this.allRangeSmallLasers) {
                        if (laser != null && laser.isAlive()) {
                            laser.discard();
                        }
                    }
                    this.allRangeSmallLasers.clear();
                    this.smallLaserInitialYaws.clear();
                    this.smallLaserInitialPitches.clear();
                    if (this.allRangeBigLaser != null && this.allRangeBigLaser.isAlive()) {
                        this.allRangeBigLaser.discard();
                    }
                    this.allRangeBigLaser = null;
                }
                if (this.hasEnteredFinalPhase() && !this.isDeadOrDying() && this.phaseTransitionTick == 0) {
                    int fTick = this.getFinalLaserTick() + 1;

                    if (fTick == 281 && target != null) {
                        this.setFinalLaserX((float)this.getX());
                        this.setFinalLaserY((float)(this.getY() + 0.5D));
                        this.setFinalLaserZ((float)this.getZ());

                        double dx = target.getX() - this.getX();
                        double dy = target.getEyeY() - (this.getY() + 0.5D);
                        double dz = target.getZ() - this.getZ();
                        float targetYaw = (float)(net.minecraft.util.Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
                        float targetPitch = (float)(-(net.minecraft.util.Mth.atan2(dy, Math.sqrt(dx*dx + dz*dz)) * (180F / Math.PI)));

                        this.setFinalLaserYaw(targetYaw);
                        this.setFinalLaserPitch(targetPitch);

                        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
                        this.setYRot(targetYaw);
                        this.setXRot(targetPitch);
                        this.yHeadRot = targetYaw;
                        this.yBodyRot = targetYaw;
                    }

                    if (fTick >= 400) {
                        fTick = 0;
                        for (int i = 0; i < multiplier; i++) {
                            this.shootBigLaserAtPlayer(i == 0);
                        }
                    }
                    this.setFinalLaserTick(fTick);

                    if (this.tickCount % 15 == 0) {
                        for (int i = 0; i < multiplier; i++) {
                            shootFiveWitherSkull();
                        }
                        for (int i = 0; i < 5 * multiplier; i++) {
                            this.summonBitLaser(i == 0);
                        }
                    }
                    if(this.tickCount % 15 == 0) {
                        for (int i = 0; i < 5 * multiplier; i++) {
                            this.summonPredictBitLaser(i == 0);
                        }
                    }
                }

                int arTick = this.getAllRangeTick();
                if (arTick > 0) {
                    this.setAllRangeTick(arTick + 1);
                    if (arTick == 1) {
                        if (target != null) {
                            double dx = target.getX() - this.getX();
                            double dz = target.getZ() - this.getZ();
                            this.setAllRangeTrackYaw((float)(net.minecraft.util.Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F);
                        } else {
                            this.setAllRangeTrackYaw(this.getYRot());
                        }
                        this.setAllRangeTrackPitch(-90.0F);

                        this.allRangeSmallLasers.clear();
                        this.smallLaserInitialYaws.clear();
                        this.smallLaserInitialPitches.clear();
                        this.allRangeBigLaser = null;
                    }

                    if (arTick > 0 && arTick <= ALL_RANGE_START_DUR) {
                        float currentYaw = this.getAllRangeTrackYaw();
                        float currentPitch = this.getAllRangeTrackPitch();

                        if (target != null) {
                            double dx = target.getX() - this.getX();
                            double dy = target.getEyeY() - (this.getY() + 0.5D);
                            double dz = target.getZ() - this.getZ();
                            float targetYaw = (float)(net.minecraft.util.Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
                            float targetPitch = (float)(-(net.minecraft.util.Mth.atan2(dy, Math.sqrt(dx*dx + dz*dz)) * (180F / Math.PI)));

                            float diffYaw = net.minecraft.util.Mth.wrapDegrees(targetYaw - currentYaw);
                            float turn = 0.01F;
                            currentYaw += diffYaw * turn;
                            currentPitch = -90.0F;
                        }
                        this.setAllRangeTrackYaw(currentYaw);
                        this.setAllRangeTrackPitch(currentPitch);
                    }

                    if (arTick == ALL_RANGE_START_DUR + 1) {
                        for (int m = 0; m < multiplier; m++) {
                            for (int pitch = -60; pitch <= 60; pitch += 30) {
                                for (int yaw = 0; yaw < 360; yaw += 45) {
                                    RobyteLaserEntity laser = new RobyteLaserEntity(FuryBornEntityTypes.ROBYTE_LASER.get(), this.level());
                                    laser.setPos(this.getX(), this.getY() + 0.5D, this.getZ());
                                    laser.setYRot(yaw);
                                    laser.setXRot((float)pitch);
                                    laser.setRadius(1.0F);
                                    laser.setMaxLife(ALL_RANGE_LOOP_DUR + ALL_RANGE_END_DUR);
                                    laser.setDamage(0.5F);
                                    laser.setOwner(this);
                                    this.level().addFreshEntity(laser);

                                    this.allRangeSmallLasers.add(laser);
                                    this.smallLaserInitialYaws.add((float)yaw);
                                    this.smallLaserInitialPitches.add((float)pitch);
                                }
                            }
                        }

                        for (int m = 0; m < multiplier; m++) {
                            RobyteLaserEntity bigLaser = new RobyteLaserEntity(FuryBornEntityTypes.ROBYTE_LASER.get(), this.level());
                            bigLaser.setPos(this.getX(), this.getY() + 0.5D, this.getZ());
                            bigLaser.setYRot(this.getAllRangeTrackYaw());
                            bigLaser.setXRot(this.getAllRangeTrackPitch());
                            bigLaser.setRadius(1.0F);
                            bigLaser.setMaxLife(ALL_RANGE_LOOP_DUR + ALL_RANGE_END_DUR);
                            bigLaser.setDamage(1.0F);
                            bigLaser.setOwner(this);
                            this.level().addFreshEntity(bigLaser);
                            this.allRangeBigLaser = bigLaser;
                        }
                    }

                    if (arTick > ALL_RANGE_START_DUR && arTick <= ALL_RANGE_TOTAL_DUR) {
                        int loopTick = arTick - ALL_RANGE_START_DUR;
                        float baseYaw = Math.min(loopTick, ALL_RANGE_LOOP_DUR) * 2.0F;

                        for (int i = 0; i < this.allRangeSmallLasers.size(); i++) {
                            RobyteLaserEntity laser = this.allRangeSmallLasers.get(i);
                            if (laser != null && laser.isAlive()) {
                                laser.setPos(this.getX(), this.getY() + 0.5D, this.getZ());
                                laser.setYRot(baseYaw + this.smallLaserInitialYaws.get(i));
                                float pitchOffset = (float)Math.sin(loopTick * 0.03F + Math.toRadians(this.smallLaserInitialPitches.get(i))) * 40.0F;
                                laser.setXRot(this.smallLaserInitialPitches.get(i) + pitchOffset);
                            }
                        }

                        float currentYaw = this.getAllRangeTrackYaw();
                        float currentPitch = this.getAllRangeTrackPitch();
                        if (loopTick <= ALL_RANGE_LOOP_DUR) {
                            if (loopTick % 10 == 0) {
                                for (int m = 0; m < multiplier; m++) {
                                    this.summonBitLaser(m==0);
                                    this.summonBitLaserAllRange(m==0);
                                }
                            }

                            if (target != null) {
                                double dx = target.getX() - this.getX();
                                double dy = target.getEyeY() - (this.getY() + 0.5D);
                                double dz = target.getZ() - this.getZ();
                                float targetYaw = (float)(net.minecraft.util.Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
                                float targetPitch = (float)(-(net.minecraft.util.Mth.atan2(dy, Math.sqrt(dx*dx + dz*dz)) * (180F / Math.PI)));

                                float diffYaw = net.minecraft.util.Mth.wrapDegrees(targetYaw - currentYaw);
                                float turn = 0.005F;
                                currentYaw += diffYaw * turn;

                                float diffPitch = net.minecraft.util.Mth.wrapDegrees(targetPitch - currentPitch);
                                currentPitch += diffPitch * turn;
                            }
                        }

                        this.setAllRangeTrackYaw(currentYaw);
                        this.setAllRangeTrackPitch(currentPitch);

                        if (this.allRangeBigLaser != null && this.allRangeBigLaser.isAlive()) {
                            this.allRangeBigLaser.setPos(this.getX(), this.getY() + 0.5D, this.getZ());
                            this.allRangeBigLaser.setYRot(currentYaw);
                            this.allRangeBigLaser.setXRot(currentPitch);
                        }
                    }

                    if (arTick > ALL_RANGE_TOTAL_DUR) {
                        this.setAllRangeTick(0);
                        this.attackCooldown = 20;
                        this.allRangeSmallLasers.clear();
                        this.smallLaserInitialYaws.clear();
                        this.smallLaserInitialPitches.clear();
                        this.allRangeBigLaser = null;
                    }
                }

                int tTick = this.getTransamTick();
                if (tTick > 0) {
                    this.setTransamTick(tTick + 1);
                    if (tTick == 120) {
                        for (int m = 0; m < multiplier; m++) {
                            for (int i = 0; i < 360; i += 30) {
                                RobyteLaserEntity laser = new RobyteLaserEntity(FuryBornEntityTypes.ROBYTE_LASER.get(), this.level());
                                laser.setPos(this.getX(), this.getY() + 0.5D, this.getZ());
                                laser.setYRot(i);
                                laser.setXRot(-15.0F);
                                laser.setRadius(10.0F);
                                laser.setMaxLife(TRANSAM_TOTAL_ATTACK_DUR);

                                laser.setDamage(0.5F);
                                laser.setOwner(this);
                                this.level().addFreshEntity(laser);
                            }
                        }
                    }
                    if (tTick % 100 == 0) {
                        for (int m = 0; m < multiplier; m++) {
                            this.summonBitLaser(m==0);
                            this.summonPredictBitLaser(m==0);
                        }
                    }
                    if (tTick > 120 + TRANSAM_TOTAL_ATTACK_DUR) {
                        this.setTransamTick(0);
                        this.setTransamMode(false);
                        this.attackCooldown = 200;
                        this.transamTeleportCount = 0;
                    }
                }

                int cTick = this.getCannonTick();
                if (cTick > 0) {
                    this.setCannonTick(cTick + 1);
                    this.getNavigation().stop();
                    if (cTick <= 150) {
                        if (cTick % 20 == 0) {
                            for (int m = 0; m < multiplier; m++) {
                                this.summonBitLaser(m==0);
                                this.summonPredictBitLaser(m==0);
                            }
                        }
                    }
                    if (cTick >= 80 && cTick <= 150 && cTick % 7 == 0) {
                        for (int m = 0; m < multiplier; m++) {
                            shootWitherSkull();
                        }
                    }
                    if (cTick > CANNON_DUR) {
                        this.setCannonTick(0);
                        this.attackCooldown = 20;
                    }
                }

                int aTick = this.getAttackTick();
                if (aTick > 0) {
                    this.setAttackTick(aTick + 1);

                    if (aTick > ROTATION_START_DUR && aTick <= ROTATION_START_DUR + ROTATION_LOOP_DUR) {
                        if (aTick % 100 == 0) {
                            for (int m = 0; m < multiplier; m++) {
                                this.summonPredictBitLaser(m==0);
                            }
                        }
                    }

                    if (aTick > ROTATION_TOTAL_ATTACK_DUR) {
                        this.setAttackTick(0);
                        this.attackCooldown = 20;
                    }
                }

                if (this.phaseTransitionTick > 0) {
                    this.phaseTransitionTick++;
                    this.getNavigation().stop();
                    if (this.phaseTransitionTick > PHASE_TRANSITION_DUR) {
                        this.phaseTransitionTick = 0;
                    }
                }

                if (this.attackCooldown > 0) {
                    this.attackCooldown--;
                }
            }
        }
    }

    private void shootBigLaserAtPlayer(boolean SoundPlaying) {
        RobyteLaserEntity laser = new RobyteLaserEntity(FuryBornEntityTypes.ROBYTE_LASER.get(), this.level());
        laser.setPos(this.getFinalLaserX(), this.getFinalLaserY(), this.getFinalLaserZ());
        laser.setYRot(this.getFinalLaserYaw());
        laser.setXRot(this.getFinalLaserPitch());
        if (this.isRebellion()) {
            laser.setRadius(30.0F);
        } else {
        laser.setRadius(15.0F);
        }
        laser.setMaxLife(400);
        if (this.isRebellion()) {
            laser.setDamage(4.0F);
        } else {
            laser.setDamage(0.5F);
        }
        laser.setOwner(this);
        laser.setMuted(!SoundPlaying);
        this.level().addFreshEntity(laser);
    }

    private void shootWitherSkull() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            double d0 = target.getX() - this.getX();
            double d1 = target.getY(0.5D) - this.getY(1.0D);
            double d2 = target.getZ() - this.getZ();
            net.minecraft.world.phys.Vec3 look = this.getViewVector(1.0F);

            double spawnX = this.getX() + look.x * 1.0D;
            double spawnY = this.getY(1.5D) - 2.0D;
            double spawnZ = this.getZ() + look.z * 1.0D;
            double[] angles = {0.0};

            for (double angle : angles) {
                double radians = Math.toRadians(angle);
                double rotX = d0 * Math.cos(radians) - d2 * Math.sin(radians);
                double rotZ = d0 * Math.sin(radians) + d2 * Math.cos(radians);

                WitherSkull skull = new WitherSkull(this.level(), this, rotX, d1, rotZ);
                skull.setPos(spawnX, spawnY, spawnZ);
                skull.xPower *= 1.5D;
                skull.yPower *= 1.5D;
                skull.zPower *= 1.5D;
                this.level().addFreshEntity(skull);
            }
            this.playSound(SoundEvents.WITHER_SHOOT, 1.0F, 1.0F);
        }
    }

    private void summonBitLaser(boolean PlayingSound) {
        if (!this.level().isClientSide) {
            RobyteBitLaserEntity bit = new RobyteBitLaserEntity(FuryBornEntityTypes.ROBYTE_BIT_LASER.get(), this.level());
            bit.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            bit.setOwner(this);
            bit.setTarget(this.level().getNearestPlayer(this, 128.0));
            bit.setMuted(!PlayingSound);
            if (this.isRebellion()) {
                bit.setDamage(4.0F);
            }
            this.level().addFreshEntity(bit);
        }
    }

    private void summonBitLaserAllRange(boolean PlayingSound) {
        if (!this.level().isClientSide) {
            boolean isFirst = true;
            for (LivingEntity target : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(128.0D))) {
                if (target instanceof RobyteEntity) continue;
                RobyteBitLaserEntity bit = new RobyteBitLaserEntity(FuryBornEntityTypes.ROBYTE_BIT_LASER.get(), this.level());
                bit.setOwner(this);
                bit.setTarget(target);
                bit.moveTo(target.getX(), target.getY(), target.getZ(), this.getYRot(), this.getXRot());
                bit.setMuted(!(PlayingSound && isFirst));
                isFirst = false;
                if (this.isRebellion()) {
                    bit.setDamage(4.0F);
                }
                this.level().addFreshEntity(bit);
            }
        }
    }

    private void summonPredictBitLaser(boolean PlayingSound) {
        if (!this.level().isClientSide) {
            RobyteBitLaserEntity bit = new RobyteBitLaserEntity(FuryBornEntityTypes.ROBYTE_BIT_LASER.get(), this.level());
            bit.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            bit.setOwner(this);
            bit.setTarget(this.level().getNearestPlayer(this, 128.0));
            bit.setMode(true);
            bit.setMuted(!PlayingSound);
            if (this.isRebellion()) {
                bit.setDamage(4.0F);
            }
            this.level().addFreshEntity(bit);
        }
    }

    private void shootFiveWitherSkull() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            double d0 = target.getX() - this.getX();
            double d1 = target.getY(0.5D) - this.getY(1.0D);
            double d2 = target.getZ() - this.getZ();
            net.minecraft.world.phys.Vec3 look = this.getViewVector(1.0F);

            double spawnX = this.getX() + look.x * 1.0D;
            double spawnY = this.getY(1.5D) - 2.0D;
            double spawnZ = this.getZ() + look.z * 1.0D;
            double[] angles = {-40.0,-20.0, 0.0, 20.0,40.0};

            for (double angle : angles) {
                double radians = Math.toRadians(angle);
                double rotX = d0 * Math.cos(radians) - d2 * Math.sin(radians);
                double rotZ = d0 * Math.sin(radians) + d2 * Math.cos(radians);

                WitherSkull skull = new WitherSkull(this.level(), this, rotX, d1, rotZ);
                skull.setPos(spawnX, spawnY, spawnZ);
                skull.xPower *= 1.5D;
                skull.yPower *= 1.5D;
                skull.zPower *= 1.5D;
                this.level().addFreshEntity(skull);
            }
            this.playSound(SoundEvents.WITHER_SHOOT, 1.0F, 1.0F);
        }
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 1) {
            this.setNoGravity(false);
            net.minecraft.world.phys.Vec3 currentMovement = this.getDeltaMovement();
            this.setDeltaMovement(currentMovement.x, -2.5D, currentMovement.z);
        }
        if (this.hasEnteredFinalPhase()) {
            if (this.deathTime >= 140 && !this.level().isClientSide()) {
                this.level().broadcastEntityEvent(this, (byte)60);
                this.remove(Entity.RemovalReason.KILLED);
            }
        } else {
            if (this.deathTime >= 1 && !this.level().isClientSide()) {
                this.level().broadcastEntityEvent(this, (byte)60);
                this.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.isDeadOrDying() && source.getEntity() instanceof Player player) {
            if (!player.hasEffect(FuryBornEffects.MONITORED.get())) {
                player.addEffect(new MobEffectInstance(FuryBornEffects.MONITORED.get(), -1, 0, false, false, true));
            }
        }
        if (!this.hasEnteredFinalPhase() && this.getAttackTick() > 0) {
            if (source.is(DamageTypeTags.IS_PROJECTILE)) {
                this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.0F);
                return false;
            }
        }
        if (this.hasEnteredFinalPhase()) {
            if (source.getDirectEntity() != null && !source.isIndirect()) {
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (!this.isDeadOrDying() && hurt && target instanceof Player player) {
            if (!player.hasEffect(FuryBornEffects.MONITORED.get())) {
                player.addEffect(new MobEffectInstance(FuryBornEffects.MONITORED.get(), -1, 0, false, false, true));
            }
        }
        return hurt;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
    }

    @Override
    protected void jumpFromGround() {
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("HasSummonedArea", this.hasSummonedArea);
        compoundTag.putInt("TransamCooldown", this.transamCooldown);
        compoundTag.putInt("ActionCount", this.actionCount);
        compoundTag.putBoolean("IsRebellion", this.isRebellion());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("HasSummonedArea")) {
            this.hasSummonedArea = compoundTag.getBoolean("HasSummonedArea");
        }
        if (compoundTag.contains("TransamCooldown")) {
            this.transamCooldown = compoundTag.getInt("TransamCooldown");
        }
        if (compoundTag.contains("ActionCount")) {
            this.actionCount = compoundTag.getInt("ActionCount");
        }
        if (compoundTag.contains("IsRebellion")) {
            this.setRebellion(compoundTag.getBoolean("IsRebellion"));
        }
    }
}