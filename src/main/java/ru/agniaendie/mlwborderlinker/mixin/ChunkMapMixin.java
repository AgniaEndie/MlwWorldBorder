package ru.agniaendie.mlwborderlinker.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

import static ru.agniaendie.mlwborderlinker.Config.xCoord;
import static ru.agniaendie.mlwborderlinker.Config.zCoord;

@Mixin(net.minecraft.server.level.ChunkMap.class)
public class ChunkMapMixin {
    @Inject(method = "scheduleChunkGeneration", at = @At("HEAD"), cancellable = true)
    private void mlw$cancelOutsideGen(ChunkHolder holder, ChunkStatus status, CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> cir) {
        ChunkPos pos = holder.getPos();
        if (Math.abs(pos.x) > xCoord || Math.abs(pos.z) > zCoord) {
            cir.setReturnValue(CompletableFuture.completedFuture(Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED)));
        }
    }
}
