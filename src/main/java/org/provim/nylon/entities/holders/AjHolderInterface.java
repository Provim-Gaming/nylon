package org.provim.nylon.entities.holders;

import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.entities.holders.elements.LocatorDisplay;

import java.util.List;

@SuppressWarnings("unused")
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
     */
    void playAnimation(String name);

    /**
     * See above, allows to specify the animation speed
     */
    void playAnimation(String name, int speed);

    /**
     * Schedules an animation for the main animation layer.
     * <p>
     * onFinished will be called on the last frame update for that animation
     */
    void playAnimation(String name, Runnable onFinished);

    /**
     * See above, allows to specify the animation speed
     */
    void playAnimation(String name, int speed, Runnable onFinished);

    /**
     * Pauses the current animation named `name`. The animation can be continued using `runAnimation`.
     */
    void pauseAnimation(String name);

    /**
     * Stops the current animation named `name`.
     */
    void stopAnimation(String name);

    /**
     * Returns a list of all polymer VirtualElements used for this holder
     */
    List<VirtualElement> getVirtualElements();

    /**
     * Returns the parent entity
     */
    Entity getParent();
}
