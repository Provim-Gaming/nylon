package org.provim.nylon.holders.base;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjHolderInterface;
import org.provim.nylon.util.IChunkMap;
import org.provim.nylon.util.Utils;

import java.util.function.Consumer;

/**
 * Base class for all AJ holders that handles Polymer's ElementHolder specific logic.
 * <p>
 * This class mainly exists to split off ElementHolder logic from the element logic.
 */
public abstract class AjElementHolder<T extends Entity & AjEntity> extends ElementHolder implements AjHolderInterface {
    protected final ServerLevel level;
    protected final T parent;
    protected int tickCount;
    private boolean isLoaded;
    private ServerGamePacketListenerImpl[] watchingPlayers;

    public AjElementHolder(T parent) {
        this.parent = parent;
        this.level = (ServerLevel) parent.level();
        this.tickCount = parent.tickCount - 1;
        this.watchingPlayers = Utils.EMPTY_CONNECTION_ARRAY;
    }

    abstract protected void onAsyncTick();

    abstract protected void onEntityDataLoaded();

    abstract protected void addDirectPassengers(IntList passengers);

    @Override
    public final boolean startWatching(ServerGamePacketListenerImpl player) {
        if (!this.isLoaded) {
            this.isLoaded = true;
            this.onEntityDataLoaded();
        }

        return super.startWatching(player);
    }

    @Override
    public final void tick() {
        if (this.getAttachment() == null) {
            return;
        }

        int parentTickCount = this.parent.tickCount;
        if (parentTickCount < ++this.tickCount) {
            // If the parent entity is behind, they likely haven't been ticked - in which case we can skip this tick too.
            this.tickCount = parentTickCount;
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

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);
        this.sendDirectPassengers(consumer);
    }

    public void sendDirectPassengers(Consumer<Packet<ClientGamePacketListener>> consumer) {
        IntList passengers = new IntArrayList();
        this.addDirectPassengers(passengers);

        if (passengers.size() > 0) {
            consumer.accept(VirtualEntityUtils.createRidePacket(this.parent.getId(), passengers));
        }
    }

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
    }

    public T getParent() {
        return this.parent;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public MinecraftServer getServer() {
        return this.level.getServer();
    }
}
