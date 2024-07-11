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

package org.provim.nylon.extra;

import org.provim.nylon.data.model.nylon.Transform;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.wrappers.DisplayWrapper;
import org.provim.nylon.holders.wrappers.Locator;

/**
 * Listener for locators, updates a single DisplayElement
 */
public class DisplayElementUpdateListener implements Locator.LocatorListener {
    protected final DisplayWrapper<?> display;

    public DisplayElementUpdateListener(DisplayWrapper<?> display) {
        this.display = display;
    }

    @Override
    public void update(AbstractAjHolder holder, Transform transform) {
        holder.updateElement(this.display, transform);
    }
}