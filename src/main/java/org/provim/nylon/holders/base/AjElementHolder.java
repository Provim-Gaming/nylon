package org.provim.nylon.holders.base;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.Util;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.provim.nylon.api.AjHolderInterface;
import org.provim.nylon.util.Utils;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Base class for all AJ holders that handles Polymer's ElementHolder specific logic.
 */
public abstract class AjElementHolder<T extends Entity> extends ElementHolder implements AjHolderInterface {
    private static final Executor EXECUTOR = Util.backgroundExecutor();
    protected final T parent;
    private final boolean updateElementsAsync;
    private boolean isLoaded;
    private int tickCount;

    public AjElementHolder(T parent, boolean updateElementsAsync) {
        this.parent = parent;
        this.tickCount = parent.tickCount - 1;
        this.updateElementsAsync = updateElementsAsync;
    }

    abstract protected void onEntityDataLoaded();

    abstract protected void updateElements();

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
            EXECUTOR.execute(this::updateElementsInternal);
        } else {
            this.updateElementsInternal();
        }
    }

    private void updateElementsInternal() {
        this.updateElements();

        for (VirtualElement element : Utils.getElementsUnsafe(this)) {
            element.tick();
        }
    }

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
    }

    @Override
    public List<VirtualElement> getVirtualElements() {
        return this.getElements();
    }
}
