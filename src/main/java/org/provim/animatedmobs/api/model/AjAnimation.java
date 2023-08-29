package org.provim.animatedmobs.api.model;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public record AjAnimation(
        ObjectArrayList<AjFrame> frames,
        @SerializedName("loop_mode") LoopMode loopMode
) {
    public enum LoopMode {
        once, loop
    }
}
