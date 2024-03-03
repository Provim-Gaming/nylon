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
        // Block here at the end of the gametick just before packet flushing is resumed until all the async element updates have completed.
        // This way we can take advantage of suspending packet flushing, which gives a significant improvement in network performance and ping.
        // This will never realistically block the main thread, as these async model ticks are done at the same time as entity ticks on the main thread,
        // which in any normal world will take far longer than the model updates. This is just a safety measure.
        for (ServerLevel level : this.getAllLevels()) {
            IChunkMap.blockUntilAsyncTickFinished(level.getChunkSource().chunkMap);
        }
    }
}
