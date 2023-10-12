package org.provim.nylon.entities.holders.base;

import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.provim.nylon.entities.holders.elements.LocatorDisplay;

import java.util.List;

@SuppressWarnings("unused")
public interface AjHolderInterface {

    /**
     * Notifies the holder that the synchronized data has been updated.
     * <p>
     * Holder implementations can use this to update their element data.
     */
    void onSyncedDataUpdated(EntityDataAccessor<?> key, Object value);

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
     * Returns the entity id of the vehicle of the display entities.
     */
    int getDisplayVehicleId();

    /**
     * Returns the entity id of the vehicle used for mounting mobs and players.
     */
    int getVehicleId();

    /**
     * Returns the entity id that should be used for leashing mobs.
     * <p>
     * If the entity id is not from a {@link Mob} (clientside), the leash will not be rendered.
     */
    int getLeashedId();

    /**
     * Resets the model variant back to default.
     */
    void setDefaultVariant();

    /**
     * Applies the given variant to the model of the entity.
     */
    void setCurrentVariant(String currentVariant);

    /**
     * Starts playing an animation on the model.
     */
    void playAnimation(String name);

    /**
     * Starts playing an animation on the model.
     *
     * @param speed: The animation speed.
     */
    void playAnimation(String name, int speed);

    /**
     * Starts playing an animation on the model.
     *
     * @param onFinished: Callback to be executed on the last frame of the animation.
     */
    void playAnimation(String name, Runnable onFinished);

    /**
     * Starts playing an animation on the model.
     *
     * @param speed:      The animation speed.
     * @param onFinished: Callback to be executed on the last frame of the animation.
     */
    void playAnimation(String name, int speed, Runnable onFinished);

    /**
     * Pauses the current animation with the given name.
     * The animation can be continued using `runAnimation`.
     */
    void pauseAnimation(String name);

    /**
     * Stops the current animation with the given name.
     */
    void stopAnimation(String name);

    /**
     * Returns a list of all Polymer {@link VirtualElement} used in this holder.
     */
    List<VirtualElement> getVirtualElements();

    /**
     * Returns the parent entity.
     */
    Entity getParent();
}
