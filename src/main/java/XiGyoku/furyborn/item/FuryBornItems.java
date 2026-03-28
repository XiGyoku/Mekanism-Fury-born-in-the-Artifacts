package XiGyoku.furyborn.item;

import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.block.FuryBornBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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

    public static void register(IEventBus eventBus){
    }
}