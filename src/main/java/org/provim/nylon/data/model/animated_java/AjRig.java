package org.provim.nylon.data.model.animated_java;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.UUID;

public record AjRig(
        @SerializedName("node_map") Object2ObjectOpenHashMap<UUID, AjNode> node_map,
        @SerializedName("default_pose") AjPose[] default_pose
) {
}
