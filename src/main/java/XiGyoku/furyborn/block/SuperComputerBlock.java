package XiGyoku.furyborn.block;

import XiGyoku.furyborn.entity.FuryBornEntityTypes;
import XiGyoku.furyborn.entity.RobyteEntity;
import XiGyoku.furyborn.item.FuryBornItems;
import XiGyoku.furyborn.sound.FuryBornSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class SuperComputerBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty IS_NULL = BooleanProperty.create("is_null");

    public SuperComputerBlock(Properties properties) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                .strength(2.0f, 3.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(IS_NULL, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(IS_NULL, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, IS_NULL);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        boolean isRobyte = heldItem.is(FuryBornItems.ROBYTE_DATA_MODEL.get());
        boolean isNull = heldItem.is(FuryBornItems.NULL_DATA_MODEL.get());

        if (isRobyte || isNull) {
            if (!level.isClientSide) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                level.setBlock(pos, state.setValue(IS_NULL, isNull), 3);
                if (isNull) {
                    level.playSound(null, pos, FuryBornSounds.SUPERCOMPUTER_LOADING.get(), SoundSource.BLOCKS, 0.5F, 0.5F);
                } else {
                    level.playSound(null, pos, FuryBornSounds.SUPERCOMPUTER_LOADING.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
                }
                level.scheduleTick(pos, this, 100);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        RobyteEntity robyteEntity = FuryBornEntityTypes.ROBYTE.get().create(level);

        if (robyteEntity != null) {
            robyteEntity.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 0.0F, 0.0F);
            if (state.getValue(IS_NULL)) {
                robyteEntity.setRebellion(true);
            }
            level.addFreshEntity(robyteEntity);
            level.playSound(null, pos, FuryBornSounds.ROBYTE_GETUP.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            level.setBlock(pos, state.setValue(IS_NULL, false), 3);
        }
    }
}