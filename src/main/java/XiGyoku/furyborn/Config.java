package XiGyoku.furyborn;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Furyborn.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    static {
        BUILDER.push("Halo of Exolumen Settings");
    }

    public static final ForgeConfigSpec.DoubleValue ROBYTE_HP = BUILDER
            .defineInRange("robyteHP", 2000.0, 1.0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_HP = BUILDER
            .defineInRange("robyte_rebHP", Integer.MAX_VALUE, 1.0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_ARMOR = BUILDER
            .defineInRange("robyteArmor", 10.0, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_ARMOR = BUILDER
            .defineInRange("robyte_rebArmor", 5.0, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_ARMOR_TOUGHNESS = BUILDER
            .defineInRange("robyteArmorToughness", 5.0, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_ARMOR_TOUGHNESS = BUILDER
            .defineInRange("robyte_rebArmorToughness", 5.0, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_DAMAGE = BUILDER
            .defineInRange("robyteDamage", 1.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_DAMAGE = BUILDER
            .defineInRange("robyte_rebDamage", 1.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_LASER_DAMAGE = BUILDER
            .defineInRange("robyteLaserDamage", 0.5, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_LASER_DAMAGE = BUILDER
            .defineInRange("robyte_rebLaserDamage", 2.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue BUSTER_THROWER_DAMAGE = BUILDER
            .defineInRange("busterThrowerDamage", 1.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_SPEED = BUILDER
            .defineInRange("robyteSpeed", 0.3, 0.1, 10.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_ATTACK_SPEED = BUILDER
            .defineInRange("robyteAttackSpeed", 1.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_FOLLOW_RANGE = BUILDER
            .defineInRange("robyteFollowRange", 128.0, 16.0, 1024.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_FLYING_SPEED = BUILDER
            .defineInRange("robyteFlyingSpeed", 3.0, 0.1, 10.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_MOVEMENT_SPEED = BUILDER
            .defineInRange("robyte_rebMovementSpeed", 5.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_FLYING_SPEED = BUILDER
            .defineInRange("robyte_rebFlyingSpeed", 30.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_ATTACK_SPEED = BUILDER
            .defineInRange("robyte_rebAttackSpeed", 3.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_TELEPORT_RANGE = BUILDER
            .defineInRange("robyteTeleportRange", 6.0, 1.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_TRANSAM_TELEPORT_RANGE = BUILDER
            .defineInRange("robyteTransamTeleportRange", 12.0, 1.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_AREA_RADIUS = BUILDER
            .defineInRange("robyteAreaRadius", 64.0, 5.0, 1000.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_AREA_HEIGHT = BUILDER
            .defineInRange("robyteAreaHeight", 64.0, 5.0, 1000.0);

    public static final ForgeConfigSpec.IntValue ROBYTE_TELEPORT_COOLDOWN = BUILDER
            .defineInRange("robyteTeleportCooldown", 400, 1, 72000);

    public static final ForgeConfigSpec.IntValue ROBYTE_TRANSAM_TELEPORT_COOLDOWN = BUILDER
            .defineInRange("robyteTransamTeleportCooldown", 50, 1, 72000);

    public static final ForgeConfigSpec.IntValue ROBYTE_CANNON_MODE_TICK = BUILDER
            .defineInRange("robyteCannonModeTick", 280, 1, 6000);

    public static final ForgeConfigSpec.IntValue ROBYTE_ALL_RANGE_MODE_TICK = BUILDER
            .defineInRange("robyteAllRangeModeTick", 200, 1, 6000);

    public static final ForgeConfigSpec.IntValue ROBYTE_POWERUP_MODE_TICK = BUILDER
            .defineInRange("robytePowerupModeTick", 400, 1, 6000);

    public static final ForgeConfigSpec.IntValue ROBYTE_ROTATION_ATTACK_TICK = BUILDER
            .defineInRange("robyteRotationAttackTick", 200, 1, 6000);

    public static final ForgeConfigSpec.IntValue ROBYTE_CANNON_MODE_COOLDOWN = BUILDER
            .defineInRange("robyteCannonModeCooldown", 20, 0, 72000);

    public static final ForgeConfigSpec.IntValue ROBYTE_ALL_RANGE_MODE_COOLDOWN = BUILDER
            .defineInRange("robyteAllRangeModeCooldown", 20, 0, 72000);

    public static final ForgeConfigSpec.IntValue ROBYTE_POWERUP_MODE_COOLDOWN = BUILDER
            .defineInRange("robytePowerupModeCooldown", 200, 0, 72000);

    public static final ForgeConfigSpec.IntValue ROBYTE_ROTATION_ATTACK_COOLDOWN = BUILDER
            .defineInRange("robyteRotationAttackCooldown", 20, 0, 72000);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_ALL_RANGE_MODE_HP_THRESHOLD = BUILDER
            .defineInRange("robyteAllRangeModeHPThreshold", 0.7, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_AREA_DAMAGE = BUILDER
            .defineInRange("robyteAreaDamage", 2.0, 0.1, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_AREA_DAMAGE = BUILDER
            .defineInRange("robyte_rebAreaDamage", Float.MAX_VALUE, 0.1, Double.MAX_VALUE);

    public static final ForgeConfigSpec.BooleanValue ROBYTE_REBELLION_DO_DEATH_ATTACK = BUILDER
            .define("robyteRebellionDeathAttack", true);

    public static final ForgeConfigSpec.IntValue BUSTER_THROWER_MAX_CHARGE = BUILDER
            .defineInRange("busterThrowerMaxCharge", 2500000, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue BUSTER_THROWER_OVERCHARGE_COST = BUILDER
            .defineInRange("busterThrowerMaxCost", 2500000, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue BUSTER_THROWER_BASE_COST = BUILDER
            .defineInRange("busterThrowerBaseCost", 125000, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue BUSTER_THROWER_EXPLOSION_COST = BUILDER
            .defineInRange("busterThrowerPowerupCost", 250000, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue BUSTER_THROWER_LASER_SIZE = BUILDER
            .defineInRange("busterThrowerLaserSize", 15.0F, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue BUSTER_THROWER_LASER_LENGTH = BUILDER
            .defineInRange("busterThrowerLaserLength", 200.0D, 0.1, 400.0D);

    public static final ForgeConfigSpec.BooleanValue BUSTER_THROWER_FLEXIBLE = BUILDER
            .define("busterThrowerFlexible", true);

    public static final ForgeConfigSpec.BooleanValue HALO_LASER = BUILDER
            .define("haloLaser", true);

    public static final ForgeConfigSpec.DoubleValue HALO_LASER_DAMAGE = BUILDER
            .defineInRange("haloLaserDamage", 5.0, 0.1, 100.0);

    private static final ForgeConfigSpec.DoubleValue HALO_OFFSET_X = BUILDER
            .defineInRange("haloOffsetX", 1.0, -2.0, 2.0);

    private static final ForgeConfigSpec.DoubleValue HALO_OFFSET_Y = BUILDER
            .defineInRange("haloOffsetY", -1.0, -2.0, 2.0);

    private static final ForgeConfigSpec.DoubleValue HALO_OFFSET_Z = BUILDER
            .defineInRange("haloOffsetZ", 1.0, -2.0, 2.0);

    private static final ForgeConfigSpec.DoubleValue HALO_ROTATION_X = BUILDER
            .defineInRange("haloRotationX", 180.0, -360.0, 360.0);

    private static final ForgeConfigSpec.DoubleValue HALO_ROTATION_Y = BUILDER
            .defineInRange("haloRotationY", 0.0, -360.0, 360.0);

    private static final ForgeConfigSpec.DoubleValue HALO_ROTATION_Z = BUILDER
            .defineInRange("haloRotationZ", 0.0, -360.0, 360.0);

    public static final ForgeConfigSpec.DoubleValue HALO_SCALE = BUILDER
            .defineInRange("haloScale", 3.0, 0.1, 10.0);

    public static final ForgeConfigSpec.DoubleValue HALO_SUN_SCALE = BUILDER
            .defineInRange("haloSunScale", 0.2, 0.1, 10.0);

    public static final ForgeConfigSpec.BooleanValue HALO_INDIVIDUAL_ROTATION = BUILDER
            .define("haloIndividualRotation", true);

    public static final ForgeConfigSpec.IntValue HALO_ORBIT_COUNT = BUILDER
            .defineInRange("haloOrbitCount", 5, 1, 100);

    public static final ForgeConfigSpec.DoubleValue HALO_ORBIT_SPACING = BUILDER
            .defineInRange("haloOrbitSpacing", 0.5, 0.0, 100.0);

    public static final ForgeConfigSpec.BooleanValue HALO_UNIFY_PLANET_COLOR = BUILDER
            .define("haloUnifyPlanetColor", false);

    public static final ForgeConfigSpec.ConfigValue<String> HALO_UNIFIED_PLANET_COLOR = BUILDER
            .define("haloUnifiedPlanetColor", "#FFFFFF");

    public static final ForgeConfigSpec.DoubleValue HALO_ROTATION_SPEED_MULTIPLIER = BUILDER
            .defineInRange("haloRotationSpeedMultiplier", 0.5, 0.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue TOOLTIP_HALO_SCALE = BUILDER
            .defineInRange("tooltipHaloScale", 1.0, 0.1, 10.0);

    public static final ForgeConfigSpec.BooleanValue TOOLTIP_HALO_INDIVIDUAL_ROTATION = BUILDER
            .define("tooltipHaloIndividualRotation", true);

    public static final ForgeConfigSpec.IntValue TOOLTIP_HALO_ORBIT_COUNT = BUILDER
            .defineInRange("tooltipHaloOrbitCount", 5, 1, 100);

    public static final ForgeConfigSpec.DoubleValue TOOLTIP_HALO_ORBIT_SPACING = BUILDER
            .defineInRange("tooltipHaloOrbitSpacing", 0.2, 0.0, 100.0);

    public static final ForgeConfigSpec.BooleanValue TOOLTIP_HALO_UNIFY_PLANET_COLOR = BUILDER
            .define("tooltipHaloUnifyPlanetColor", false);

    public static final ForgeConfigSpec.ConfigValue<String> TOOLTIP_HALO_UNIFIED_PLANET_COLOR = BUILDER
            .define("tooltipHaloUnifiedPlanetColor", "#FFFFFF");

    public static final ForgeConfigSpec.DoubleValue TOOLTIP_HALO_ROTATION_SPEED_MULTIPLIER = BUILDER
            .defineInRange("tooltipHaloRotationSpeedMultiplier", 0.5, 0.0, 100.0);

    static {
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static float haloOffsetX;
    public static float haloOffsetY;
    public static float haloOffsetZ;
    public static float haloRotX;
    public static float haloRotY;
    public static float haloRotZ;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        haloOffsetX = HALO_OFFSET_X.get().floatValue();
        haloOffsetY = HALO_OFFSET_Y.get().floatValue();
        haloOffsetZ = HALO_OFFSET_Z.get().floatValue();
        haloRotX = HALO_ROTATION_X.get().floatValue();
        haloRotY = HALO_ROTATION_Y.get().floatValue();
        haloRotZ = HALO_ROTATION_Z.get().floatValue();
    }
}