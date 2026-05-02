package XiGyoku.furyborn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import XiGyoku.furyborn.item.FuryBornItems;

import java.util.List;

public class RoadBikeBitEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<String> ANIM_STATE = SynchedEntityData.defineId(RoadBikeBitEntity.class, EntityDataSerializers.STRING);

    private static final float MAX_SPEED = 1.0f;
    private static final float ACCELERATION = 0.02f;
    private static final float DECELERATION = 0.08f;

    private static final int TICK_THROTTLE = 5;
    private static final int TICK_RETURN = 5;
    private static final int TICK_REBELLION_START = 15;
    private static final int TICK_REBELLION_END = 10;

    private float currentSpeed = 0.0f;
    private int animTimer = 0;

    public boolean isRebellionKeyPressed = false;

    public RoadBikeBitEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIM_STATE, BikeAnimState.IDLE.name());
    }

    public BikeAnimState getBikeState() {
        return BikeAnimState.valueOf(this.entityData.get(ANIM_STATE));
    }

    public void setBikeState(BikeAnimState state) {
        if (getBikeState() != state) {
            this.entityData.set(ANIM_STATE, state.name());
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() && !this.isVehicle()) {
            if (!this.level().isClientSide()) {
                this.spawnAtLocation(FuryBornItems.ROADBIKE_BIT_ITEM.get());
                this.discard();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else if (!this.level().isClientSide() && player.getVehicle() == null) {
            player.startRiding(this);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(FuryBornItems.ROADBIKE_BIT_ITEM.get());
    }

    @Override
    public boolean canBeRiddenUnderFluidType(net.minecraftforge.fluids.FluidType type, Entity rider) {
        return true;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() - 0.25D;
    }

    @Override
    public void tick() {
        super.tick();

        if (animTimer > 0) {
            animTimer--;
        }

        if (!this.level().isClientSide()) {
            BikeAnimState currentState = getBikeState();
            if (currentState == BikeAnimState.REBELLION_LOOP) {
                performRebellionAttack();
            }
        }
    }

    @Override
    public void travel(Vec3 movementInput) {
        if (!this.isAlive()) return;

        LivingEntity passenger = this.getControllingPassenger();
        if (this.isVehicle() && passenger != null) {
            BikeAnimState state = getBikeState();

            float forwardInput = passenger.zza;
            float sideInput = passenger.xxa;

            if (isRebellionKeyPressed && state != BikeAnimState.REBELLION_LOOP && state != BikeAnimState.REBELLION_START) {
                if (state == BikeAnimState.LEFT_LOOP || state == BikeAnimState.LEFT_THROTTLE) {
                    setBikeState(BikeAnimState.LEFT_RETURN);
                    animTimer = TICK_RETURN;
                } else if (state == BikeAnimState.RIGHT_LOOP || state == BikeAnimState.RIGHT_THROTTLE) {
                    setBikeState(BikeAnimState.RIGHT_RETURN);
                    animTimer = TICK_RETURN;
                } else if (animTimer <= 0) {
                    setBikeState(BikeAnimState.REBELLION_START);
                    animTimer = TICK_REBELLION_START;
                }
            } else if (!isRebellionKeyPressed && state == BikeAnimState.REBELLION_LOOP) {
                setBikeState(BikeAnimState.REBELLION_END);
                animTimer = TICK_REBELLION_END;
            }

            if (animTimer <= 0) {
                if (state == BikeAnimState.REBELLION_START) {
                    setBikeState(BikeAnimState.REBELLION_LOOP);
                } else if (state == BikeAnimState.REBELLION_END || state == BikeAnimState.LEFT_RETURN || state == BikeAnimState.RIGHT_RETURN) {
                    setBikeState(BikeAnimState.IDLE);
                }
            }

            boolean canSteer = (state != BikeAnimState.REBELLION_START && state != BikeAnimState.REBELLION_LOOP && state != BikeAnimState.REBELLION_END);

            if (canSteer) {
                if (sideInput > 0) {
                    if (state == BikeAnimState.IDLE || state == BikeAnimState.GO || state == BikeAnimState.GOBACK) {
                        setBikeState(BikeAnimState.LEFT_THROTTLE);
                        animTimer = TICK_THROTTLE;
                    } else if (state == BikeAnimState.LEFT_THROTTLE && animTimer <= 0) {
                        setBikeState(BikeAnimState.LEFT_LOOP);
                    } else if (state == BikeAnimState.RIGHT_LOOP || state == BikeAnimState.RIGHT_THROTTLE) {
                        setBikeState(BikeAnimState.RIGHT_RETURN);
                        animTimer = TICK_RETURN;
                    }
                } else if (sideInput < 0) {
                    if (state == BikeAnimState.IDLE || state == BikeAnimState.GO || state == BikeAnimState.GOBACK) {
                        setBikeState(BikeAnimState.RIGHT_THROTTLE);
                        animTimer = TICK_THROTTLE;
                    } else if (state == BikeAnimState.RIGHT_THROTTLE && animTimer <= 0) {
                        setBikeState(BikeAnimState.RIGHT_LOOP);
                    } else if (state == BikeAnimState.LEFT_LOOP || state == BikeAnimState.LEFT_THROTTLE) {
                        setBikeState(BikeAnimState.LEFT_RETURN);
                        animTimer = TICK_RETURN;
                    }
                } else {
                    if (state == BikeAnimState.LEFT_LOOP || state == BikeAnimState.LEFT_THROTTLE) {
                        setBikeState(BikeAnimState.LEFT_RETURN);
                        animTimer = TICK_RETURN;
                    } else if (state == BikeAnimState.RIGHT_LOOP || state == BikeAnimState.RIGHT_THROTTLE) {
                        setBikeState(BikeAnimState.RIGHT_RETURN);
                        animTimer = TICK_RETURN;
                    }
                }

                state = getBikeState();

                if (state == BikeAnimState.LEFT_LOOP) {
                    this.setYRot(this.getYRot() - 4.0f);
                } else if (state == BikeAnimState.RIGHT_LOOP) {
                    this.setYRot(this.getYRot() + 4.0f);
                }

                if (state == BikeAnimState.IDLE || state == BikeAnimState.GO || state == BikeAnimState.GOBACK) {
                    if (forwardInput > 0) {
                        setBikeState(BikeAnimState.GO);
                    } else if (forwardInput < 0) {
                        setBikeState(BikeAnimState.GOBACK);
                    } else {
                        setBikeState(BikeAnimState.IDLE);
                    }
                }
            }

            if (forwardInput > 0) {
                currentSpeed += ACCELERATION;
                if (currentSpeed > MAX_SPEED) currentSpeed = MAX_SPEED;
            } else if (forwardInput < 0) {
                currentSpeed -= ACCELERATION;
                if (currentSpeed < -MAX_SPEED / 2.0f) currentSpeed = -MAX_SPEED / 2.0f;
            } else {
                if (currentSpeed > 0) {
                    currentSpeed -= DECELERATION;
                    if (currentSpeed < 0) currentSpeed = 0;
                } else if (currentSpeed < 0) {
                    currentSpeed += DECELERATION;
                    if (currentSpeed > 0) currentSpeed = 0;
                }
            }

            this.yRotO = this.getYRot();
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            passenger.setYBodyRot(this.getYRot());

            Vec3 moveVector = new Vec3(0, 0, currentSpeed).yRot(-this.getYRot() * ((float)Math.PI / 180F));
            Vec3 currentMotion = this.getDeltaMovement();
            this.setDeltaMovement(new Vec3(moveVector.x, currentMotion.y, moveVector.z));

            super.travel(new Vec3(0, movementInput.y, 0));

        } else {
            this.currentSpeed = 0;
            this.setBikeState(BikeAnimState.IDLE);
            super.travel(movementInput);
        }
    }

    private void performRebellionAttack() {
        Vec3 forward = this.getLookAngle().scale(2.0);
        AABB attackBox = this.getBoundingBox().move(forward).inflate(1.5D, 0.5D, 1.5D);
        List<Entity> targets = this.level().getEntities(this, attackBox);

        for (Entity target : targets) {
            if (target instanceof LivingEntity living && target != this.getControllingPassenger()) {
                living.hurt(this.damageSources().mobAttack(this), 15.0F);
                Vec3 knockbackDir = target.position().subtract(this.position()).normalize();
                living.knockback(1.5D, -knockbackDir.x, -knockbackDir.z);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<RoadBikeBitEntity> event) {
        BikeAnimState state = getBikeState();
        AnimationController<RoadBikeBitEntity> controller = event.getController();

        double speedMultiplier = 1.0;
        if (state == BikeAnimState.GO || state == BikeAnimState.GOBACK) {
            float speedRatio = Math.abs(currentSpeed) / MAX_SPEED;
            speedMultiplier = 0.25 + (speedRatio * 3.75);
        }
        controller.setAnimationSpeed(speedMultiplier);

        switch (state) {
            case GO:
                controller.setAnimation(RawAnimation.begin().thenLoop("animation.roadbike_bit.walk"));
                break;
            case GOBACK:
                controller.setAnimation(RawAnimation.begin().thenLoop("goback"));
                break;
            case LEFT_THROTTLE:
                controller.setAnimation(RawAnimation.begin().thenPlay("left_throttle"));
                break;
            case LEFT_LOOP:
                controller.setAnimation(RawAnimation.begin().thenLoop("left"));
                break;
            case LEFT_RETURN:
                controller.setAnimation(RawAnimation.begin().thenPlay("left_throttle_return"));
                break;
            case RIGHT_THROTTLE:
                controller.setAnimation(RawAnimation.begin().thenPlay("right_throttle"));
                break;
            case RIGHT_LOOP:
                controller.setAnimation(RawAnimation.begin().thenLoop("right"));
                break;
            case RIGHT_RETURN:
                controller.setAnimation(RawAnimation.begin().thenPlay("right_throttle_return"));
                break;
            case REBELLION_START:
                controller.setAnimation(RawAnimation.begin().thenPlay("rebellion"));
                break;
            case REBELLION_LOOP:
                controller.setAnimation(RawAnimation.begin().thenLoop("rebelliongo"));
                break;
            case REBELLION_END:
                controller.setAnimation(RawAnimation.begin().thenPlay("rebellion_return"));
                break;
            case IDLE:
            default:
                controller.setAnimation(RawAnimation.begin().thenLoop("animation.roadbike_bit.idle"));
                break;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}