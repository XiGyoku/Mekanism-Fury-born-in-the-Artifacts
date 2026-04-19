package XiGyoku.furyborn.client.util;

import XiGyoku.furyborn.Config;
import XiGyoku.furyborn.client.util.TargetMarkRenderUtil;
import XiGyoku.furyborn.entity.RobyteBitLaserEntity;
import XiGyoku.furyborn.item.ItemBusterThrower;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class TargetMarkManager {

    private static class MarkData {
        float alpha;
        boolean activeThisTick;
        float targetSize;
        float currentSize;
        Vec3 position;

        MarkData(float size, Vec3 pos) {
            this.alpha = 0.0F;
            this.activeThisTick = true;
            this.targetSize = size;
            this.currentSize = size;
            this.position = pos;
        }
    }

    private static final Map<UUID, MarkData> ENTITY_MARKS = new HashMap<>();
    private static MarkData blockMark = null;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            ENTITY_MARKS.clear();
            blockMark = null;
            return;
        }

        LocalPlayer player = mc.player;

        for (MarkData data : ENTITY_MARKS.values()) {
            data.activeThisTick = false;
        }
        if (blockMark != null) {
            blockMark.activeThisTick = false;
        }

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof RobyteBitLaserEntity bitLaser) {
                Vec3 start = bitLaser.position().add(0, bitLaser.getBbHeight() / 2.0F, 0);
                Vec3 dir = bitLaser.getViewVector(1.0F);
                Vec3 end = start.add(dir.scale(128.0D));

                LivingEntity target = null;
                double closestDist = Double.MAX_VALUE;
                AABB searchBox = new AABB(start, end).inflate(2.0D);

                for (LivingEntity e : bitLaser.level().getEntitiesOfClass(LivingEntity.class, searchBox, e -> !e.isSpectator())) {
                    AABB targetBox = e.getBoundingBox().inflate(1.0D);
                    if (targetBox.clip(start, end).isPresent()) {
                        double dist = start.distanceToSqr(e.position());
                        if (dist < closestDist) {
                            closestDist = dist;
                            target = e;
                        }
                    }
                }

                if (target != null) {
                    UUID targetUUID = target.getUUID();
                    Vec3 centerPos = target.position().add(0, target.getBbHeight() / 2.0F, 0);
                    Vec3 toPlayer = player.getEyePosition(1.0F).subtract(centerPos);
                    if (toPlayer.lengthSqr() > 0.0001) {
                        toPlayer = toPlayer.normalize();
                    } else {
                        toPlayer = new Vec3(0, 0, 1);
                    }
                    float offsetDist = target.getBbWidth();
                    Vec3 pos = centerPos.add(toPlayer.scale(offsetDist));

                    MarkData data = ENTITY_MARKS.get(targetUUID);
                    if (data == null) {
                        data = new MarkData(1.5F, pos);
                        ENTITY_MARKS.put(targetUUID, data);
                    } else {
                        data.activeThisTick = true;
                        data.targetSize = 1.5F;
                        data.position = pos;
                    }
                }
            }
        }

        if (player.isUsingItem()) {
            ItemStack useItem = player.getUseItem();
            if (useItem.getItem() instanceof ItemBusterThrower) {
                int useTime = player.getUseItemRemainingTicks();
                int totalDuration = useItem.getUseDuration();
                int ticksUsed = totalDuration - useTime;

                int chargeTime = 40;
                int cooldownTime = 20;
                int totalCycle = chargeTime + cooldownTime;
                int cycleTime = ticksUsed % totalCycle;

                if (cycleTime > 0 && cycleTime < chargeTime) {
                    double length = Config.BUSTER_THROWER_LASER_LENGTH.get().floatValue();
                    double radius = Config.BUSTER_THROWER_LASER_SIZE.get().floatValue();
                    Vec3 start = player.getEyePosition(1.0F);
                    Vec3 dir = player.getViewVector(1.0F);
                    Vec3 end = start.add(dir.scale(length));

                    ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
                    BlockHitResult blockHit = mc.level.clip(context);

                    boolean hitEntity = false;

                    if (blockHit.getType() == HitResult.Type.BLOCK) {
                        end = blockHit.getLocation();
                    }

                    AABB searchBox = new AABB(start, end).inflate(radius + 1.0D);
                    for (LivingEntity target : mc.level.getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != player && !e.isSpectator())) {
                        AABB targetBox = target.getBoundingBox().inflate(radius);
                        if (targetBox.clip(start, end).isPresent()) {
                            UUID targetUUID = target.getUUID();
                            Vec3 centerPos = target.position().add(0, target.getBbHeight() / 2.0F, 0);
                            Vec3 toPlayer = player.getEyePosition(1.0F).subtract(centerPos);
                            if (toPlayer.lengthSqr() > 0.0001) {
                                toPlayer = toPlayer.normalize();
                            } else {
                                toPlayer = new Vec3(0, 0, 1);
                            }
                            float offsetDist = target.getBbWidth();
                            Vec3 pos = centerPos.add(toPlayer.scale(offsetDist));

                            MarkData data = ENTITY_MARKS.get(targetUUID);
                            if (data == null) {
                                data = new MarkData(2.0F, pos);
                                ENTITY_MARKS.put(targetUUID, data);
                            } else {
                                data.activeThisTick = true;
                                data.targetSize = 2.0F;
                                data.position = pos;
                            }
                            hitEntity = true;
                        }
                    }

                    if (!hitEntity && blockHit.getType() == HitResult.Type.BLOCK) {
                        Vec3 pos = blockHit.getLocation();
                        if (blockMark == null) {
                            blockMark = new MarkData(5.0F, pos);
                        } else {
                            blockMark.activeThisTick = true;
                            blockMark.targetSize = 5.0F;
                            blockMark.position = pos;
                        }
                    }
                }
            }
        }

        Iterator<Map.Entry<UUID, MarkData>> iterator = ENTITY_MARKS.entrySet().iterator();
        while (iterator.hasNext()) {
            MarkData data = iterator.next().getValue();
            if (data.activeThisTick) {
                data.alpha = Mth.clamp(data.alpha + 0.1F, 0.0F, 1.0F);
                data.currentSize = Mth.lerp(0.2F, data.currentSize, data.targetSize);
            } else {
                data.alpha = Mth.clamp(data.alpha - 0.2F, 0.0F, 1.0F);
                if (data.alpha <= 0.0F) {
                    iterator.remove();
                }
            }
        }

        if (blockMark != null) {
            if (blockMark.activeThisTick) {
                blockMark.alpha = Mth.clamp(blockMark.alpha + 0.1F, 0.0F, 1.0F);
                blockMark.currentSize = Mth.lerp(0.2F, blockMark.currentSize, blockMark.targetSize);
            } else {
                blockMark.alpha = Mth.clamp(blockMark.alpha - 0.2F, 0.0F, 1.0F);
                if (blockMark.alpha <= 0.0F) {
                    blockMark = null;
                }
            }
        }
    }

    public static void render(PoseStack poseStack, float partialTicks) {
        for (MarkData data : ENTITY_MARKS.values()) {
            if (data.alpha > 0.0F) {
                poseStack.pushPose();
                poseStack.translate(data.position.x, data.position.y, data.position.z);
                TargetMarkRenderUtil.renderZMark(poseStack, data.currentSize, data.alpha * 0.7F);
                poseStack.popPose();
            }
        }

        if (blockMark != null && blockMark.alpha > 0.0F) {
            poseStack.pushPose();
            poseStack.translate(blockMark.position.x, blockMark.position.y, blockMark.position.z);
            TargetMarkRenderUtil.renderZMark(poseStack, blockMark.currentSize, blockMark.alpha * 0.7F);
            poseStack.popPose();
        }
    }
}