package org.provim.nylon.entities.holders.elements;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import org.provim.nylon.entities.holders.AbstractAjHolder;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

public class LocatorDisplay extends DisplayWrapper<DisplayElement> {
    private final AbstractAjHolder<?> holder;
    private boolean isActive = true;

    public static LocatorDisplay of(DisplayElement element, AjNode node, AjPose defaultPose, AbstractAjHolder<?> holder) {
        return new LocatorDisplay(element, node, defaultPose, holder);
    }

    protected LocatorDisplay(DisplayElement element, AjNode node, AjPose defaultPose, AbstractAjHolder<?> holder) {
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
