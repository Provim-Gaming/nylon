package org.provim.animatedmobs.api.model;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.UUID;

public record AjAnimation(
        ObjectArrayList<AjFrame> frames,
        @SerializedName("loop_mode") LoopMode loopMode
) {
    public enum LoopMode {
        ONCE, LOOP
    }

    public record AjFrame(
            float time,
            @SerializedName("nodes") ObjectArrayList<AjPose> poses
    ) {
        public AjPose findPose(UUID uuid) {
            for (AjPose pose : this.poses) {
                if (pose.getUuid().equals(uuid)) {
                    return pose;
                }
            }
            return null;
        }
    }
}
