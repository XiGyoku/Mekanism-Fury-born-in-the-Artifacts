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

    private boolean hasSummonedArea = false;
    private RobyteAreaEntity cachedArea = null;
    public int teleportCooldown = 0;
    private boolean hasStartedClientBgm = false;

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
    }

    public int getAttackTick() {
        return this.entityData.get(ATTACK_TICK);
    }

    public void setAttackTick(int tick) {
        this.entityData.set(ATTACK_TICK, tick);
    }

    public int getCannonTick() {
        return this.entityData.get(CANNON_TICK);
    }

    public void setCannonTick(int tick) {
        this.entityData.set(CANNON_TICK, tick);
    }

    public boolean hasEnteredFinalPhase() {
        return this.entityData.get(ENTERED_FINAL_PHASE);
    }

    public void setEnteredFinalPhase(boolean phase) {
        this.entityData.set(ENTERED_FINAL_PHASE, phase);
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

        if (this.getCannonTick() > 0) {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("CannonAttack"));
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

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && !this.isDeadOrDying()) {
            if (!this.hasStartedClientBgm) {
                ClientSoundHelper.playRobyteBgm(this);
                this.hasStartedClientBgm = true;
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
            LivingEntity target = this.getTarget();
            if (target instanceof Player player && this.teleportCooldown <= 0 && !this.isDeadOrDying()) {
                if (this.cachedArea != null && this.cachedArea.isEntityInsideArea(this) && this.cachedArea.isEntityInsideArea(player)) {
                    float yRot = player.getYRot();
                    double offsetX = Math.sin(Math.toRadians(yRot)) * 6.0;
                    double offsetZ = -Math.cos(Math.toRadians(yRot)) * 6.0;

                    double targetX = player.getX() + offsetX;
                    double targetY = player.getY() + 1.0;
                    double targetZ = player.getZ() + offsetZ;

                    this.teleportTo(targetX, targetY, targetZ);
                    this.lookAt(player, 30.0F, 30.0F);
                    this.setYRot(player.getYRot());

                    this.playSound(FuryBornSounds.ROBYTE_TELEPORT.get(), 1.0F, 1.0F);
                    this.teleportCooldown = 400;
                }
            }
            if (!this.isDeadOrDying() && this.cachedArea != null) {
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
            }
            if (!this.isDeadOrDying() && !this.hasEnteredFinalPhase() && this.getHealth() <= this.getMaxHealth() * 0.2F) {
                this.setEnteredFinalPhase(true);
                this.phaseTransitionTick = 1;
                this.setAttackTick(0);
                this.setCannonTick(0);
                this.bossEvent.setColor(BossEvent.BossBarColor.GREEN);
                this.bossEvent.setOverlay(BossEvent.BossBarOverlay.NOTCHED_6);
            }
            if (this.hasEnteredFinalPhase() && !this.isDeadOrDying() && this.phaseTransitionTick == 0) {
                if (this.tickCount % 15 == 0) {
                    shootFiveWitherSkull();
                    for (int i = 0; i < 5; i++) {
                        this.summonBitLaser();
                    }
                }
            }

            int cTick = this.getCannonTick();
            if (cTick > 0) {
                this.setCannonTick(cTick + 1);
                this.getNavigation().stop();
                if (cTick <= 150) {
                    if (cTick % 20 == 0) {
                        this.summonBitLaser();
                    }
                }
                if (cTick >= 80 && cTick <= 150 && cTick % 7 == 0) {
                    shootWitherSkull();
                }
                if (cTick > CANNON_DUR) {
                    this.setCannonTick(0);
                    this.attackCooldown = 20;
                }
            }

            int aTick = this.getAttackTick();
            if (aTick > 0) {
                this.setAttackTick(aTick + 1);
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

    private void summonBitLaser() {
        if (!this.level().isClientSide) {
            RobyteBitLaserEntity bit = new RobyteBitLaserEntity(FuryBornEntityTypes.ROBYTE_BIT_LASER.get(), this.level());
            bit.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            bit.setOwner(this);
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("HasSummonedArea")) {
            this.hasSummonedArea = compoundTag.getBoolean("HasSummonedArea");
        }
    }
}