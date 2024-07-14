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

package org.provim.nylon.data.deserialization;

import com.google.gson.*;
import org.joml.Vector3f;

import java.lang.reflect.Type;

public class Vector3fDeserializer implements JsonDeserializer<Vector3f> {
    @Override
    public Vector3f deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonArray jsonArray = element.getAsJsonArray();
        float x = jsonArray.get(0).getAsFloat();
        float y = jsonArray.get(1).getAsFloat();
        float z = jsonArray.get(2).getAsFloat();
        return new Vector3f(x, y, z);
    }
}