package ru.agniaendie.mlwborderlinker.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.util.List;
public class WorldBorderHandler implements IWorldBorderHandler {

    @Override
    public void TransferEntity(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;

        ServerLevel level = (ServerLevel) event.level;
        WorldBorder border = level.getWorldBorder();

        for (Entity entity : level.getAllEntities()) {
            if (entity == null || !entity.isAlive() || entity.isPassenger()) continue;

            if (entity instanceof ServerPlayer player) {
                if ((getDistanceToBorder(player, border) < (16.0D * 8))){
                    double mirrorX = -player.getX();
                    double mirrorZ = -player.getZ();

                    int chunkX = Mth.floor(mirrorX) >> 4;
                    int chunkZ = Mth.floor(mirrorZ) >> 4;

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            int sourceCX = chunkX + dx;
                            int sourceCZ = chunkZ + dz;
                            this.PushChunk(player, sourceCX, sourceCZ);
                        }
                    }
                }
            }

            if (!border.isWithinBounds(entity.getX(), entity.getZ(), -2.5D)) {
                teleportChain(entity, level, 5.0D);
            }
        }
    }

    private boolean willBeOutside(ServerPlayer player, int cx, int cz, WorldBorder border) {
        int sizeChunks = (int) border.getSize() >> 4;
        int pCX = player.chunkPosition().x;
        int pCZ = player.chunkPosition().z;

        int vX = (Math.abs(cx - pCX) > sizeChunks / 2) ? (cx < pCX ? cx + sizeChunks : cx - sizeChunks) : cx;
        int vZ = (Math.abs(cz - pCZ) > sizeChunks / 2) ? (cz < pCZ ? cz + sizeChunks : cz - sizeChunks) : cz;

        return (vX << 4) < border.getMinX() || (vX << 4) >= border.getMaxX() ||
                (vZ << 4) < border.getMinZ() || (vZ << 4) >= border.getMaxZ();
    }

    @Override
    public void PushChunk(ServerPlayer player, int chunkX, int chunkZ) {
        ServerLevel level = player.serverLevel();
        WorldBorder border = level.getWorldBorder();
        int sizeChunks = (int) border.getSize() >> 4;

        int pCX = player.chunkPosition().x;
        int pCZ = player.chunkPosition().z;

        int vX = (Math.abs(chunkX - pCX) > sizeChunks / 2) ? (chunkX < pCX ? chunkX + sizeChunks : chunkX - sizeChunks) : chunkX;
        int vZ = (Math.abs(chunkZ - pCZ) > sizeChunks / 2) ? (chunkZ < pCZ ? chunkZ + sizeChunks : chunkZ - sizeChunks) : chunkZ;

        double checkX = (vX << 4) + 8.0D;
        double checkZ = (vZ << 4) + 8.0D;

        if (border.isWithinBounds(checkX, checkZ)) {
            return; // Это чанк внутри мира, его трогать нельзя!
        }

        LevelChunk chunk = (LevelChunk) level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        if (chunk != null) {
            ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), null, null);
            ((ru.agniaendie.mlwborderlinker.mixin.ChunkPacketAccessor) packet).setX(vX);
            ((ru.agniaendie.mlwborderlinker.mixin.ChunkPacketAccessor) packet).setZ(vZ);
            player.connection.send(packet);
        }
    }


    private static double getDistanceToBorder(Entity e, WorldBorder border) {
        double x = e.getX();
        double z = e.getZ();
        double toMinX = x - border.getMinX();
        double toMaxX = border.getMaxX() - x;
        double toMinZ = z - border.getMinZ();
        double toMaxZ = border.getMaxZ() - z;
        return Math.min(Math.min(toMinX, toMaxX), Math.min(toMinZ, toMaxZ));
    }

    private static void teleportChain(Entity root, ServerLevel level, double pushInside) {
        double targetX = -root.getX();
        double targetZ = -root.getZ();

        targetX += (targetX >= 0 ? -pushInside : pushInside);
        targetZ += (targetZ >= 0 ? -pushInside : pushInside);

        List<Entity> passengers = root.getPassengers();
        Vec3 motion = root.getDeltaMovement();

        root.ejectPassengers();
        BlockPos pos = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR,
                BlockPos.containing(targetX, root.getY(), targetZ)
        );

        double targetY = (root.getY() >= pos.getY() ? root.getY() : pos.getY());

        performSingleTeleport(root, level, pos.getX(), targetY, pos.getZ());

        for (Entity passenger : passengers) {
            performSingleTeleport(passenger, level, pos.getX(), pos.getY(), pos.getZ());
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
            e.hurtMarked = true;
        }
    }
}
