package XiGyoku.furyborn.event;

import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.item.FuryBornItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Furyborn.MODID)
public class HoeAttackEvent {

    @SubscribeEvent
    public static void onEntityAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        if (player.level().isClientSide) return;

        Optional<SlotResult> optionalCurio = CuriosApi.getCuriosHelper().findFirstCurio(player, FuryBornItems.HALO_OF_EXOLUMEN.get());
        if (optionalCurio.isPresent()) {
            ItemStack haloStack = optionalCurio.get().stack();
            CompoundTag nbt = haloStack.getOrCreateTag();
            nbt.putInt("TargetEntityID", target.getId());
            nbt.putInt("LaserTick", 0);
            haloStack.setTag(nbt);
        }
    }
}