package org.provim.nylon.util;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.provim.nylon.holders.wrapper.Locator;

/**
 * Listener for locators, updates a single DisplayElement
 */
public class ElementUpdateListener implements Locator.LocatorListener {
    protected final DisplayElement element;

    public static ElementUpdateListener of(DisplayElement element) {
        return new ElementUpdateListener(element);
    }

    public ElementUpdateListener(DisplayElement element) {
        this.element = element;
    }

    @Override
    public void update(Vector3f position, Quaternionf rotation) {
        this.element.setTranslation(position);
        this.element.setRightRotation(rotation);
        this.element.startInterpolation();
    }
}