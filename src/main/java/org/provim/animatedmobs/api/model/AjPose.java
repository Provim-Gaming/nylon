package org.provim.animatedmobs.api.model;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

@SuppressWarnings("unused")
public class AjPose {
    private UUID uuid;
    private Matrix4f matrix;

    private transient Vector3f translation;
    private transient Quaternionf rotation;
    private transient Vector3f scale;

    public UUID getUuid() {
        return this.uuid;
    }

    public Matrix4f getMatrix() {
        return this.matrix;
    }

    public Vector3f getPos() {
        if (this.translation == null) {
            this.translation = this.matrix.getTranslation(new Vector3f());
        }
        return new Vector3f(this.translation);
    }

    public Quaternionf getRot() {
        if (this.rotation == null) {
            this.rotation = this.matrix.getNormalizedRotation(new Quaternionf());
        }
        return new Quaternionf(this.rotation);
    }

    public Vector3f getScale() {
        if (this.scale == null) {
            this.scale = this.matrix.getScale(new Vector3f());
        }
        return new Vector3f(this.scale);
    }
}
