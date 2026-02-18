package ru.agniaendie.mlwborderlinker.handler;

import net.minecraftforge.event.TickEvent;
public interface IWorldBorderHandler {
    void TransferEntity(TickEvent.LevelTickEvent event);
}
