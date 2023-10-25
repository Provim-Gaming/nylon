package org.provim.nylon.extra;

import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import net.minecraft.world.phys.Vec3;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.wrappers.Locator;
import org.provim.nylon.model.AjPose;

/**
 * Listener for locators, updates a single GenericEntityElement
 */
public class ElementUpdateListener implements Locator.LocatorListener {
    protected final GenericEntityElement element;

    public ElementUpdateListener(GenericEntityElement element) {
        this.element = element;
    }

    @Override
    public void update(AbstractAjHolder<?> holder, AjPose pose) {
        this.element.setOffset(new Vec3(pose.translation()));
    }
}