package org.provim.animatedmobs.api.model;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.UUID;

public record AjNodeStructure(
        @SerializedName("uuid") UUID uuid,
        @SerializedName("children") ObjectArrayList<AjNodeStructure> childNodes
) {
}
