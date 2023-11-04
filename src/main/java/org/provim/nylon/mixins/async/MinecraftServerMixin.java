package org.provim.nylon.mixins.async;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.provim.nylon.util.IChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    public abstract Iterable<ServerLevel> getAllLevels();

    @Inject(
            method = "tickChildren",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerConnectionListener;tick()V",
                    ordinal = 0
            )
    )
    private void nylon$ensureAsyncTickFinished(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        for (ServerLevel level : this.getAllLevels()) {
            IChunkMap.blockUntilAsyncTickFinished(level.getChunkSource().chunkMap);
        }
    }
}
