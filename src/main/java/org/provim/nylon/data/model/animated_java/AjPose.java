package org.provim.nylon.data.model.animated_java;

import org.joml.Matrix4f;

import java.util.UUID;

public record AjPose(
        UUID uuid,
        Matrix4f matrix
) {
}
