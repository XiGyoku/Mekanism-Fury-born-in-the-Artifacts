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

import java.util.ArrayList;
import java.util.Collections;
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

    public static final RegistryObject<Item> HALO_PROJECTOR_ITEM =
            Furyborn.ITEMS.register("halo_projector",
                    () -> new BlockItem(FuryBornBlocks.HALO_PROJECTOR.get(), new Item.Properties().rarity(FuryBornRarities.STARRY)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("block.furyborn.halo_projector_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> ROBIT_DATA_MODEL =
            Furyborn.ITEMS.register("robit_datamodel",
                    () -> new Item(new Item.Properties().rarity(FuryBornRarities.STARRY)) {
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("item.furyborn.robit_datamodel_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> NULL_DATA_MODEL =
            Furyborn.ITEMS.register("null_datamodel",
                    () -> new Item(new Item.Properties().rarity(FuryBornRarities.RED_STARRY)) {
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("item.furyborn.null_datamodel_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> ROBYTE_DATA_MODEL =
            Furyborn.ITEMS.register("robyte_datamodel",
                    () -> new Item(new Item.Properties().rarity(FuryBornRarities.RED_STARRY)) {
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("item.furyborn.robyte_datamodel_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> ROBYTE_CHIP =
            Furyborn.ITEMS.register("robyte_chip",
                    () -> new Item(new Item.Properties()) {
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("item.furyborn.robyte_chip_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> ROBYTE_CHIP_AWAKEN =
            Furyborn.ITEMS.register("robyte_chip_awaken",
                    () -> new Item(new Item.Properties().rarity(FuryBornRarities.STARRY)) {
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("item.furyborn.robyte_chip_awaken_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> ROBYTE_SPAWN_EGG =
            Furyborn.ITEMS.register("robyte_spawn_egg",
                    () -> new ForgeSpawnEggItem(FuryBornEntityTypes.ROBYTE, 0xf8f8ff, 0xff0000,
                            new Item.Properties()));

    public static final RegistryObject<Item> BUSTER_THROWER =
            Furyborn.ITEMS.register("buster_thrower",
                    () -> new ItemBusterThrower(new Item.Properties().rarity(FuryBornRarities.STARRY).stacksTo(1)));

    public static final RegistryObject<Item> HALO_OF_EXOLUMEN =
            Furyborn.ITEMS.register("halo_of_exolumen",
                    () -> new HaloOfExolumenItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUNRAISER_DRIVE =
            Furyborn.ITEMS.register("sunraiser_drive",
                    () -> new SunRaiserDriveItem(new Item.Properties().rarity(FuryBornRarities.STARRY).stacksTo(1)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("item.furyborn.sunraiser_drive_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> SYSTEM_XROSSALIVE =
            Furyborn.ITEMS.register("system_xrossalive",
                    () -> new SystemXrossAliveItem(new Item.Properties().rarity(FuryBornRarities.STARRY).stacksTo(1)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("item.furyborn.system_xrossalive_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> EXOLUMEN_PORTAL_ITEM =
            Furyborn.ITEMS.register("exolumen_portal",
                    () -> new BlockItem(FuryBornBlocks.EXOLUMEN_PORTAL.get(), new Item.Properties().rarity(FuryBornRarities.STARRY)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("block.furyborn.exolumen_portal_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> MEK_DIRT =
            Furyborn.ITEMS.register("mek_dirt",
                    () -> new BlockItem(FuryBornBlocks.MEK_DIRT.get(), new Item.Properties().rarity(FuryBornRarities.STARRY)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("block.furyborn.mek_dirt_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> MEK_COBBLESTONE =
            Furyborn.ITEMS.register("mek_cobblestone",
                    () -> new BlockItem(FuryBornBlocks.MEK_COBBLESTONE.get(), new Item.Properties().rarity(FuryBornRarities.STARRY)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("block.furyborn.mek_cobblestone_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> MEK_TALL_GRASS =
            Furyborn.ITEMS.register("mek_tall_grass",
                    () -> new BlockItem(FuryBornBlocks.MEK_TALL_GRASS.get(), new Item.Properties().rarity(FuryBornRarities.STARRY)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("block.furyborn.mek_tall_grass_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> MEK_GRASS_BLOCK =
            Furyborn.ITEMS.register("mek_grass_block",
                    () -> new BlockItem(FuryBornBlocks.MEK_GRASS_BLOCK.get(), new Item.Properties().rarity(FuryBornRarities.STARRY)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("block.furyborn.mek_grass_block_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> PORTAL_FRAME_MEK =
            Furyborn.ITEMS.register("portal_frame_mek",
                    () -> new BlockItem(FuryBornBlocks.PORTAL_FRAME_MEK.get(), new Item.Properties().rarity(FuryBornRarities.STARRY)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("block.furyborn.portal_frame_mek_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static final RegistryObject<Item> SUPERCHAGED_COIL_PORTAL_ITEM =
            Furyborn.ITEMS.register("supercharged_coil_portal",
                    () -> new BlockItem(FuryBornBlocks.SUPERCHARGED_COIL_PORTAL.get(), new Item.Properties().rarity(FuryBornRarities.STARRY)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
                            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
                            pTooltipComponents.add(Component.translatable("block.furyborn.supercharged_coil_portal_desc").withStyle(ChatFormatting.GRAY));
                        }
                    });

    public static void register(IEventBus eventBus){
    }

    private static List<Item> cachedAllItems = null;

    public static List<Item> getAllItems() {
        if (cachedAllItems == null) {
            List<Item> list = new ArrayList<>();
            Furyborn.ITEMS.getEntries().stream()
                    .filter(RegistryObject::isPresent)
                    .map(RegistryObject::get)
                    .forEach(list::add);
            cachedAllItems = Collections.unmodifiableList(list);
        }
        return cachedAllItems;
    }
}