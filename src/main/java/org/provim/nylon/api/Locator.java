package org.provim.nylon.api;

import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import org.joml.Matrix4f;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface Locator {

    /**
     * Activates this locator.
     */
    default void activate() {
        this.updateActivity(true, false);
    }

    /**
     * Deactivates this locator.
     */
    default void deactivate() {
        this.updateActivity(false, false);
    }

    /**
     * Activates this locator on the server.
     * This locator will not exist on the client.
     */
    default void activateServerOnly() {
        this.updateActivity(true, true);
    }

    /**
     * Updates the activity status of this locator.
     *
     * @param isActive:     Whether the locator should be activated or deactivated.
     * @param isServerOnly: Decides if the locator should exist on the client.
     */
    void updateActivity(boolean isActive, boolean isServerOnly);

    /**
     * Sets the consumer that will be called when the transformation of this locator changes.
     */
    void setTransformationUpdateConsumer(Consumer<Matrix4f> consumer);

    /**
     * Whether the locator is currently active.
     */
    boolean isActive();

    /**
     * The element that is used for displaying the locator on the client.
     */
    VirtualElement element();
}
