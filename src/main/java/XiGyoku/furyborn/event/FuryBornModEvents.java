package XiGyoku.furyborn.event;

import XiGyoku.furyborn.Furyborn;
import XiGyoku.furyborn.entity.FuryBornEntityTypes;
import XiGyoku.furyborn.entity.RobixEntity;
import XiGyoku.furyborn.entity.RobyteEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Furyborn.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FuryBornModEvents {

    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(FuryBornEntityTypes.ROBYTE.get(), RobyteEntity.createAttributes());
//        event.put(FuryBornEntityTypes.ROBIX.get(), RobixEntity.createAttributes().build());
    }
}