package org.provim.animatedmobs.api.entities.holders.wrappers;

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import org.provim.animatedmobs.api.model.AjNode;
import org.provim.animatedmobs.api.model.AjPose;

public class Bone extends WrappedDisplay<ItemDisplayElement> {
    private final boolean isHead;

    public static Bone of(ItemDisplayElement element, AjNode node, AjPose defaultPose, boolean isHead) {
        return new Bone(element, node, defaultPose, isHead);
    }

    public static Bone of(ItemDisplayElement element, AjNode node, AjPose defaultPose) {
        return new Bone(element, node, defaultPose, node.name().startsWith("head"));
    }

    protected Bone(ItemDisplayElement element, AjNode node, AjPose defaultPose, boolean isHead) {
        super(element, node, defaultPose);
        this.isHead = isHead;
    }

    @Override
    public boolean isHead() {
        return this.isHead;
    }
}
