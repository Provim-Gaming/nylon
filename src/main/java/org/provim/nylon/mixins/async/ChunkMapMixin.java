package org.provim.nylon.mixins.async;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ChunkMap;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.holders.base.AjElementHolder;
import org.provim.nylon.util.IChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(value = ChunkMap.class, priority = 900)
public class ChunkMapMixin implements IChunkMap {
    @Unique
    private final ObjectArrayList<AjElementHolder<?>> nylon$scheduledAsyncTicks = new ObjectArrayList<>();
    @Unique
    @Nullable
    private CompletableFuture<Void> nylon$asyncTickFuture;

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void nylon$afterTickEntityTrackers(CallbackInfo ci) {
        ObjectArrayList<AjElementHolder<?>> holders = this.nylon$scheduledAsyncTicks;
        if (holders.isEmpty()) {
            this.nylon$asyncTickFuture = null;
            return;
        }

        this.nylon$asyncTickFuture = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < holders.size(); i++) {
                var holder = holders.get(i);
                holder.asyncTick();
            }
            holders.clear();
        });
    }

    @Override
    public void nylon$scheduleAsyncTick(AjElementHolder<?> holder) {
        this.nylon$scheduledAsyncTicks.add(holder);
    }

    @Override
    public void nylon$blockUntilAsyncTickFinished() {
        if (this.nylon$asyncTickFuture != null && !this.nylon$asyncTickFuture.isDone()) {
            // Makes sure that all the async ticks have finished.
            this.nylon$asyncTickFuture.join();
        }
    }
}
