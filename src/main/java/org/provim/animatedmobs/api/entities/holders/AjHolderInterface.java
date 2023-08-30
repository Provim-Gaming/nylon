package org.provim.animatedmobs.api.entities.holders;

import org.provim.animatedmobs.api.entities.holders.wrappers.Locator;

public interface AjHolderInterface {
    Locator getLocator(String name);

    int[] getDisplayIds();

    int getVehicleId();

    void setDefaultVariant();

    void setCurrentVariant(String currentVariant);

    void setCurrentAnimation(String currentAnimation);

    void startExtraAnimation(String animationName);

    boolean extraAnimationRunning();
}
