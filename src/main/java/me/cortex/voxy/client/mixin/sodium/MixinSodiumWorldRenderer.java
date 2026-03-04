package me.cortex.voxy.client.mixin.sodium;

import me.cortex.voxy.client.core.IGetVoxyRenderSystem;
import me.cortex.voxy.client.core.rendering.Viewport;
import me.cortex.voxy.client.core.util.IrisUtil;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class MixinSodiumWorldRenderer {

    @Unique
    private ChunkRenderMatrices voxy$capturedMatrices;

    @Inject(
            method = "drawChunkLayer(Lnet/minecraft/class_1921;Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderMatrices;DDD)V",
            at = @At("HEAD"),
            remap = false
    )
    private void voxy$captureMatrices(
            RenderType renderLayer,
            ChunkRenderMatrices matrices,
            double x,
            double y,
            double z,
            CallbackInfo ci
    ) {
        this.voxy$capturedMatrices = matrices;
    }

    @Inject(
            method = "drawChunkLayer(Lnet/minecraft/class_1921;Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderMatrices;DDD)V",
            at = @At("TAIL"),
            remap = false
    )
    private void voxy$injectRender(
            RenderType renderLayer,
            ChunkRenderMatrices matrices,
            double x,
            double y,
            double z,
            CallbackInfo ci
    ) {
        this.doRender(this.voxy$capturedMatrices, renderLayer, x, y, z);
    }

    @Unique
    private void doRender(ChunkRenderMatrices matrices, RenderType renderLayer, double x, double y, double z) {
        if (renderLayer != RenderType.solid()) {
            return;
        }

        var renderer = ((IGetVoxyRenderSystem) Minecraft.getInstance().levelRenderer).getVoxyRenderSystem();
        if (renderer != null) {
            Viewport<?> viewport = IrisUtil.irisShaderPackEnabled()
                    ? renderer.getViewport()
                    : renderer.setupViewport(matrices, x, y, z);

            renderer.renderOpaque(viewport);
        }
    }
}
