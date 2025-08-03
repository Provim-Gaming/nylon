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

package org.provim.nylon.holders.entity.simple;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.data.model.nylon.NylonModel;
import org.provim.nylon.holders.entity.EntityHolder;

public class SimpleEntityHolder<T extends Entity & AjEntity> extends EntityHolder<T> {
    public SimpleEntityHolder(T parent, NylonModel model) {
        super(parent, model);
    }

    @Override
    protected void addDirectPassengers(IntList passengers) {
        super.addDirectPassengers(passengers);
        for (int display : this.getDisplayIds()) {
            passengers.add(display);
        }
    }
}
