package org.provim.nylon.util;

import net.minecraft.server.level.ChunkMap;
import org.provim.nylon.holders.base.AjElementHolder;

public interface IChunkMap {
    void nylon$scheduleAsyncTick(AjElementHolder holder);

    void nylon$blockUntilAsyncTickFinished();

    static void scheduleAsyncTick(AjElementHolder holder) {
        IChunkMap chunkMap = (IChunkMap) holder.getLevel().getChunkSource().chunkMap;
        chunkMap.nylon$scheduleAsyncTick(holder);
    }

    static void blockUntilAsyncTickFinished(ChunkMap map) {
        IChunkMap chunkMap = (IChunkMap) map;
        chunkMap.nylon$blockUntilAsyncTickFinished();
    }
}
