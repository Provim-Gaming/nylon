package org.provim.nylon.model;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.UUID;

/**
 * Represents an Animated Java JSON model exported from Blockbench.
 * <a href="https://github.com/Animated-Java/animated-java/blob/main/exporters/jsonExporter/jsonStructure.json">JSON Structure</a>
 */
public record AjModel(
        @SerializedName("project_settings") AjProjectSettings projectSettings,
        @SerializedName("rig") AjRig rig,
        @SerializedName("variants") Reference2ObjectOpenHashMap<UUID, AjVariant> variants,
        @SerializedName("animations") Object2ObjectOpenHashMap<String, AjAnimation> animations
) {
}
