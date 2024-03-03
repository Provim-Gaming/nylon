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

package org.provim.nylon.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.lang.reflect.Type;
import java.util.UUID;

/**
 * The purpose of this deserializer is to reuse matching UUIDs, so that we can use reference equality.
 */
public class ReferenceUuidDeserializer implements JsonDeserializer<UUID> {
    private static final Object2ObjectOpenHashMap<String, UUID> UUID_CACHE = new Object2ObjectOpenHashMap<>();

    @Override
    public UUID deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        String string = element.getAsString();
        UUID uuid = UUID_CACHE.get(string);
        if (uuid != null) {
            return uuid;
        }

        uuid = UUID.fromString(string);
        UUID_CACHE.put(string, uuid);
        return uuid;
    }
}
