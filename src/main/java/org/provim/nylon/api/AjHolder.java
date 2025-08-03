/*
 * Nylon
 * Copyright (C) 2023, 2024 Provim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.provim.nylon.api;

import org.provim.nylon.data.model.nylon.NylonModel;
import org.provim.nylon.holders.wrappers.Locator;

@SuppressWarnings("unused")
public interface AjHolder {
    /**
     * Returns the model of this holder.
     */
    NylonModel getModel();

    /**
     * Returns the locator with the given name.
     */
    Locator getLocator(String name);

    /**
     * Returns the variant controller for this holder.
     */
    VariantController getVariantController();

    /**
     * Returns the animator for this holder.
     */
    Animator getAnimator();

    /**
     * Returns the scale of this holder.
     */
    float getScale();

    /**
     * Sets the scale of this holder.
     */
    void setScale(float scale);

    /**
     * Sets the color of this holder.
     * Only works if the item is dyeable and the model faces have tintindex 0.
     */
    void setColor(int color);

    /**
     * Clears the color of this holder.
     * Only works if the item is dyeable and the model faces have tintindex 0.
     */
    default void clearColor() {
        this.setColor(-1);
    }
}
