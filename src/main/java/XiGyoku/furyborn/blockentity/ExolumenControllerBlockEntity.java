package XiGyoku.furyborn.blockentity;

import XiGyoku.furyborn.block.ExolumenPortalBlock;
import XiGyoku.furyborn.block.FuryBornBlocks;
import mekanism.common.entity.EntityRobit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.UUID;

public class ExolumenControllerBlockEntity extends BlockEntity {

    private PortalAnimationState currentState = PortalAnimationState.IDLE;
    private int animationTick = 0;
    private Entity targetRobit = null;
    private UUID targetRobitUUID = null;

    private BlockPos portalCenter = null;
    private boolean isMaster = false;
    private int energyCostPerTick = 0;

    private static final SoundEvent FUSION_SOUND = SoundEvent.createVariableRangeEvent(new ResourceLocation("mekanismgenerators", "fusion_reactor"));

    public ExolumenControllerBlockEntity(BlockPos pos, BlockState state) {
        super(FuryBornBlockEntities.EXOLUMEN_CONTROLLER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ExolumenControllerBlockEntity entity) {
        if (level.getGameTime() % 40 == 0 || entity.portalCenter == null) {
            entity.checkStructure(pos);
        }

        if (!entity.isMaster || entity.portalCenter == null) return;

        if (entity.targetRobit == null && entity.targetRobitUUID != null && level.isClientSide) {
            AABB searchArea = new AABB(pos).inflate(15.0);
            for (Entity e : level.getEntities(null, searchArea)) {
                if (e.getUUID().equals(entity.targetRobitUUID)) {
                    entity.targetRobit = e;
                    break;
                }
            }
        }

        if (entity.currentState == PortalAnimationState.IDLE) {
            if (!level.isClientSide) {
                entity.autoStartScan(level, pos);
            }
            return;
        }

        if (!level.isClientSide && !entity.consumeEnergyFromPorts(level)) {
            entity.resetSequence(level, pos);
            return;
        }

        entity.animationTick++;

        switch (entity.currentState) {
            case PULLING_ROBIT -> entity.handlePullingPhase(level, pos);
            case CHARGING -> entity.handleChargingPhase(level, pos);
            case FUSING -> entity.handleFusingPhase(level, pos);
            case DROPPING -> entity.handleDroppingPhase(level, pos);
        }
    }

    private void checkStructure(BlockPos myPos) {
        BlockPos framePos = myPos.below();
        this.portalCenter = null;
        this.isMaster = false;

        for (int ox = -2; ox <= 2; ox++) {
            for (int oz = -2; oz <= 2; oz++) {
                BlockPos candidate = framePos.offset(ox, 0, oz);
                if (isValidFrameStructure(candidate)) {
                    this.portalCenter = candidate;
                    break;
                }
            }
            if (this.portalCenter != null) break;
        }

        if (this.portalCenter != null) {
            BlockPos masterCoil = null;
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                        BlockPos c = this.portalCenter.offset(x, 1, z);
                        if (level.getBlockState(c).is(FuryBornBlocks.SUPERCHARGED_COIL_PORTAL.get())) {
                            masterCoil = c;
                            break;
                        }
                    }
                }
                if (masterCoil != null) break;
            }
            this.isMaster = myPos.equals(masterCoil);
        }
    }

    private boolean isValidFrameStructure(BlockPos center) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    if (!level.getBlockState(center.offset(x, 0, z)).is(FuryBornBlocks.PORTAL_FRAME_MEK.get())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void autoStartScan(Level level, BlockPos pos) {
        if (checkAllPortsEnergy(level, 0.5f)) {
            findNearestRobit(level);
            if (this.targetRobit != null) {
                calculateEnergyCost(level);
                startSequence();
            }
        }
    }

    private boolean checkAllPortsEnergy(Level level, float threshold) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    BlockEntity be = level.getBlockEntity(portalCenter.offset(x, 0, z));
                    if (be instanceof PortalFrameBlockEntity port) {
                        if (port.getMaxEnergyStored() == 0) return false;
                        if ((float) port.getEnergyStored() / port.getMaxEnergyStored() < threshold) return false;
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void calculateEnergyCost(Level level) {
        for (int x = -2; x <= 2; x++) {
            if (Math.abs(x) == 2) {
                BlockEntity be = level.getBlockEntity(portalCenter.offset(x, 0, -2));
                if (be instanceof PortalFrameBlockEntity port) {
                    this.energyCostPerTick = Math.max(1, port.getEnergyStored() / 280);
                    return;
                }
            }
        }
    }

    private boolean consumeEnergyFromPorts(Level level) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    BlockEntity be = level.getBlockEntity(portalCenter.offset(x, 0, z));
                    if (be instanceof PortalFrameBlockEntity port) {
                        if (port.getEnergyStored() < energyCostPerTick) return false;
                        port.consumeEnergy(energyCostPerTick);
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void extractRemainingEnergy(Level level) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    BlockEntity be = level.getBlockEntity(portalCenter.offset(x, 0, z));
                    if (be instanceof PortalFrameBlockEntity port) {
                        port.consumeEnergy(port.getEnergyStored());
                    }
                }
            }
        }
    }

    private void resetSequence(Level level, BlockPos pos) {
        if (this.targetRobit instanceof LivingEntity living) {
            living.setSecondsOnFire(0);
            living.removeEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY);
        }
        if (!level.isClientSide) {
            level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        this.currentState = PortalAnimationState.IDLE;
        this.animationTick = 0;
        this.targetRobit = null;
        this.targetRobitUUID = null;
        syncData();
    }

    public void startSequence() {
        this.currentState = PortalAnimationState.PULLING_ROBIT;
        this.animationTick = 0;
        syncData();
    }

    private void lockRobitInAir() {
        if (this.targetRobit != null && portalCenter != null) {
            this.targetRobit.setDeltaMovement(Vec3.ZERO);
            this.targetRobit.setPos(portalCenter.getX() + 0.5, portalCenter.getY() + 5.0, portalCenter.getZ() + 0.5);
            this.targetRobit.hasImpulse = true;
        }
    }

    private void handlePullingPhase(Level level, BlockPos pos) {
        if (this.targetRobit != null && this.targetRobit.isAlive()) {
            double radius = Math.max(0.1, 4.0 - (animationTick * 0.05));
            double angle = animationTick * 0.2;
            Vec3 targetPoint = new Vec3(portalCenter.getX() + 0.5 + Math.cos(angle) * radius, portalCenter.getY() + 5.0, portalCenter.getZ() + 0.5 + Math.sin(angle) * radius);

            Vec3 moveVec = targetPoint.subtract(this.targetRobit.position());
            this.targetRobit.setDeltaMovement(moveVec.scale(0.2));
            this.targetRobit.setYRot((float) (angle * 57.295));
            this.targetRobit.hasImpulse = true;

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CLOUD, this.targetRobit.getX(), this.targetRobit.getY(), this.targetRobit.getZ(), 2, 0.1, 0.1, 0.1, 0.01);
            }

            if (radius <= 0.1 && animationTick > 60) {
                transitionTo(PortalAnimationState.CHARGING);
            }
        } else {
            resetSequence(level, pos);
        }
    }

    private void handleChargingPhase(Level level, BlockPos pos) {
        lockRobitInAir();

        if (this.animationTick == 50) {
            level.playSound(null, pos, FUSION_SOUND, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (this.targetRobit instanceof LivingEntity living && this.animationTick > 50) {
            living.setSecondsOnFire(1);
        }

        if (this.animationTick > 100) transitionTo(PortalAnimationState.FUSING);
    }

    private void handleFusingPhase(Level level, BlockPos pos) {
        lockRobitInAir();

        if (this.targetRobit instanceof LivingEntity living) {
            living.setSecondsOnFire(1);
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.INVISIBILITY, 2, 0, false, false));
        }

        if (this.animationTick == 40) {
            if (!level.isClientSide && this.targetRobit != null) {
                BlockPos robitPos = this.targetRobit.blockPosition();
                ServerLevel sl = (ServerLevel) level;

                level.playSound(null, robitPos, SoundEvents.IRON_GOLEM_DEATH, SoundSource.NEUTRAL, 1.0F, 1.0F);
                level.playSound(null, robitPos, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 2.0F, 1.5F);

                double rX = this.targetRobit.getX();
                double rY = this.targetRobit.getY() + 0.5;
                double rZ = this.targetRobit.getZ();
                sl.sendParticles(ParticleTypes.EXPLOSION, rX, rY, rZ, 10, 0.5, 0.5, 0.5, 0.1);
                sl.sendParticles(ParticleTypes.FLAME, rX, rY, rZ, 30, 0.5, 0.5, 0.5, 0.2);

                this.targetRobit.discard();
                this.targetRobit = null;
            }
        }

        if (this.animationTick > 80) {
            transitionTo(PortalAnimationState.DROPPING);
        }
    }

    private void handleDroppingPhase(Level level, BlockPos pos) {
        if (this.animationTick > 40) {
            if (!level.isClientSide) {
                extractRemainingEnergy(level);
                ExolumenPortalBlock.generatePortalStructure((ServerLevel) level, portalCenter, false);

                level.playSound(null, portalCenter, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.playSound(null, portalCenter, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, 0.7F);

                ServerLevel sl = (ServerLevel) level;
                double cX = portalCenter.getX() + 0.5;
                double cY = portalCenter.getY() + 1.0;
                double cZ = portalCenter.getZ() + 0.5;
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cX, cY, cZ, 2, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, cX, cY, cZ, 100, 2.0, 1.0, 2.0, 0.1);
                sl.sendParticles(ParticleTypes.FLAME, cX, cY, cZ, 50, 2.0, 1.0, 2.0, 0.2);
            }
            this.currentState = PortalAnimationState.IDLE;
            syncData();
        }
    }

    private void findNearestRobit(Level level) {
        Vec3 centerVec = new Vec3(portalCenter.getX() + 0.5, portalCenter.getY() + 1.0, portalCenter.getZ() + 0.5);
        AABB searchArea = new AABB(centerVec.x - 6, centerVec.y - 3, centerVec.z - 6, centerVec.x + 6, centerVec.y + 6, centerVec.z + 6);
        this.targetRobit = level.getEntitiesOfClass(EntityRobit.class, searchArea).stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(centerVec)))
                .orElse(null);
        if (this.targetRobit != null) this.targetRobitUUID = this.targetRobit.getUUID();
    }

    private void transitionTo(PortalAnimationState nextState) {
        this.currentState = nextState;
        this.animationTick = 0;
        syncData();
    }

    public boolean isMaster() { return isMaster; }
    public BlockPos getPortalCenter() { return portalCenter; }
    public PortalAnimationState getCurrentState() { return currentState; }
    public int getAnimationTick() { return animationTick; }

    private void syncData() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        currentState = PortalAnimationState.valueOf(tag.getString("State"));
        animationTick = tag.getInt("Tick");
        isMaster = tag.getBoolean("IsMaster");
        if (tag.contains("PortalCenter")) {
            portalCenter = BlockPos.of(tag.getLong("PortalCenter"));
        }
        if (tag.hasUUID("RobitUUID")) {
            targetRobitUUID = tag.getUUID("RobitUUID");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("State", currentState.name());
        tag.putInt("Tick", animationTick);
        tag.putBoolean("IsMaster", isMaster);
        if (portalCenter != null) {
            tag.putLong("PortalCenter", portalCenter.asLong());
        }
        if (targetRobitUUID != null) {
            tag.putUUID("RobitUUID", targetRobitUUID);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }
}