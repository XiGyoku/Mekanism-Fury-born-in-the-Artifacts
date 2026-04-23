package XiGyoku.furyborn.blockentity;

import XiGyoku.furyborn.block.FuryBornBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FuryBornBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "furyborn");

    public static final RegistryObject<BlockEntityType<HaloProjectorBlockEntity>> HALO_PROJECTOR = BLOCK_ENTITIES.register("halo_projector",
            () -> BlockEntityType.Builder.of(HaloProjectorBlockEntity::new, FuryBornBlocks.HALO_PROJECTOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<ExolumenControllerBlockEntity>> EXOLUMEN_CONTROLLER =
            BLOCK_ENTITIES.register("exolumen_controller",
                    () -> BlockEntityType.Builder.of(ExolumenControllerBlockEntity::new, FuryBornBlocks.SUPERCHARGED_COIL_PORTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<PortalFrameBlockEntity>> PORTAL_FRAME_MEK =
            BLOCK_ENTITIES.register("portal_frame_mek",
                    () -> BlockEntityType.Builder.of(PortalFrameBlockEntity::new, FuryBornBlocks.PORTAL_FRAME_MEK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}