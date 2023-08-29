package org.provim.animatedmobs.api.model;

import com.google.gson.annotations.SerializedName;

public record AjAnimation(
        AjFrame[] frames,
        @SerializedName("loop_mode") LoopMode loopMode
) {
    public enum LoopMode {
        once, loop
    }
}
