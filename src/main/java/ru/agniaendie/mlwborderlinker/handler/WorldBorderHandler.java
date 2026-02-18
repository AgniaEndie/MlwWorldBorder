package ru.agniaendie.mlwborderlinker.handler;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import java.util.List;

public class WorldBorderHandler implements IWorldBorderHandler{

    public void TransferEntity(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;

        ServerLevel level = (ServerLevel) event.level;
        WorldBorder border = level.getWorldBorder();
        double margin = -2.5D; // Зазор детекции
        double pushInside = 5.0D; // Глубина заброса

        for (Entity entity : level.getAllEntities()) {
            if (entity == null || !entity.isAlive() || entity.isPassenger()) continue;

            if (!border.isWithinBounds(entity.getX(), entity.getZ(), margin)) {
                teleportChain(entity, level, pushInside);
            }
        }
    }

    private static void teleportChain(Entity root, ServerLevel level, double pushInside) {
        double targetX = -root.getX();
        double targetZ = -root.getZ();

        targetX = targetX > 0 ? targetX - pushInside : targetX + pushInside;
        targetZ = targetZ > 0 ? targetZ - pushInside : targetZ + pushInside;

        List<Entity> passengers = root.getPassengers();
        Vec3 motion = root.getDeltaMovement();

        root.ejectPassengers();

        performSingleTeleport(root, level, targetX, root.getY(), targetZ);
        for (Entity passenger : passengers) {
            performSingleTeleport(passenger, level, targetX, root.getY(), targetZ);
            passenger.startRiding(root, true);
        }

        root.setDeltaMovement(motion);
        level.getChunkSource().broadcastAndSend(root, new ClientboundSetEntityMotionPacket(root));
    }

    private static void performSingleTeleport(Entity e, ServerLevel level, double x, double y, double z) {
        if (e instanceof ServerPlayer player) {
            player.teleportTo(level, x, y, z, e.getYRot(), e.getXRot());
        } else {
            e.absMoveTo(x, y, z, e.getYRot(), e.getXRot());
            e.setPos(x, y, z);
            e.hurtMarked = true;
        }
    }
}
