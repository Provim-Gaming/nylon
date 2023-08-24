package org.provim.animatedmobs.api.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.world.item.Item;

public record AjProjectSettings(
        @SerializedName("project_namespace") String projectNamespace,
        @SerializedName("project_resolution") int[] projectResolution,
        @SerializedName("rig_item") Item rigItem,
        @SerializedName("rig_item_model") String rigItemModel,
        @SerializedName("rig_export_folder") String rigExportFolder,
        @SerializedName("texture_export_folder") String textureExportFolder,
        @SerializedName("enable_advanced_resource_pack_settings") boolean enableAdvancedResourcePackSettings,
        @SerializedName("resource_pack_mcmeta") String resourcePackMcmeta,
        @SerializedName("verbose") boolean verbose,
        @SerializedName("exporter") String exporter
) {
}
