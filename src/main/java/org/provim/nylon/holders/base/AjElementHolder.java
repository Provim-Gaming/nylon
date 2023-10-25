package org.provim.nylon.holders.base;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjHolderInterface;

import java.util.List;
import java.util.function.Consumer;

/**
 * Base class for all AJ holders that handles Polymer's ElementHolder specific logic.
 * <p>
 * This class mainly exists to split off ElementHolder logic from the element logic.
 */
public abstract class AjElementHolder<T extends Entity & AjEntity> extends ElementHolder implements AjHolderInterface {
    protected final T parent;
    protected final MinecraftServer server;
    protected int tickCount;
    private boolean isLoaded;

    public AjElementHolder(T parent) {
        this.parent = parent;
        this.server = parent.getServer();
        this.tickCount = parent.tickCount - 1;

        if (this.server == null) {
            throw new IllegalStateException("You can only create AjElementHolders for serverside entities!");
        }
    }

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
        int parentTickCount = this.parent.tickCount;
        if (parentTickCount < ++this.tickCount) {
            // If the parent entity is behind, they likely haven't been ticked - in which case we can skip this tick too.
            this.tickCount = parentTickCount;
            return;
        }

        super.tick();
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

    @Override
    public List<VirtualElement> getVirtualElements() {
        return this.getElements();
    }

    public T getParent() {
        return this.parent;
    }

    public MinecraftServer getServer() {
        return this.server;
    }
}
