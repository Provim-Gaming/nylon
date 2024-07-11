package org.provim.nylon.data.model.animated_java;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.UUID;

public record AjVariant(
        String name,
        @SerializedName("excluded_nodes") ReferenceOpenHashSet<UUID> excludedNodes
) {
}
