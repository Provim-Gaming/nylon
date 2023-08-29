package org.provim.animatedmobs.api.model;

import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.lang.reflect.Type;
import java.util.UUID;

public record AjFrame(
        float time,
        Object2ObjectOpenHashMap<UUID, AjPose> nodes
) {
    public static class Deserializer implements JsonDeserializer<AjFrame> {
        @Override
        public AjFrame deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();

            float time = context.deserialize(object.get("time"), float.class);
            AjPose[] nodes = context.deserialize(object.get("nodes"), AjPose[].class);

            Object2ObjectOpenHashMap<UUID, AjPose> nodeMap = new Object2ObjectOpenHashMap<>(nodes.length);
            for (AjPose pose : nodes) {
                nodeMap.put(pose.uuid(), pose);
            }

            return new AjFrame(time, nodeMap);
        }
    }
}