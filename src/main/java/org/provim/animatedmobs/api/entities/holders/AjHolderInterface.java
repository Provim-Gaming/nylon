package org.provim.animatedmobs.api.entities.holders;

import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import org.provim.animatedmobs.api.entities.holders.elements.LocatorDisplay;

import java.util.List;

public interface AjHolderInterface {

    /**
     * Returns the locator with the given name.
     * <p>
     * This also includes inactive locators that are currently not being displayed.
     */
    LocatorDisplay getLocator(String name);

    /**
     * Returns an array of entity ids used for displaying the model.
     */
    int[] getDisplayIds();

    /**
     * Returns the entity id used for displaying the model (display entities).
     */
    int getDisplayVehicleId();

    /**
     * Returns the entity id used for mounting mobs and players.
     */
    int getVehicleId();

    /**
     * Resets the model variant back to default.
     */
    void setDefaultVariant();

    /**
     * Applies the given variant to the model of the entity.
     */
    void setCurrentVariant(String currentVariant);

    /**
     * Schedules an animation for the main animation layer.
     * <p>
     * The animation will start when the current animation is finished.
     */
    void scheduleAnimation(String name);

    /**
     * Sets the current main animation immediately. This will abruptly exit running animations.
     */
    void setCurrentAnimation(String name);

    /**
     * Schedules an animation for the extra animation layer.
     * <p>
     * The animation will start when the current animation is finished.
     */
    void scheduleExtraAnimation(String name);

    /**
     * Sets the current extra animation immediately. This will abruptly exit running animations.
     */
    void setExtraAnimation(String name);

    List<VirtualElement> getVirtualElements();
}
