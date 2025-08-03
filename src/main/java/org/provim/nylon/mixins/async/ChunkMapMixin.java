/*
 * Nylon
 * Copyright (C) 2023, 2024 Provim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
    private ObjectArrayList<AjElementHolder> nylon$scheduledAsyncTicks = new ObjectArrayList<>();
    @Unique
    @Nullable
    private CompletableFuture<Void> nylon$asyncTickFuture;

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void nylon$afterTickEntityTrackers(CallbackInfo ci) {
        ObjectArrayList<AjElementHolder> holders = this.nylon$scheduledAsyncTicks;
        if (holders.isEmpty()) {
            this.nylon$asyncTickFuture = null;
            return;
        }

        this.nylon$scheduledAsyncTicks = new ObjectArrayList<>(holders.size());
        this.nylon$asyncTickFuture = CompletableFuture.runAsync(() -> {
            for (AjElementHolder holder : holders) {
                holder.asyncTick();
            }
        });
    }

    @Override
    public void nylon$scheduleAsyncTick(AjElementHolder holder) {
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
