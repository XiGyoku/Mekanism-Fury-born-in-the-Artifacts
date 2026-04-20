package XiGyoku.furyborn.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class HaloProjectorBlockEntity extends BlockEntity {

    public float offsetX = 0.0f;
    public float offsetY = 1.0f;
    public float offsetZ = 0.0f;
    public float rotX = 180.0f;
    public float rotY = 0.0f;
    public float rotZ = 0.0f;
    public float scale = 1.0f;
    public float sunScale = 0.2f;
    public boolean individualRotation = true;
    public int orbitCount = 5;
    public float orbitSpacing = 0.5f;
    public boolean unifyPlanetColor = false;
    public int unifiedPlanetColor = 0xFFFFFF;
    public float rotationSpeedMultiplier = 0.5f;
    public float hazeScale = 0.1f;
    public int redstoneMode = 0;
    public float currentRenderScale = 0.0f;

    public HaloProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(FuryBornBlockEntities.HALO_PROJECTOR.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(Double.MAX_VALUE);
    }

    public boolean isActive() {
        if (this.level == null) return false;
        boolean powered = this.getBlockState().getValue(XiGyoku.furyborn.block.HaloProjectorBlock.POWERED);
        if (this.redstoneMode == 0) return true;
        if (this.redstoneMode == 1) return powered;
        if (this.redstoneMode == 2) return !powered;
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("OffsetX", this.offsetX);
        tag.putFloat("OffsetY", this.offsetY);
        tag.putFloat("OffsetZ", this.offsetZ);
        tag.putFloat("RotX", this.rotX);
        tag.putFloat("RotY", this.rotY);
        tag.putFloat("RotZ", this.rotZ);
        tag.putFloat("Scale", this.scale);
        tag.putFloat("SunScale", this.sunScale);
        tag.putBoolean("IndividualRotation", this.individualRotation);
        tag.putInt("OrbitCount", this.orbitCount);
        tag.putFloat("OrbitSpacing", this.orbitSpacing);
        tag.putBoolean("UnifyPlanetColor", this.unifyPlanetColor);
        tag.putInt("UnifiedPlanetColor", this.unifiedPlanetColor);
        tag.putFloat("RotationSpeedMultiplier", this.rotationSpeedMultiplier);
        tag.putFloat("HazeScale", this.hazeScale);
        tag.putInt("RedstoneMode", this.redstoneMode);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.offsetX = tag.getFloat("OffsetX");
        this.offsetY = tag.getFloat("OffsetY");
        this.offsetZ = tag.getFloat("OffsetZ");
        this.rotX = tag.getFloat("RotX");
        this.rotY = tag.getFloat("RotY");
        this.rotZ = tag.getFloat("RotZ");
        this.scale = tag.getFloat("Scale");
        this.sunScale = tag.getFloat("SunScale");
        this.individualRotation = tag.getBoolean("IndividualRotation");
        this.orbitCount = tag.getInt("OrbitCount");
        this.orbitSpacing = tag.getFloat("OrbitSpacing");
        this.unifyPlanetColor = tag.getBoolean("UnifyPlanetColor");
        this.unifiedPlanetColor = tag.getInt("UnifiedPlanetColor");
        this.rotationSpeedMultiplier = tag.getFloat("RotationSpeedMultiplier");
        this.hazeScale = tag.getFloat("HazeScale");
        this.redstoneMode = tag.getInt("RedstoneMode");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }
}