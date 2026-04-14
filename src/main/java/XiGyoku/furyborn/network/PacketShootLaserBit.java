package XiGyoku.furyborn.network;

import XiGyoku.furyborn.Config;
import XiGyoku.furyborn.client.entity.RobyteBitLaserModel;
import XiGyoku.furyborn.entity.FuryBornEntityTypes;
import XiGyoku.furyborn.entity.RobyteBitLaserEntity;
import XiGyoku.furyborn.item.HaloOfExolumenItem;
import XiGyoku.furyborn.item.ItemBusterThrower;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

public class PacketShootLaserBit {
    public PacketShootLaserBit() {}

    public PacketShootLaserBit(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                if (CuriosApi.getCuriosHelper().findEquippedCurio(stack -> stack.getItem() instanceof HaloOfExolumenItem, player).isPresent()) {
                    RobyteBitLaserEntity bit = new RobyteBitLaserEntity(FuryBornEntityTypes.ROBYTE_BIT_LASER.get(), player.level());
                    TargetingConditions conditions = TargetingConditions.forCombat()
                            .range(128.0)
                            .selector(e -> e != player && e.isAlive());

                    bit.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                    bit.setOwner(player);
                    AABB searchBox = player.getBoundingBox().inflate(128.0);
                    LivingEntity closestTarget = player.level().getNearestEntity(
                            LivingEntity.class,
                            conditions,
                            player,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            searchBox
                    );
                    bit.setTarget(closestTarget);
                    player.level().addFreshEntity(bit);
                }
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}