package XiGyoku.furyborn.block;

import XiGyoku.furyborn.Furyborn;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.SunflowerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class FuryBornBlocks {
    public static final RegistryObject<Block> T_SUPER_BLOCK = registerBlock("t_supercomputer",
            () -> new SuperComputerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Block> HALO_PROJECTOR = registerBlock("halo_projector",
            () -> new HaloProjectorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Block> MEK_DIRT = registerBlock("mek_dirt",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT).mapColor(MapColor.DIRT)));

    public static final RegistryObject<Block> MEK_GRASS_BLOCK = registerBlock("mek_grass_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK).mapColor(MapColor.GRASS)));

    public static final RegistryObject<Block> MEK_COBBLESTONE = registerBlock("mek_cobblestone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE).mapColor(MapColor.STONE)));

    public static final RegistryObject<Block> MEK_TALL_GRASS = registerBlock("mek_tall_grass",
            () -> new TallGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Block> NANOCORE_ORE = registerBlock("nanocore_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE).mapColor(MapColor.STONE).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> EXOLUMEN_PORTAL = registerBlock("exolumen_portal",
            () -> new ExolumenPortalBlock());

    public static final RegistryObject<Block> PORTAL_FRAME_MEK = registerBlock("portal_frame_mek",
            () -> new PortalFrameBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

    public static final RegistryObject<Block> SUPERCHARGED_COIL_PORTAL = registerBlock("supercharged_coil_portal",
            () -> new SuperchargedCoilPortalBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> MEKA_SUNFLOWER = registerBlock("meka_sunflower",
            () -> new SunflowerBlock(BlockBehaviour.Properties.copy(Blocks.SUNFLOWER)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
        return Furyborn.BLOCKS.register(name, block);
    }

    public static void register(IEventBus eventBus){
    }
}