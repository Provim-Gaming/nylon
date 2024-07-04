package org.provim.nylon.data.model.animated_java;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record AjFrame(
        AjPose[] nodes,
        @Nullable Variant variant,
        @Nullable Commands commands
) {
    public record Variant(
            UUID uuid, // TODO: This is referenced by UUID but there's no way to get the variant definition by UUID from the json export file.
            @Nullable String executeCondition
    ) {
    }

    public record Commands(
            String commands,
            @Nullable String executeCondition
    ) {
    }
}