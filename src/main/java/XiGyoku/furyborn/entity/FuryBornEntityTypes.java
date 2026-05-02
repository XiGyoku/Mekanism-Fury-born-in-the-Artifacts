package XiGyoku.furyborn.entity;

import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.client.entity.RobyteBitLaserRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import XiGyoku.furyborn.entity.RobyteBitLaserEntity;

public class FuryBornEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Furyborn.MODID);

    public static final RegistryObject<EntityType<RobyteEntity>> ROBYTE =
            ENTITY_TYPES.register("robyte",
                    () -> EntityType.Builder.of(RobyteEntity::new, MobCategory.MONSTER)
                            .sized(0.8f,3.0f)
                            .build(new ResourceLocation(Furyborn.MODID, "robyte").toString()));

    public static final RegistryObject<EntityType<RobyteAreaEntity>> ROBYTE_AREA =
            ENTITY_TYPES.register("robyte_area",
                    () -> EntityType.Builder.of(RobyteAreaEntity::new, MobCategory.MISC)
                            .sized(0.8f,3.0f)
                            .clientTrackingRange(64)
                            .build(new ResourceLocation(Furyborn.MODID, "robyte_area").toString()));

    public static final RegistryObject<EntityType<RobyteBitLaserEntity>> ROBYTE_BIT_LASER =
            ENTITY_TYPES.register("robyte_bit_laser",
                    () -> EntityType.Builder.of(RobyteBitLaserEntity::new, MobCategory.MISC)
                            .sized(1.0f,1.0f)
                            .clientTrackingRange(64)
                            .build(new ResourceLocation(Furyborn.MODID, "robyte_bit_laser").toString()));

    public static final RegistryObject<EntityType<RobyteLaserEntity>> ROBYTE_LASER =
            ENTITY_TYPES.register("robyte_laser",
                    () -> EntityType.Builder.of(RobyteLaserEntity::new, MobCategory.MISC)
                            .sized(0.5f,0.5f)
                            .clientTrackingRange(64)
                            .build(new ResourceLocation(Furyborn.MODID, "robyte_laser").toString()));

    public static final RegistryObject<EntityType<RobixEntity>> ROBIX =
            ENTITY_TYPES.register("robix",
                    () -> EntityType.Builder.of(RobixEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build("robix"));

    public static final RegistryObject<EntityType<RoadBikeBitEntity>> ROADBIKE_BIT =
            ENTITY_TYPES.register("roadbike_bit",
                    () -> EntityType.Builder.of(RoadBikeBitEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(64)
                            .build("roadbike_bit"));

    public static void register(IEventBus eventBus) {
            ENTITY_TYPES.register(eventBus);
    }
}
