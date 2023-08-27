package org.provim.animatedmobs.api.entities.holders;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;

import java.util.UUID;

public interface AjHolderInterface {

    void updateElements();

    ItemDisplayElement getItemDisplayElement(UUID elementUUID);

    public DisplayElement getAdditionalDisplayNamed(String name);

    int[] getDisplayIds();

    int getVehicleId();

    void setDefaultVariant();

    void setCurrentVariant(String currentVariant);

    void setCurrentAnimation(String currentAnimation);

    void startExtraAnimation(String animationName);

    boolean extraAnimationRunning();
}
