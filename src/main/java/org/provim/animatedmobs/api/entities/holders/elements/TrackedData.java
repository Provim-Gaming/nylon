package org.provim.animatedmobs.api.entities.holders.elements;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class TrackedData {
    private final Quaternionf rightRotation = new Quaternionf();
    private final Quaternionf leftRotation = new Quaternionf();
    private final Vector3f translation = new Vector3f();
    private final Vector3f scale = new Vector3f();
    private boolean dirty;

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean updateScale(Vector3fc scale) {
        if (!this.scale.equals(scale)) {
            this.scale.set(scale);
            this.dirty = true;
            return true;
        }
        return false;
    }

    public boolean updateTranslation(Vector3fc translation) {
        if (!this.translation.equals(translation)) {
            this.translation.set(translation);
            this.dirty = true;
            return true;
        }
        return false;
    }

    public boolean updateRightRotation(Quaternionfc rightRotation) {
        if (!this.rightRotation.equals(rightRotation)) {
            this.rightRotation.set(rightRotation);
            this.dirty = true;
            return true;
        }
        return false;
    }

    public boolean updateLeftRotation(Quaternionfc leftRotation) {
        if (!this.leftRotation.equals(leftRotation)) {
            this.leftRotation.set(leftRotation);
            this.dirty = true;
            return true;
        }
        return false;
    }

    public Vector3f getScale() {
        return this.scale;
    }

    public Vector3f getTranslation() {
        return this.translation;
    }

    public Quaternionf getRightRotation() {
        return this.rightRotation;
    }

    public Quaternionf getLeftRotation() {
        return this.leftRotation;
    }
}
