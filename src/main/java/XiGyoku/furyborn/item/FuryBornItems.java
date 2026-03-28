package XiGyoku.furyborn.item;

import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.block.FuryBornBlocks;
import XiGyoku.furyborn.entity.FuryBornEntityTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

public class FuryBornItems {
    public static final RegistryObject<Item> T_SUPER_BLOCK_ITEM =
            Furyborn.ITEMS.register("t_supercomputer",
                    () -> new BlockItem(FuryBornBlocks.T_SUPER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> ROBIT_DATA_MODEL =
            Furyborn.ITEMS.register("robit_datamodel",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ROBYTE_DATA_MODEL =
            Furyborn.ITEMS.register("robyte_datamodel",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ROBYTE_SPAWN_EGG =
            Furyborn.ITEMS.register("robyte_spawn_egg",
                    () -> new ForgeSpawnEggItem(FuryBornEntityTypes.ROBYTE, 0xf8f8ff, 0x00fa9a,
                            new Item.Properties()));

    public static void register(IEventBus eventBus){
    }
}