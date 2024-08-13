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

import com.google.gson.JsonObject;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.nio.charset.StandardCharsets;

public class ModelResources {
    private static final Object2ObjectOpenHashMap<String, byte[]> RESOURCES = new Object2ObjectOpenHashMap<>();

    public static void addResource(String path, JsonObject data) {
        addResource(path, data.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void addResource(String path, byte[] data) {
        RESOURCES.put(path, data);
    }

    public static void buildResources(ResourcePackBuilder builder) {
        for (String key : RESOURCES.keySet()) {
            builder.addData(key, RESOURCES.get(key));
        }
    }
}
