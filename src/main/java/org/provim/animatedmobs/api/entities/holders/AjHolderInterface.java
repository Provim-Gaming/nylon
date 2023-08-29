package org.provim.animatedmobs.api.entities.holders;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;

public interface AjHolderInterface {
    DisplayElement getLocator(String name);

    int[] getDisplayIds();

    int getVehicleId();

    void setDefaultVariant();

    void setCurrentVariant(String currentVariant);

    void setCurrentAnimation(String currentAnimation);

    void startExtraAnimation(String animationName);

    boolean extraAnimationRunning();
}
