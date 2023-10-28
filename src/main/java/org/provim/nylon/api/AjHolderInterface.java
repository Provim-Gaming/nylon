package org.provim.nylon.api;

import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import org.provim.nylon.holders.wrappers.Locator;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public interface AjHolderInterface {

    /**
     * Notifies the holder that the synchronized data has been updated.
     * <p>
     * Holder implementations can use this to update their element data.
     */
    void onSyncedDataUpdated(EntityDataAccessor<?> key, Object value);

    /**
     * Notifies the holder that the dimensions of the parent entity have been updated.
     * <p>
     * Holder implementations can use this to update their element data.
     */
    void onDimensionsUpdated(EntityDimensions dimensions);

    /**
     * Returns the locator with the given name.
     */
    Locator getLocator(String name);

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
     * Returns the entity id that should be used for handling entity events clientside.
     */
    int getEntityEventId();

    /**
     * Resets the model variant back to default.
     */
    void setDefaultVariant();

    /**
     * Applies the given variant to the model of the entity.
     */
    void setCurrentVariant(String variant);

    /**
     * Applies the given variant to the model of the entity.
     */
    void setCurrentVariant(UUID variant);

    /**
     * Returns the animator for this model.
     */
    Animator getAnimator();

    /**
     * Returns a list of all Polymer {@link VirtualElement} used in this holder.
     */
    List<VirtualElement> getVirtualElements();
}
