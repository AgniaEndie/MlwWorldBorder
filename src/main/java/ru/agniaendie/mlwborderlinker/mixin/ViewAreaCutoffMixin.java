package ru.agniaendie.mlwborderlinker.mixin;

import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ViewArea.class)
public class ViewAreaCutoffMixin {
    @Shadow
    @Final
    protected Level level;

    @Inject(method = "getRenderChunkAt", at = @At("HEAD"), cancellable = true)
    private void mlw$cutoffOutside(BlockPos pos, CallbackInfoReturnable<ChunkRenderDispatcher.RenderChunk> cir) {
        if (this.level == null) return;

        WorldBorder border = this.level.getWorldBorder();

        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;

        int minCX = (int) border.getMinX() >> 4;
        int maxCX = (int) border.getMaxX() >> 4;
        int minCZ = (int) border.getMinZ() >> 4;
        int maxCZ = (int) border.getMaxZ() >> 4;

        boolean xPlayable = cx >= minCX && cx < maxCX;
        boolean xMirror = cx == minCX - 1 || cx == maxCX;
        boolean xValid = xPlayable || xMirror;

        boolean zPlayable = cz >= minCZ && cz < maxCZ;
        boolean zMirror = cz == minCZ - 1 || cz == maxCZ;
        boolean zValid = zPlayable || zMirror;

        if (!xValid || !zValid) {
            cir.setReturnValue(null);
        }
    }

}

