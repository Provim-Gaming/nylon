package org.provim.nylon.holders.wrappers;

import org.provim.nylon.model.AjAnimation;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

public abstract class AbstractWrapper {
    private final AjNode node;
    private final AjPose defaultPose;
    protected AjAnimation lastAnimation;
    protected AjPose lastPose;

    public AbstractWrapper(AjNode node, AjPose defaultPose) {
        this.node = node;
        this.defaultPose = defaultPose;
        this.lastPose = defaultPose;
    }

    public AjNode node() {
        return this.node;
    }

    public String name() {
        return this.node.name();
    }

    public AjPose getDefaultPose() {
        return this.defaultPose;
    }

    public AjPose getLastPose() {
        return this.lastPose;
    }

    public AjPose getLastPoseFor(AjAnimation animation) {
        return animation == this.lastAnimation ? this.lastPose : null;
    }

    public void setLastPose(AjPose pose, AjAnimation animation) {
        this.lastAnimation = animation;
        this.lastPose = pose;
    }
}
