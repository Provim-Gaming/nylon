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

package org.provim.nylon.holders.base;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.util.IChunkMap;
import org.provim.nylon.util.Utils;

/**
 * Base class for all AJ holders that handles Polymer's ElementHolder specific logic.
 * <p>
 * This class mainly exists to split off ElementHolder logic from the element logic.
 */
public abstract class AjElementHolder extends ElementHolder {
    protected final ServerLevel level;
    private ServerGamePacketListenerImpl[] watchingPlayers;
    private boolean elementsInitialized;
    private boolean isDataLoaded;

    protected AjElementHolder(ServerLevel level) {
        this.level = level;
        this.watchingPlayers = Utils.EMPTY_CONNECTION_ARRAY;
    }

    abstract protected void initializeElements();

    abstract protected void onAsyncTick();

    abstract protected void onDataLoaded();

    abstract protected boolean shouldSkipTick();

    @Override
    public void setAttachment(@Nullable HolderAttachment attachment) {
        if (attachment != null && !this.elementsInitialized) {
            this.elementsInitialized = true;
            this.initializeElements();
        }
        super.setAttachment(attachment);
    }

    @Override
    public final boolean startWatching(ServerGamePacketListenerImpl player) {
        if (!this.isDataLoaded) {
            this.isDataLoaded = true;
            this.onDataLoaded();
        }

        return super.startWatching(player);
    }

    @Override
    public final void tick() {
        if (this.getAttachment() == null || this.shouldSkipTick()) {
            return;
        }

        this.onTick();

        this.updatePosition();

        // Schedule an async tick for this holder.
        IChunkMap.scheduleAsyncTick(this);
    }

    public final void asyncTick() {
        this.watchingPlayers = this.getWatchingPlayers().toArray(this.watchingPlayers);

        this.onAsyncTick();

        for (VirtualElement element : this.getElements()) {
            element.tick();
        }
    }

    @Override
    public void sendPacket(Packet<ClientGamePacketListener> packet) {
        if (this.getServer().isSameThread()) {
            super.sendPacket(packet);
        } else {
            for (ServerGamePacketListenerImpl conn : this.watchingPlayers) {
                if (conn != null) {
                    Utils.sendPacketNoFlush(conn, packet);
                }
            }
        }
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public MinecraftServer getServer() {
        return this.level.getServer();
    }
}
