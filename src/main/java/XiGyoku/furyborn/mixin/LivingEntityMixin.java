package XiGyoku.furyborn.mixin;

import XiGyoku.furyborn.entity.RobyteEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 1000)
public abstract class LivingEntityMixin extends Entity {

    protected LivingEntityMixin(EntityType<? extends Entity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true)
    public void furyborn$getMaxHealth(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof RobyteEntity robyte) {
            if (robyte.isRebellion()) {
                cir.setReturnValue((float) Integer.MAX_VALUE);
            } else {
                cir.setReturnValue(2000.0f);
            }
        }
    }
}