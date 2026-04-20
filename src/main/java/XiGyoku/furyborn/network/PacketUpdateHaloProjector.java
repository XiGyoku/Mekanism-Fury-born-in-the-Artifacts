package XiGyoku.furyborn.network;

import XiGyoku.furyborn.blockentity.HaloProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateHaloProjector {
    private final BlockPos pos;
    private final float offsetX, offsetY, offsetZ;
    private final float rotX, rotY, rotZ;
    private final float scale, sunScale;
    private final boolean individualRotation;
    private final int orbitCount;
    private final float orbitSpacing;
    private final boolean unifyPlanetColor;
    private final int unifiedPlanetColor;
    private final float rotationSpeedMultiplier;
    private final float hazeScale;
    private final int redstoneMode;

    public PacketUpdateHaloProjector(BlockPos pos, float offsetX, float offsetY, float offsetZ,
                                     float rotX, float rotY, float rotZ, float scale, float sunScale,
                                     boolean individualRotation, int orbitCount, float orbitSpacing,
                                     boolean unifyPlanetColor, int unifiedPlanetColor,
                                     float rotationSpeedMultiplier, float hazeScale, int redstoneMode) {
        this.pos = pos;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.scale = scale;
        this.sunScale = sunScale;
        this.individualRotation = individualRotation;
        this.orbitCount = orbitCount;
        this.orbitSpacing = orbitSpacing;
        this.unifyPlanetColor = unifyPlanetColor;
        this.unifiedPlanetColor = unifiedPlanetColor;
        this.rotationSpeedMultiplier = rotationSpeedMultiplier;
        this.hazeScale = hazeScale;
        this.redstoneMode = redstoneMode;
    }

    public PacketUpdateHaloProjector(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.offsetX = buf.readFloat();
        this.offsetY = buf.readFloat();
        this.offsetZ = buf.readFloat();
        this.rotX = buf.readFloat();
        this.rotY = buf.readFloat();
        this.rotZ = buf.readFloat();
        this.scale = buf.readFloat();
        this.sunScale = buf.readFloat();
        this.individualRotation = buf.readBoolean();
        this.orbitCount = buf.readInt();
        this.orbitSpacing = buf.readFloat();
        this.unifyPlanetColor = buf.readBoolean();
        this.unifiedPlanetColor = buf.readInt();
        this.rotationSpeedMultiplier = buf.readFloat();
        this.hazeScale = buf.readFloat();
        this.redstoneMode = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeFloat(this.offsetX);
        buf.writeFloat(this.offsetY);
        buf.writeFloat(this.offsetZ);
        buf.writeFloat(this.rotX);
        buf.writeFloat(this.rotY);
        buf.writeFloat(this.rotZ);
        buf.writeFloat(this.scale);
        buf.writeFloat(this.sunScale);
        buf.writeBoolean(this.individualRotation);
        buf.writeInt(this.orbitCount);
        buf.writeFloat(this.orbitSpacing);
        buf.writeBoolean(this.unifyPlanetColor);
        buf.writeInt(this.unifiedPlanetColor);
        buf.writeFloat(this.rotationSpeedMultiplier);
        buf.writeFloat(this.hazeScale);
        buf.writeInt(this.redstoneMode);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                BlockEntity be = player.level().getBlockEntity(this.pos);
                if (be instanceof HaloProjectorBlockEntity projector) {
                    projector.offsetX = this.offsetX;
                    projector.offsetY = this.offsetY;
                    projector.offsetZ = this.offsetZ;
                    projector.rotX = this.rotX;
                    projector.rotY = this.rotY;
                    projector.rotZ = this.rotZ;
                    projector.scale = this.scale;
                    projector.sunScale = this.sunScale;
                    projector.individualRotation = this.individualRotation;
                    projector.orbitCount = this.orbitCount;
                    projector.orbitSpacing = this.orbitSpacing;
                    projector.unifyPlanetColor = this.unifyPlanetColor;
                    projector.unifiedPlanetColor = this.unifiedPlanetColor;
                    projector.rotationSpeedMultiplier = this.rotationSpeedMultiplier;
                    projector.hazeScale = this.hazeScale;
                    projector.redstoneMode = this.redstoneMode;

                    projector.setChanged();
                    player.level().sendBlockUpdated(this.pos, projector.getBlockState(), projector.getBlockState(), 3);
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}