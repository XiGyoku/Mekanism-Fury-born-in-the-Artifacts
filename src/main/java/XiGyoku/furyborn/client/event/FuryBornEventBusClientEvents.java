package XiGyoku.furyborn.client.event;

import XiGyoku.furyborn.item.FuryBornItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FuryBornEventBusClientEvents {
    private static volatile Set<Item> cachedFuryBornItems = null;

    private static final List<RegistryObject<Item>> FURYBORN_REGISTRY_OBJECTS = List.of(
            FuryBornItems.HALO_OF_EXOLUMEN
    );

    private static Set<Item> buildCache() {
        Set<Item> set = new HashSet<>();
        for (RegistryObject<Item> ro : FURYBORN_REGISTRY_OBJECTS) {
            if (ro.isPresent()) set.add(ro.get());
        }
        return Collections.unmodifiableSet(set);
    }

    private static Set<Item> getFuryBornItemsCache() {
        Set<Item> local = cachedFuryBornItems;
        if (local == null) {
            synchronized (FuryBornEventBusClientEvents.class) {
                local = cachedFuryBornItems;
                if (local == null) {
                    local = buildCache();
                    cachedFuryBornItems = local;
                }
            }
        }
        return local;
    }

    private static boolean isFurybornItem(ItemStack stack) {
        Item item = stack.getItem();
        return getFuryBornItemsCache().contains(item);
    }

//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
//        ItemStack stack = event.getItemStack();
//        if (!isFurybornItem(stack)) return;
//
//        event.setCanceled(true);
//    }
}
