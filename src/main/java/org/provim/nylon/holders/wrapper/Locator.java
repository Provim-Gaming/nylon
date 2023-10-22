package org.provim.nylon.holders.wrapper;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

public class Locator extends AbstractWrapper {
    private final ObjectSet<LocatorListener> locatorListeners;
    private final boolean alwaysUpdate;

    public static Locator of(AjNode node, AjPose defaultPose, boolean alwaysUpdate) {
        return new Locator(node, defaultPose, alwaysUpdate);
    }

    public Locator(AjNode node, AjPose defaultPose, boolean alwaysUpdate) {
        super(node, defaultPose);
        this.alwaysUpdate = alwaysUpdate;
        this.locatorListeners = new ObjectArraySet<>();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return this.alwaysUpdate;
    }

    public void updateListeners(AjPose pose) {
        this.locatorListeners.forEach(listener -> listener.update(pose.translation(), pose.rotation()));
    }

    public void addListener(LocatorListener newListener) {
        this.locatorListeners.add(newListener);
    }

    public void removeListener(LocatorListener oldListener) {
        this.locatorListeners.remove(oldListener);
    }

    public void removeAllListeners() {
        this.locatorListeners.clear();
    }

    public interface LocatorListener {
        void update(Vector3f position, Quaternionf rotation);
    }
}
