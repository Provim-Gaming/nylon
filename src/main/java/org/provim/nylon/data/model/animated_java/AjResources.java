package org.provim.nylon.data.model.animated_java;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.UUID;

public record AjResources(
        @SerializedName("variant_models") Map<UUID, Map<UUID, VariantModel>> variantModels,
        Map<UUID, JsonObject> models,
        Map<String, Texture> textures,
        String modelExportFolder
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
