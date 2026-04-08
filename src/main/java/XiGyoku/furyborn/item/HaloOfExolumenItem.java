package XiGyoku.furyborn.item;

import XiGyoku.furyborn.entity.FuryBornEntityTypes;
import XiGyoku.furyborn.entity.RobyteBitLaserEntity;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class HaloOfExolumenItem extends Item implements ICurioItem {

    public HaloOfExolumenItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "Halo Max Health", 9.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(Attributes.ARMOR, new AttributeModifier(uuid, "Halo Armor", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        if (entity instanceof Player player) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            if (!player.level().isClientSide) {
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20.0f);
            }
        }

        if (!entity.level().isClientSide) {
            entity.clearFire();
            entity.setAirSupply(entity.getMaxAirSupply());

            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WEAKNESS);
            entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

            if (entity.tickCount % 20 == 0 && entity.getHealth() < entity.getMaxHealth()) {
                entity.heal(entity.getMaxHealth() * 0.05F);
            }

            if (entity instanceof Player player) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack invStack = player.getInventory().getItem(i);
                    if (!invStack.isEmpty()) {
                        invStack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                            energy.receiveEnergy(Integer.MAX_VALUE, false);
                        });
                    }
                }
            }
        }

        if (entity.level().isClientSide) return;
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains("TargetEntityID")) {
            int targetId = nbt.getInt("TargetEntityID");
            Entity target = entity.level().getEntity(targetId);
            if (target instanceof LivingEntity && target.isAlive()) {
                int tickCount = nbt.getInt("LaserTick");
                tickCount++;
                if (tickCount >= 20) {
                    fireBitLaser(entity, (LivingEntity) target);
                    tickCount = 0;
                }
                nbt.putInt("LaserTick", tickCount);
            } else {
                nbt.remove("TargetEntityID");
                nbt.remove("LaserTick");
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    private void fireBitLaser(LivingEntity player, LivingEntity target) {
        if (!player.level().isClientSide) {
            RobyteBitLaserEntity bit = new RobyteBitLaserEntity(FuryBornEntityTypes.ROBYTE_BIT_LASER.get(), player.level());
            bit.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            bit.setOwner(player);
            bit.setTarget(target);
            bit.setDamage(target.getHealth() / 10.0F);
            player.level().addFreshEntity(bit);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.furyborn.halo_of_exolumen_desc1"));
        pTooltipComponents.add(Component.translatable("item.furyborn.halo_of_exolumen_desc2"));
        pTooltipComponents.add(Component.translatable("item.furyborn.halo_of_exolumen_desc3"));
        pTooltipComponents.add(Component.translatable("item.furyborn.halo_of_exolumen_desc4"));
        pTooltipComponents.add(Component.translatable("item.furyborn.halo_of_exolumen_desc5"));
    }

    @Override
    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
        return ICurio.DropRule.ALWAYS_KEEP;
    }
}