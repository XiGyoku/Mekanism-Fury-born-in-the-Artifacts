package XiGyoku.furyborn;

import XiGyoku.furyborn.block.FuryBornBlocks;
import XiGyoku.furyborn.blockentity.FuryBornBlockEntities;
import XiGyoku.furyborn.client.entity.*;
import XiGyoku.furyborn.client.item.HaloOfExolumenRenderer;
import XiGyoku.furyborn.client.item.ModelBusterThrower;
import XiGyoku.furyborn.effect.FuryBornEffects;
import XiGyoku.furyborn.entity.FuryBornEntityTypes;
import XiGyoku.furyborn.item.FuryBornItems;
import XiGyoku.furyborn.network.FuryBornNetwork;
import XiGyoku.furyborn.sound.FuryBornSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Furyborn.MODID)
public class Furyborn {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "furyborn";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "furyborn" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "furyborn" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "furyborn" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a creative tab with the id "furyborn:furyborn_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> FURYBORN_TAB = CREATIVE_MODE_TABS.register("furyborn_tab", () -> CreativeModeTab.builder().icon(() -> FuryBornItems.ROBYTE_DATA_MODEL.get().getDefaultInstance()).title(Component.translatable("itemGroup.furyborn_tab")).displayItems((parameters, output) -> {
        output.accept(FuryBornItems.T_SUPER_BLOCK_ITEM.get());
        output.accept(FuryBornItems.ROBYTE_DATA_MODEL.get());
        output.accept(FuryBornItems.ROBIT_DATA_MODEL.get());
        output.accept(FuryBornItems.BUSTER_THROWER.get());
        output.accept(FuryBornItems.NULL_DATA_MODEL.get());
        output.accept(FuryBornItems.HALO_OF_EXOLUMEN.get());
        output.accept(FuryBornItems.ROBYTE_SPAWN_EGG.get());
        output.accept(FuryBornItems.ROBIX_SPAWN_EGG.get());
        output.accept(FuryBornItems.SUNRAISER_DRIVE.get());
        output.accept(FuryBornItems.SYSTEM_XROSSALIVE.get());
        output.accept(FuryBornItems.HALO_PROJECTOR_ITEM.get());
        output.accept(FuryBornItems.MEK_DIRT.get());
        output.accept(FuryBornItems.MEK_GRASS_BLOCK.get());
        output.accept(FuryBornItems.MEK_COBBLESTONE.get());
        output.accept(FuryBornItems.MEK_TALL_GRASS.get());
        output.accept(FuryBornItems.EXOLUMEN_PORTAL_ITEM.get());
        output.accept(FuryBornItems.PORTAL_FRAME_MEK.get());
        output.accept(FuryBornItems.ROBYTE_CHIP.get());
        output.accept(FuryBornItems.ROBYTE_CHIP_AWAKEN.get());
        output.accept(FuryBornItems.SUPERCHAGED_COIL_PORTAL_ITEM.get());
        output.accept(FuryBornItems.NANOCORE_ORE_ITEM.get());
        output.accept(FuryBornItems.NANOCORE_INGOT.get());
        output.accept(FuryBornItems.RAW_NANOCORE.get());
        output.accept(FuryBornItems.UNKNOWN_CIRCUIT.get());
        output.accept(FuryBornItems.MEKA_SUNFLOWER.get());
        output.accept(FuryBornItems.EXOLUMEN_DATAMODEL.get());
        output.accept(FuryBornItems.ROADBIKE_BIT_ITEM.get());
    }).build());

    public Furyborn() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Custom Registration
        FuryBornBlocks.register(modEventBus);
        FuryBornItems.register(modEventBus);
        FuryBornBlockEntities.register(modEventBus);
        FuryBornEffects.register(modEventBus);
        FuryBornSounds.register(modEventBus);
        FuryBornNetwork.register();

        FuryBornEntityTypes.register(modEventBus);

        // Geckolib Initialization
        GeckoLib.initialize();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("これで何もかも終わりだ…\n任務完了…");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("量子コンピュータ ヴェーダが起動シークエンスに突入...");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("クライアントかな？いや、違うな。\nクライアントはもっと、バァーって動くもんな...");

            EntityRenderers.register(FuryBornEntityTypes.ROBYTE.get(), RobyteRenderer::new);
            EntityRenderers.register(FuryBornEntityTypes.ROBYTE_AREA.get(), RobyteAreaRenderer::new);
            EntityRenderers.register(FuryBornEntityTypes.ROBYTE_BIT_LASER.get(), RobyteBitLaserRenderer::new);
            EntityRenderers.register(FuryBornEntityTypes.ROBYTE_LASER.get(), RobyteLaserRenderer::new);
            EntityRenderers.register(FuryBornEntityTypes.ROBIX.get(), RobixRenderer::new);
            EntityRenderers.register(FuryBornEntityTypes.ROADBIKE_BIT.get(), RoadBikeBitRenderer::new);

            event.enqueueWork(() -> {
                CuriosRendererRegistry.register(FuryBornItems.HALO_OF_EXOLUMEN.get(), HaloOfExolumenRenderer::new);
                CuriosRendererRegistry.register(FuryBornItems.SUNRAISER_DRIVE.get(), SunRaiserDriveRenderer::new);
            });
        }
    }
}
