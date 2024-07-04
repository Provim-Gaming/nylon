package org.provim.nylon.data.model.animated_java;

import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record AjNode(
        NodeType type,
        String name,
        UUID uuid,
        ResourceLocation resourceLocation
        // TODO: Implement bone configs
) {
    public enum NodeType {
        bone,
        locator
    }
}