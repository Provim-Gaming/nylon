package org.provim.nylon.model;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

public record AjAnimation(
        AjFrame[] frames,
        @SerializedName("start_delay") int startDelay,
        @SerializedName("loop_delay") int loopDelay,

        @SerializedName("duration") int duration,
        @SerializedName("loop_mode") LoopMode loopMode,
        @SerializedName("affected_bones") ReferenceOpenHashSet<String> affectedBones,
        @SerializedName("affected_bones_is_a_whitelist") boolean affectedBonesIsAWhitelist
) {

    public boolean isAffected(String boneName) {
        return this.affectedBonesIsAWhitelist == this.affectedBones.contains(boneName);
    }

    public enum LoopMode {
        once, hold, loop
    }
}
