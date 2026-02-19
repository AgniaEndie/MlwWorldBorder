package ru.agniaendie.mlwborderlinker.mixin;

import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mutable;

@Mixin(ClientboundLevelChunkWithLightPacket.class)
public interface ChunkPacketAccessor {
    @Accessor("x") @Mutable void setX(int x);
    @Accessor("z") @Mutable void setZ(int z);
}
