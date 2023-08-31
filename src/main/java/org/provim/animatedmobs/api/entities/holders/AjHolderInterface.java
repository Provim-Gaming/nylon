package org.provim.animatedmobs.api.entities.holders;

import org.provim.animatedmobs.api.entities.holders.elements.LocatorDisplay;

public interface AjHolderInterface {
    LocatorDisplay getLocator(String name);

    int[] getDisplayIds();

    int getDisplayVehicleId();

    int getVehicleId();

    void setDefaultVariant();

    void setCurrentVariant(String currentVariant);

    void setCurrentAnimation(String currentAnimation);

    void startExtraAnimation(String animationName);

    boolean extraAnimationRunning();
}
