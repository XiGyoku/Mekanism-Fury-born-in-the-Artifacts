package XiGyoku.furyborn.item;

import XiGyoku.furyborn.entity.FuryBornEntityTypes;
import XiGyoku.furyborn.entity.RoadBikeBitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class RoadBikeBitItem extends Item {
    public RoadBikeBitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        
        ItemStack itemstack = context.getItemInHand();
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos spawnPos = blockpos.relative(direction);

        RoadBikeBitEntity bike = FuryBornEntityTypes.ROADBIKE_BIT.get().spawn((ServerLevel) level, itemstack, context.getPlayer(), spawnPos, MobSpawnType.SPAWN_EGG, true, !blockpos.equals(spawnPos) && direction == Direction.UP);
        
        if (bike != null) {
            itemstack.shrink(1);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}