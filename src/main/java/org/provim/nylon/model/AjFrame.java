package org.provim.nylon.model;

import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.UUID;

public record AjFrame(
        float time,
        Reference2ObjectOpenHashMap<UUID, AjPose> poses,

        UUID variant,
        ResourceLocation soundEffect,
        String command
) {
    public static class Deserializer implements JsonDeserializer<AjFrame> {
        @Override
        public AjFrame deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();

            float time = context.deserialize(object.get("time"), float.class);
            AjPose[] nodes = context.deserialize(object.get("nodes"), AjPose[].class);

            UUID variant = null;
            if (object.has("variant"))
                variant = context.deserialize(object.get("variant").getAsJsonObject().get("uuid"), UUID.class);

            ResourceLocation sound = null; // FIXME: not implemented in animated-java yet
            if (object.has("sound"))
                sound = context.deserialize(object.get("sound").getAsJsonObject().get("id"), ResourceLocation.class);

            String command = null;
            if (object.has("commands"))
                command = context.deserialize(object.get("commands").getAsJsonObject().get("commands"), String.class);

            Reference2ObjectOpenHashMap<UUID, AjPose> nodeMap = new Reference2ObjectOpenHashMap<>(nodes.length);
            for (AjPose pose : nodes) {
                nodeMap.put(pose.uuid(), pose);
            }

            return new AjFrame(time, nodeMap, variant, sound, command);
        }
    }
}