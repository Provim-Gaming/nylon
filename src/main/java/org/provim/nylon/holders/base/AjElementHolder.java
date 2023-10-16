package org.provim.nylon.holders.base;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.Util;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjHolderInterface;
import org.provim.nylon.util.Utils;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Base class for all AJ holders that handles Polymer's ElementHolder specific logic.
 */
public abstract class AjElementHolder<T extends Entity & AjEntity> extends ElementHolder implements AjHolderInterface {
    private static final Executor EXECUTOR = Util.backgroundExecutor();
    protected final T parent;
    protected final MinecraftServer server;
    private final boolean updateElementsAsync;
    private boolean isLoaded;
    private int tickCount;

    public AjElementHolder(T parent, boolean updateElementsAsync) {
        this.parent = parent;
        this.server = parent.getServer();
        this.tickCount = parent.tickCount - 1;
        this.updateElementsAsync = updateElementsAsync;

        if (this.server == null) {
            throw new IllegalStateException("You can only create AjElementHolders for serverside entities!");
        }
    }

    abstract protected void onEntityDataLoaded();

    abstract protected void updateElements();

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
            // If the parent entity is behind, they likely haven't been ticked - in which case we don't need to update our elements.
            this.tickCount = parentTickCount;
            return;
        }

        this.onTick();

        this.updatePosition();

        if (this.updateElementsAsync) {
            EXECUTOR.execute(() -> {
                this.updateElements();
                this.server.tell(new TickTask(this.server.getTickCount(), this::tickElements));
            });
        } else {
            this.updateElements();
            this.tickElements();
        }
    }

    private void tickElements() {
        for (VirtualElement element : Utils.getElementsUnchecked(this)) {
            element.tick();
        }
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

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

    public MinecraftServer getServer() {
        return this.server;
    }
}
