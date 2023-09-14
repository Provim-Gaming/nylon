package org.provim.animatedmobs.api.model;

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
