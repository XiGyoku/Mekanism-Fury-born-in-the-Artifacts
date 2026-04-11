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

    private static final ForgeConfigSpec.DoubleValue HALO_OFFSET_X = BUILDER
            .comment("X offset from player for Halo of Exolumen")
            .defineInRange("haloOffsetX", 1.0, -2.0, 2.0);

    private static final ForgeConfigSpec.DoubleValue HALO_OFFSET_Y = BUILDER
            .comment("Y offset from player for Halo of Exolumen")
            .defineInRange("haloOffsetY", -1.0, -2.0, 2.0);

    private static final ForgeConfigSpec.DoubleValue HALO_OFFSET_Z = BUILDER
            .comment("Z offset from player for Halo of Exolumen")
            .defineInRange("haloOffsetZ", 1.0, -2.0, 2.0);

    private static final ForgeConfigSpec.DoubleValue HALO_ROTATION_X = BUILDER
            .comment("X rotation for Halo of Exolumen (Degrees)")
            .defineInRange("haloRotationX", 180.0, -360.0, 360.0);

    private static final ForgeConfigSpec.DoubleValue HALO_ROTATION_Y = BUILDER
            .comment("Y rotation for Halo of Exolumen (Degrees)")
            .defineInRange("haloRotationY", 0.0, -360.0, 360.0);

    private static final ForgeConfigSpec.DoubleValue HALO_ROTATION_Z = BUILDER
            .comment("Z rotation for Halo of Exolumen (Degrees)")
            .defineInRange("haloRotationZ", 0.0, -360.0, 360.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_HP = BUILDER
            .comment("Base HP for Robyte")
            .defineInRange("robyteHP", 2000.0, 1.0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_HP = BUILDER
            .comment("Base HP for Boosted Robyte")
            .defineInRange("robyte_rebHP", Integer.MAX_VALUE, 1.0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_ARMOR = BUILDER
            .comment("Armor value for Robyte")
            .defineInRange("robyteArmor", 10.0, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_ARMOR = BUILDER
            .comment("Armor value for Boosted Robyte")
            .defineInRange("robyte_rebArmor", 5.0, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_ARMOR_TOUGHNESS = BUILDER
            .comment("Armor toughness for Robyte")
            .defineInRange("robyteArmorToughness", 5.0, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_ARMOR_TOUGHNESS = BUILDER
            .comment("Armor toughness for Boosted Robyte")
            .defineInRange("robyte_rebArmorToughness", 5.0, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_DAMAGE = BUILDER
            .comment("Damage multiplier for Robyte")
            .defineInRange("robyteDamage", 1.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_DAMAGE = BUILDER
            .comment("Damage multiplier for Boosted Robyte")
            .defineInRange("robyte_rebDamage", 1.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_LASER_DAMAGE = BUILDER
            .comment("Damage multiplier for Robyte Laser")
            .defineInRange("robyteLaserDamage", 0.5, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_LASER_DAMAGE = BUILDER
            .comment("Damage multiplier for Boosted Robyte Laser")
            .defineInRange("robyte_rebLaserDamage", 2.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue BUSTER_THROWER_DAMAGE = BUILDER
            .comment("Damage multiplier for Buster Thrower")
            .defineInRange("busterThrowerDamage", 1.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_SPEED = BUILDER
            .comment("Base speed for Robyte")
            .defineInRange("robyteSpeed", 0.3, 0.1, 10.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_ATTACK_SPEED = BUILDER
            .comment("Attack speed for Robyte")
            .defineInRange("robyteAttackSpeed", 1.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_FOLLOW_RANGE = BUILDER
            .comment("Follow range for Robyte")
            .defineInRange("robyteFollowRange", 128.0, 16.0, 1024.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_FLYING_SPEED = BUILDER
            .comment("Flying speed for Robyte")
            .defineInRange("robyteFlyingSpeed", 3.0, 0.1, 10.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_MOVEMENT_SPEED = BUILDER
            .comment("Movement speed for Boosted Robyte")
            .defineInRange("robyte_rebMovementSpeed", 5.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_FLYING_SPEED = BUILDER
            .comment("Flying speed for Boosted Robyte")
            .defineInRange("robyte_rebFlyingSpeed", 30.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_ATTACK_SPEED = BUILDER
            .comment("Attack speed for Boosted Robyte")
            .defineInRange("robyte_rebAttackSpeed", 3.0, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_TELEPORT_RANGE = BUILDER
            .comment("Teleport range for Robyte")
            .defineInRange("robyteTeleportRange", 6.0, 1.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_TRANSAM_TELEPORT_RANGE = BUILDER
            .comment("Teleport range for Powerup Robyte")
            .defineInRange("robyteTransamTeleportRange", 12.0, 1.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_AREA_RADIUS = BUILDER
            .comment("Radius (X and Z direction) of Robyte Area")
            .defineInRange("robyteAreaRadius", 64.0, 5.0, 1000.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_AREA_HEIGHT = BUILDER
            .comment("Height (Y direction) of Robyte Area")
            .defineInRange("robyteAreaHeight", 64.0, 5.0, 1000.0);

    public static final ForgeConfigSpec.IntValue ROBYTE_TELEPORT_COOLDOWN = BUILDER
            .comment("Cooldown (in ticks) for Robyte teleportation")
            .defineInRange("robyteTeleportCooldown", 400, 1, 72000);

    public static final ForgeConfigSpec.IntValue ROBYTE_TRANSAM_TELEPORT_COOLDOWN = BUILDER
            .comment("Cooldown (in ticks) for Powerup Robyte teleportation")
            .defineInRange("robyteTransamTeleportCooldown", 50, 1, 72000);

    public static final ForgeConfigSpec.IntValue ROBYTE_CANNON_MODE_TICK = BUILDER
            .comment("Duration (in ticks) for Robyte's Cannon Mode attack")
            .defineInRange("robyteCannonModeTick", 280, 1, 6000);

    public static final ForgeConfigSpec.IntValue ROBYTE_ALL_RANGE_MODE_TICK = BUILDER
            .comment("Duration (in ticks) for Robyte's All-Range Mode attack")
            .defineInRange("robyteAllRangeModeTick", 200, 1, 6000);

    public static final ForgeConfigSpec.IntValue ROBYTE_POWERUP_MODE_TICK = BUILDER
            .comment("Duration (in ticks) for Robyte's Powerup Mode attack")
            .defineInRange("robytePowerupModeTick", 400, 1, 6000);

    public static final ForgeConfigSpec.IntValue ROBYTE_ROTATION_ATTACK_TICK = BUILDER
            .comment("Duration (in ticks) for Robyte's Rotation Attack")
            .defineInRange("robyteRotationAttackTick", 200, 1, 6000);

    public static final ForgeConfigSpec.IntValue ROBYTE_CANNON_MODE_COOLDOWN = BUILDER
            .comment("Cooldown (in ticks) for Robyte to use Cannon Mode again")
            .defineInRange("robyteCannonModeCooldown", 20, 0, 72000);

    public static final ForgeConfigSpec.IntValue ROBYTE_ALL_RANGE_MODE_COOLDOWN = BUILDER
            .comment("Cooldown (in ticks) for Robyte to use All-Range Mode again")
            .defineInRange("robyteAllRangeModeCooldown", 20, 0, 72000);

    public static final ForgeConfigSpec.IntValue ROBYTE_POWERUP_MODE_COOLDOWN = BUILDER
            .comment("Cooldown (in ticks) for Robyte to use Powerup Mode again")
            .defineInRange("robytePowerupModeCooldown", 200, 0, 72000);

    public static final ForgeConfigSpec.IntValue ROBYTE_ROTATION_ATTACK_COOLDOWN = BUILDER
            .comment("Cooldown (in ticks) for Robyte to use Rotation Attack again")
            .defineInRange("robyteRotationAttackCooldown", 20, 0, 72000);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_ALL_RANGE_MODE_HP_THRESHOLD = BUILDER
            .comment("HP threshold (as percentage) for Robyte to use All-Range Mode")
            .defineInRange("robyteAllRangeModeHPThreshold", 0.7, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_AREA_DAMAGE = BUILDER
            .comment("Damage for Robyte's area damage")
            .defineInRange("robyteAreaDamage", 2.0, 0.1, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue ROBYTE_REBELLION_AREA_DAMAGE = BUILDER
            .comment("Damage for Boosted Robyte's area damage")
            .defineInRange("robyte_rebAreaDamage", Float.MAX_VALUE, 0.1, Double.MAX_VALUE);

    public static final ForgeConfigSpec.BooleanValue ROBYTE_REBELLION_DO_DEATH_ATTACK = BUILDER
            .comment("Whether Boosted Robyte performs a powerful attack upon death")
            .define("robyteRebellionDeathAttack", true);

    public static final ForgeConfigSpec.IntValue BUSTER_THROWER_MAX_CHARGE = BUILDER
            .comment("Maximum charge (in FE) for Buster Thrower")
            .defineInRange("busterThrowerMaxCharge", 2500000, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue BUSTER_THROWER_OVERCHARGE_COST = BUILDER
            .comment("Energy cost for Buster Thrower (for Overcharge Mode)")
            .defineInRange("busterThrowerMaxCost", 2500000, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue BUSTER_THROWER_BASE_COST = BUILDER
            .comment("Energy cost for Buster Thrower (for Normal Mode)")
            .defineInRange("busterThrowerBaseCost", 125000, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue BUSTER_THROWER_EXPLOSION_COST = BUILDER
            .comment("Energy cost for Buster Thrower when in explosion Mode")
            .defineInRange("busterThrowerPowerupCost", 250000, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue BUSTER_THROWER_LASER_SIZE = BUILDER
            .comment("Size for Buster Thrower's laser")
            .defineInRange("busterThrowerLaserSize", 15.0F, 0.1, 100.0);

    public static final ForgeConfigSpec.DoubleValue BUSTER_THROWER_LASER_LENGTH = BUILDER
            .comment("Length for Buster Thrower's laser")
            .defineInRange("busterThrowerLaserLength", 200.0D, 0.1, 400.0D);

    public static final ForgeConfigSpec.BooleanValue BUSTER_THROWER_FLEXIBLE = BUILDER
            .comment("Whether the Buster Thrower can be changed between modes.")
            .define("busterThrowerFlexible", true);

    public static final ForgeConfigSpec.BooleanValue HALO_LASER = BUILDER
            .comment("Whether the Halo of Exolumen's laser is enabled.")
            .define("haloLaser", true);

    public static final ForgeConfigSpec.DoubleValue HALO_LASER_DAMAGE = BUILDER
            .comment("Damage percentage for the Halo of Exolumen's laser.")
            .defineInRange("haloLaserDamage", 5.0, 0.1, 100.0);

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