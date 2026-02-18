package ru.agniaendie.mlwborderlinker.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;

import static com.mojang.text2speech.Narrator.LOGGER;

public class WorldBorderHandler implements IWorldBorderHandler {

    @Override
    public void TransferEntity(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();

        if (entity.level().isClientSide || !entity.isAlive()) return;

        WorldBorder border = entity.level().getWorldBorder();
        double margin = -1.5D;

        if (!border.isWithinBounds(entity.getX(), entity.getZ(), margin)) {

            Vec3 currentMotion = entity.getDeltaMovement();

            double targetX = entity.getX() * -1;
            double targetZ = entity.getZ() * -1;
            double pushInside = 3.0D;

            targetX = targetX > 0 ? targetX - pushInside : targetX + pushInside;
            targetZ = targetZ > 0 ? targetZ - pushInside : targetZ + pushInside;

            float yaw = entity.getYRot();
            float pitch = entity.getXRot();

            if (entity instanceof ServerPlayer player) {
                player.teleportTo((ServerLevel) player.level(), targetX, player.getY(), targetZ, yaw, pitch);
            } else {
                entity.absMoveTo(targetX, entity.getY(), targetZ, yaw, pitch);
                entity.hasImpulse = true;
            }

            entity.setDeltaMovement(currentMotion);

            entity.hasImpulse = true;
            entity.hurtMarked = true;

            LOGGER.info("Сущность {} зеркально перенесена", entity.getName().getString());
        }
    }

}
