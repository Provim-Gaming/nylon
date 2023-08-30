package org.provim.animatedmobs.api.entities.holders.wrappers;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import org.provim.animatedmobs.api.entities.holders.AbstractAjHolder;
import org.provim.animatedmobs.api.model.AjNode;
import org.provim.animatedmobs.api.model.AjPose;

public class Locator extends WrappedDisplay<DisplayElement> {
    private final AbstractAjHolder<?> holder;
    private boolean isActive = true;

    public static Locator of(DisplayElement element, AjNode node, AjPose defaultPose, AbstractAjHolder<?> holder) {
        return new Locator(element, node, defaultPose, holder);
    }

    protected Locator(DisplayElement element, AjNode node, AjPose defaultPose, AbstractAjHolder<?> holder) {
        super(element, node, defaultPose);
        this.holder = holder;
    }

    @Override
    public boolean isHead() {
        return false;
    }

    public void updateActivity(boolean isActive, boolean update) {
        if (this.isActive == isActive) {
            return;
        }

        this.isActive = isActive;
        if (isActive) {
            this.holder.activateLocator(this, update);
        } else {
            this.holder.deactivateLocator(this, update);
        }
    }
}
