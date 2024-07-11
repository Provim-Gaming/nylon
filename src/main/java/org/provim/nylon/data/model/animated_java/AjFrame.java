package org.provim.nylon.data.model.animated_java;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record AjFrame(
        @SerializedName("node_transforms") AjTransform[] transforms,
        @Nullable Variant variant,
        @Nullable Commands commands
) {
    public record Variant(
            UUID uuid,
            @Nullable String executeCondition
    ) {
    }

    public record Commands( // TODO: Check for updates, seems to not exist in current AJ pre release.
            String commands,
            @Nullable String executeCondition
    ) {
    }
}