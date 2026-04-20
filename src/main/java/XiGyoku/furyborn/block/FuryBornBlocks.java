package XiGyoku.furyborn.block;

import XiGyoku.furyborn.Furyborn;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class FuryBornBlocks {
    public static final RegistryObject<Block> T_SUPER_BLOCK = registerBlock("t_supercomputer",
            () -> new SuperComputerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final RegistryObject<Block> HALO_PROJECTOR = registerBlock("halo_projector",
            () -> new HaloProjectorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
        return Furyborn.BLOCKS.register(name, block);
    }

    public static void register(IEventBus eventBus){
    }
}