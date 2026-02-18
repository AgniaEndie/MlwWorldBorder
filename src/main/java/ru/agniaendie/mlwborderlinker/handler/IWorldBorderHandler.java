package ru.agniaendie.mlwborderlinker.handler;

import net.minecraftforge.event.entity.living.LivingEvent;

public interface IWorldBorderHandler {
    void TransferEntity(LivingEvent.LivingTickEvent event);
}
