package ru.agniaendie.mlwborderlinker.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ru.agniaendie.mlwborderlinker.Config.xCoord;
import static ru.agniaendie.mlwborderlinker.Config.zCoord;

@Mixin(ClientPacketListener.class)
public class ChunkPacketTranslatorMixin {
    @Inject(method = "handleLevelChunkWithLight", at = @At("HEAD"))
    private void mlw$translateRemoteChunk(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        double size = xCoord * zCoord;

        int sizeChunks = (int) size >> 4;

        int pX = packet.getX();
        int pZ = packet.getZ();
        int playerX = mc.player.chunkPosition().x;
        int playerZ = mc.player.chunkPosition().z;

        int diffX = pX - playerX;
        int diffZ = pZ - playerZ;

        boolean isToroidal = Math.abs(diffX) > (sizeChunks / 2) || Math.abs(diffZ) > (sizeChunks / 2);

        if (isToroidal) {
            ChunkPacketAccessor accessor = (ChunkPacketAccessor) packet;
            int newX = (Math.abs(diffX) > sizeChunks / 2) ? (pX < playerX ? pX + sizeChunks : pX - sizeChunks) : pX;
            int newZ = (Math.abs(diffZ) > sizeChunks / 2) ? (pZ < playerZ ? pZ + sizeChunks : pZ - sizeChunks) : pZ;

            accessor.setX(newX);
            accessor.setZ(newZ);
            mc.level.getChunkSource().drop(newX, newZ);
        }
    }


}
