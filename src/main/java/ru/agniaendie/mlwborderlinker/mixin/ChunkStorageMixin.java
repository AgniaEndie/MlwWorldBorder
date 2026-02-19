package ru.agniaendie.mlwborderlinker.mixin;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public abstract class ChunkStorageMixin {

    @Shadow @Final private AtomicReferenceArray<net.minecraft.world.level.chunk.LevelChunk> chunks;

    @Inject(method = "inRange", at = @At("HEAD"), cancellable = true)
    private void mlw$alwaysInRange(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "getIndex", at = @At("HEAD"), cancellable = true)
    private void mlw$dynamicIndex(int x, int z, CallbackInfoReturnable<Integer> cir) {
        int arrayLength = this.chunks.length();
        int side = (int) Math.sqrt(arrayLength);

        int i = Math.floorMod(x, side);
        int j = Math.floorMod(z, side);

        int index = j * side + i;

        if (index >= 0 && index < arrayLength) {
            cir.setReturnValue(index);
        }
    }
}
