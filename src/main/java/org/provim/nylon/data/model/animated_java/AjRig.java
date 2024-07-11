package org.provim.nylon.data.model.animated_java;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.UUID;

public record AjRig(
        @SerializedName("node_map") Object2ObjectOpenHashMap<UUID, AjNode> nodeMap,
        @SerializedName("default_transforms") AjTransform[] defaultTransforms,
        Object2ObjectOpenHashMap<UUID, AjVariant> variants
) {
}
