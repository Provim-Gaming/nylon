package org.provim.nylon.holders.wrappers;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

public class Locator extends AbstractWrapper {
    private final ObjectSet<LocatorListener> listeners;

    public static Locator of(AjNode node, AjPose defaultPose) {
        return new Locator(node, defaultPose);
    }

    public Locator(AjNode node, AjPose defaultPose) {
        super(node, defaultPose);
        this.listeners = ObjectSets.synchronize(new ObjectArraySet<>());
    }

    public boolean requiresUpdate() {
        return this.listeners.size() > 0;
    }

    public void updateListeners(AbstractAjHolder holder, AjPose pose) {
        this.listeners.forEach(listener -> listener.update(holder, pose));
    }

    public void addListener(LocatorListener newListener) {
        this.listeners.add(newListener);
    }

    public void removeListener(LocatorListener oldListener) {
        this.listeners.remove(oldListener);
    }

    public void removeAllListeners() {
        this.listeners.clear();
    }

    public interface LocatorListener {
        /**
         * Called whenever a locator is updated.
         * This method can be called asynchronously.
         */
        void update(AbstractAjHolder holder, AjPose pose);
    }
}
