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

import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class SoundRegistry {
    public static final SoundEvent GOLEM_AMBIENT = register("golem.ambient", SoundEvents.EMPTY);
    public static final SoundEvent GOLEM_HURT = register("golem.hurt", SoundEvents.IRON_GOLEM_HURT);
    public static final SoundEvent GOLEM_DEATH = register("golem.death", SoundEvents.IRON_GOLEM_DEATH);

    private static SoundEvent register(String name, SoundEvent soundEvent) {
        ResourceLocation identifier = new ResourceLocation("animated_java", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, PolymerSoundEvent.of(identifier, soundEvent));
    }

    public static void registerSounds() {
    }
}
