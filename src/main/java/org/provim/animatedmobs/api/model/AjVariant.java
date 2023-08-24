package org.provim.animatedmobs.api.model;


import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record AjVariant(
        @SerializedName("name") String name,
        @SerializedName("uuid") String uuid,
        @SerializedName("models") Object2ObjectOpenHashMap<UUID, ModelInfo> models,
        @SerializedName("affected_bones") String[] affectedBones,
        @SerializedName("affected_bones_is_a_whitelist") boolean affectedBonesIsAWhitelist
) {
    public record ModelInfo(
            @SerializedName("custom_model_data") int customModelData,
            @SerializedName("resource_location") ResourceLocation id
    ) {
    }
}

