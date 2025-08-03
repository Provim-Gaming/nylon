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

package org.provim.nylon.component;

import org.provim.nylon.data.model.nylon.NylonModel;
import org.provim.nylon.holders.base.AbstractAjHolder;

public abstract class ComponentBase {
    protected final NylonModel model;
    protected final AbstractAjHolder holder;

    public ComponentBase(NylonModel model, AbstractAjHolder holder) {
        this.model = model;
        this.holder = holder;
    }
}
