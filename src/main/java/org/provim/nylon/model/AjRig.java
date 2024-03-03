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

package org.provim.nylon.model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.lang.reflect.Type;
import java.util.UUID;

public record AjRig(
        Object2ObjectOpenHashMap<UUID, AjNode> nodeMap,
        Reference2ObjectOpenHashMap<UUID, AjPose> defaultPose
) {
    public static class Deserializer implements JsonDeserializer<AjRig> {
        @Override
        public AjRig deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();

            Object2ObjectOpenHashMap<UUID, AjNode> nodeMap = context.deserialize(object.get("node_map"), new TypeToken<Object2ObjectOpenHashMap<UUID, AjNode>>() {}.getType());
            AjPose[] defaultPoses = context.deserialize(object.get("default_pose"), AjPose[].class);

            Reference2ObjectOpenHashMap<UUID, AjPose> defaultPoseMap = new Reference2ObjectOpenHashMap<>(defaultPoses.length);
            for (AjPose pose : defaultPoses) {
                defaultPoseMap.put(pose.uuid(), pose);
            }

            return new AjRig(nodeMap, defaultPoseMap);
        }
    }
}
