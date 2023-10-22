package org.provim.nylon.holders.wrapper;

import org.provim.nylon.model.AjAnimation;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

public abstract class AbstractWrapper {
    private final AjNode node;

    private final AjPose defaultPose;

    private AjAnimation lastAnimation;
    private AjPose lastPose;

    public AbstractWrapper(AjNode node, AjPose defaultPose) {
        this.node = node;
        this.defaultPose = defaultPose;
    }

    public AjNode node() {
        return this.node;
    }

    public String name() { return this.node.name(); }

    public AjPose getDefaultPose() {
        return this.defaultPose;
    }

    public AjPose getLastPose(AjAnimation animation) {
        return animation == this.lastAnimation ? this.lastPose : null;
    }

    public void setLastPose(AjPose lastPose, AjAnimation animation) {
        this.lastAnimation = animation;
        this.lastPose = lastPose;
    }

    abstract public boolean requiresUpdateEveryTick(); // awkward naming..?
}
