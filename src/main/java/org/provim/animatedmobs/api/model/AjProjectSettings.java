package org.provim.animatedmobs.api.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.world.item.Item;

public record AjProjectSettings(
        @SerializedName("project_namespace") String projectNamespace,
        @SerializedName("rig_item") Item rigItem,
        @SerializedName("rig_item_model") String rigItemModel
) {
}
