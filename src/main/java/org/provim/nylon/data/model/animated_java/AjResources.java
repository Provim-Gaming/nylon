package org.provim.nylon.data.model.animated_java;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.UUID;

public record AjResources(
        String textureExportFolder,
        String modelExportFolder,
        Map<UUID, JsonObject> models,
        Map<String, Map<UUID, VariantModel>> variant_models,
        Map<String, Texture> textures
) {
    public record Texture(
            String expectedPath,
            String src
    ) {
    }

    public record VariantModel(
            String modelPath,
            ResourceLocation resourceLocation,
            JsonObject model
    ) {
    }
}
