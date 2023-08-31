package org.provim.animatedmobs.api.model;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public record AjAnimation(
        AjFrame[] frames,
        @SerializedName("loop_mode") LoopMode loopMode,
        @SerializedName("affected_bones") ObjectOpenHashSet<String> affectedBones,
        @SerializedName("affected_bones_is_a_whitelist") boolean affectedBonesIsAWhitelist
) {

    public boolean isUnaffected(String boneName) {
        return this.affectedBonesIsAWhitelist != this.affectedBones.contains(boneName);
    }

    public enum LoopMode {
        once, hold, loop
    }
}
