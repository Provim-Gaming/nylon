package org.provim.nylon.api;

import org.provim.nylon.holders.wrappers.Locator;

@SuppressWarnings("unused")
public interface AjHolder {
    /**
     * Returns the locator with the given name.
     */
    Locator getLocator(String name);

    /**
     * Returns the variant controller for this holder.
     */
    VariantController getVariantController();

    /**
     * Returns the animator for this model.
     */
    Animator getAnimator();
}
