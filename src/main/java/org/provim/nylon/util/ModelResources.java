package org.provim.nylon.util;

import com.google.gson.JsonObject;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.nio.charset.StandardCharsets;

public class ModelResources {
    private static final Object2ObjectOpenHashMap<String, byte[]> RESOURCES = new Object2ObjectOpenHashMap<>();

    public static void addResource(String path, JsonObject data) {
        RESOURCES.put(path, data.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void buildResources(ResourcePackBuilder builder) {
        for (String key : RESOURCES.keySet()) {
            builder.addData(key, RESOURCES.get(key));
        }
    }
}
