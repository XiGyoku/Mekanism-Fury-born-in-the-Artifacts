package XiGyoku.furyborn.item;

import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.block.FuryBornBlocks;
import XiGyoku.furyborn.entity.FuryBornEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FuryBornItems {

    public static final RegistryObject<Item> T_SUPER_BLOCK_ITEM =
            Furyborn.ITEMS.register("t_supercomputer",
                    () -> new BlockItem(FuryBornBlocks.T_SUPER_BLOCK.get(), new Item.Properties()) {
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("item.furyborn.t_supercomputer_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> ROBIT_DATA_MODEL =
            Furyborn.ITEMS.register("robit_datamodel",
                    () -> new Item(new Item.Properties()) {
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("item.furyborn.robit_datamodel_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> ROBYTE_DATA_MODEL =
            Furyborn.ITEMS.register("robyte_datamodel",
                    () -> new Item(new Item.Properties()) {
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("item.furyborn.robyte_datamodel_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> ROBYTE_SPAWN_EGG =
            Furyborn.ITEMS.register("robyte_spawn_egg",
                    () -> new ForgeSpawnEggItem(FuryBornEntityTypes.ROBYTE, 0xf8f8ff, 0xff0000,
                            new Item.Properties()));

    public static void register(IEventBus eventBus){
    }
}