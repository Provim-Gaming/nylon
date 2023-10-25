package org.provim.nylon.extra;

import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.wrappers.DisplayWrapper;
import org.provim.nylon.holders.wrappers.Locator;
import org.provim.nylon.model.AjPose;

/**
 * Listener for locators, updates a single DisplayElement
 */
public class DisplayElementUpdateListener implements Locator.LocatorListener {
    protected final DisplayWrapper<?> display;

    public DisplayElementUpdateListener(DisplayWrapper<?> display) {
        this.display = display;
    }

    @Override
    public void update(AbstractAjHolder<?> holder, AjPose pose) {
        holder.applyPose(pose, this.display);
    }
}