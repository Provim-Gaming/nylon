package org.provim.animatedmobs.api.model;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.UUID;

public record AjRig(
        @SerializedName("default_pose") ObjectArrayList<AjPose> defaultPose,
        @SerializedName("node_structure") AjNodeStructure nodeStructure,
        @SerializedName("node_map") Object2ObjectOpenHashMap<UUID, AjNode> nodeMap
) {
    public AjPose getDefaultPose(UUID uuid) {
        for (AjPose pose : this.defaultPose) {
            if (pose.getUuid().equals(uuid)) {
                return pose;
            }
        }
        return null;
    }
}
