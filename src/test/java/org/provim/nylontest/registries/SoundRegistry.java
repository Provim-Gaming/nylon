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

package org.provim.nylontest.registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundRegistry {
    public static final SoundEvent GOLEM_AMBIENT = create("golem.ambient");
    public static final SoundEvent GOLEM_HURT = create("golem.hurt");
    public static final SoundEvent GOLEM_DEATH = create("golem.death");

    private static SoundEvent create(String name) {
        ResourceLocation identifier = ResourceLocation.fromNamespaceAndPath("animated_java", name);
        return SoundEvent.createVariableRangeEvent(identifier);
    }

    public static void registerSounds() {
    }
}
