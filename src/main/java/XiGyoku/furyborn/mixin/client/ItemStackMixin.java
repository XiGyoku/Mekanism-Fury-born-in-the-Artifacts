package XiGyoku.furyborn.mixin.client;

import XiGyoku.furyborn.item.FuryBornItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void furyborn$injectHaloName(CallbackInfoReturnable<Component> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (stack.getItem() == FuryBornItems.HALO_OF_EXOLUMEN.get()) {
            String originalName = cir.getReturnValue().getString();
            cir.setReturnValue(Component.literal(originalName + ":_FB_"));
        }
        if (stack.getItem() == FuryBornItems.BUSTER_THROWER.get()) {
            String originalName = cir.getReturnValue().getString();
            cir.setReturnValue(Component.literal(originalName + ":_FB_" + ":_FBS_"));
        }
        if (stack.getItem() == FuryBornItems.EXOLUMEN_DATAMODEL.get()) {
            String originalName = cir.getReturnValue().getString();
            cir.setReturnValue(Component.literal(originalName + ":_FB_"));
        }
    }
}