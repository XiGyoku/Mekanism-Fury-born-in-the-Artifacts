package XiGyoku.furyborn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ExolumenPortalBlock extends Block {

    protected static final VoxelShape SHAPE = Block.box(0.0D, 6.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public ExolumenPortalBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.NETHER_PORTAL)
                .noCollission()
                .noLootTable()
                .lightLevel((state) -> 12));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static void generatePortalStructure(ServerLevel level, BlockPos centerPos, boolean isToExolumen) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos targetPos = centerPos.offset(x, 0, z);
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    level.setBlockAndUpdate(targetPos, FuryBornBlocks.PORTAL_FRAME_MEK.get().defaultBlockState());
                } else {
                    level.setBlockAndUpdate(targetPos, FuryBornBlocks.EXOLUMEN_PORTAL.get().defaultBlockState());
                }

                if (isToExolumen) {
                    for (int y = 1; y <= 4; y++) {
                        level.setBlockAndUpdate(centerPos.offset(x, y, z), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    public static boolean detectAndFillPortal(ServerLevel level, BlockPos clickedPos) {
        for (int ox = -2; ox <= 2; ox++) {
            for (int oz = -2; oz <= 2; oz++) {
                BlockPos centerCandidate = clickedPos.offset(ox, 0, oz);
                if (isValidFrame(level, centerCandidate)) {
                    fillPortalOnly(level, centerCandidate);
                    level.playSound(null, clickedPos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isValidFrame(ServerLevel level, BlockPos center) {
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

    private static void fillPortalOnly(ServerLevel level, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos portalPos = center.offset(x, 0, z);
                level.setBlockAndUpdate(portalPos, FuryBornBlocks.EXOLUMEN_PORTAL.get().defaultBlockState());
            }
        }
    }

    public static void destroyAllConnectedPortals(ServerLevel level, BlockPos startPos) {
        Stack<BlockPos> targets = new Stack<>();
        Set<BlockPos> visited = new HashSet<>();

        for (Direction dir : Direction.values()) {
            BlockPos side = startPos.relative(dir);
            if (level.getBlockState(side).is(FuryBornBlocks.EXOLUMEN_PORTAL.get())) {
                targets.push(side);
            }
        }

        while (!targets.isEmpty()) {
            BlockPos current = targets.pop();
            if (visited.contains(current)) continue;

            visited.add(current);
            level.setBlockAndUpdate(current, Blocks.AIR.defaultBlockState());

            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);
                if (level.getBlockState(next).is(FuryBornBlocks.EXOLUMEN_PORTAL.get())) {
                    targets.push(next);
                }
            }
        }
    }

    private BlockPos findPortalCenter(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos candidate = pos.offset(x, 0, z);
                boolean isCenter = true;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (!level.getBlockState(candidate.offset(dx, 0, dz)).is(this)) {
                            isCenter = false;
                            break;
                        }
                    }
                }
                if (isCenter) return candidate;
            }
        }
        return pos;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            if (player.isOnPortalCooldown()) {
                player.setPortalCooldown();
                return;
            }

            BlockPos portalCenter = findPortalCenter(level, pos);

            ResourceKey<Level> exolumenKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, new ResourceLocation("furyborn", "exolumen"));
            ResourceKey<Level> targetDim = level.dimension() == exolumenKey ? Level.OVERWORLD : exolumenKey;
            ServerLevel targetLevel = level.getServer().getLevel(targetDim);

            if (targetLevel != null) {
                targetLevel.getChunk(portalCenter.getX() >> 4, portalCenter.getZ() >> 4);
                int targetY = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, portalCenter.getX(), portalCenter.getZ());
                BlockPos targetCenter = new BlockPos(portalCenter.getX(), targetY, portalCenter.getZ());

                boolean isToExolumen = targetDim == exolumenKey;
                generatePortalStructure(targetLevel, targetCenter, isToExolumen);

                player.teleportTo(targetLevel, targetCenter.getX() + 0.5D, targetCenter.getY() + 1.0D, targetCenter.getZ() + 0.5D, player.getYRot(), player.getXRot());
                player.setPortalCooldown();
            }
        }
    }
}