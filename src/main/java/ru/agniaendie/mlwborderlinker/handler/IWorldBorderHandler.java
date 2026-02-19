package ru.agniaendie.mlwborderlinker.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
public interface IWorldBorderHandler {
    void TransferEntity(TickEvent.LevelTickEvent event);
    void PushChunk(ServerPlayer serverPlayer, int chunkX, int chunkZ);
}
