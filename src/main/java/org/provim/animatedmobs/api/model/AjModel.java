package org.provim.animatedmobs.api.model;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.UUID;

@SuppressWarnings("unused")
public record AjModel(
        @SerializedName("project_settings") AjProjectSettings projectSettings,
        @SerializedName("rig") AjRig rig,
        @SerializedName("variants") Object2ObjectOpenHashMap<UUID, AjVariant> variants,
        @SerializedName("animations") Object2ObjectOpenHashMap<String, AjAnimation> animations
) {
}
