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

package org.provim.nylon.util;

import net.minecraft.network.syncher.EntityDataAccessor;
import org.provim.nylon.mixins.accessors.LivingEntityAccessor;

public class NylonConstants {
    public static final EntityDataAccessor<Integer> DATA_EFFECT_COLOR = LivingEntityAccessor.getDATA_EFFECT_COLOR_ID();
    public static final int DAMAGE_TINT_COLOR = 0xFF7E7E;
}
