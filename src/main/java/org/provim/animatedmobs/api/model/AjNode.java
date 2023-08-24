package org.provim.animatedmobs.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public record AjNode(
        String type,
        String name,
        UUID uuid,
        @SerializedName("custom_model_data") int customModelData,
        @SerializedName("resource_location") String resourceLocation
) {
}