package org.provim.nylon.entities.holders.base;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.Util;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.provim.nylon.api.AjHolderInterface;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Base class for all AJ holders that handles Polymer's ElementHolder specific logic.
 */
public abstract class AjElementHolder extends ElementHolder implements AjHolderInterface {
    private static final Executor EXECUTOR = Util.backgroundExecutor();
    private final boolean updateElementsAsync;
    private boolean isLoaded;
    private int tickCount;

    public AjElementHolder(boolean updateElementsAsync) {
        this.updateElementsAsync = updateElementsAsync;
        this.tickCount = -1;
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
        if (this.tickCount++ % 2 != 0) {
            return;
        }

        int parentTickCount = this.getParent().tickCount;
        if (parentTickCount < this.tickCount) {
            // If the parent entity is behind, they likely haven't been ticked - in which case we don't need to update our elements.
            this.tickCount = parentTickCount;
            return;
        }

        super.tick();
    }

    @Override
    protected final void onTick() {
        if (this.updateElementsAsync) {
            EXECUTOR.execute(this::updateElements);
        } else {
            this.updateElements();
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
