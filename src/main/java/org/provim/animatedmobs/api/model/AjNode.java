package org.provim.animatedmobs.api.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record AjNode(
        NodeType type,
        String name,
        UUID uuid,
        @SerializedName("custom_model_data") int customModelData,
        @SerializedName("resource_location") ResourceLocation resourceLocation,
        @SerializedName("entity_type") ResourceLocation entityType
) {
    public enum NodeType {
        bone(true),
        locator(false);

        private final boolean hasModelData;

        NodeType(boolean hasModelData) {
            this.hasModelData = hasModelData;
        }

        public boolean hasModelData() {
            return this.hasModelData;
        }
    }
}