package org.provim.animatedmobs.api.entities.holders.elements;

import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class FastTextDisplayElement extends TextDisplayElement {
    private final TrackedData trackedData = new TrackedData();

    @Override
    public void tick() {
        super.tick();
        this.trackedData.setDirty(false);
    }

    @Override
    public void startInterpolation() {
        if (this.trackedData.isDirty()) {
            super.startInterpolation();
        }
    }

    @Override
    public void setScale(Vector3fc scale) {
        if (this.trackedData.updateScale(scale)) {
            super.setScale(scale);
        }
    }

    @Override
    public void setTranslation(Vector3fc translation) {
        if (this.trackedData.updateTranslation(translation)) {
            super.setTranslation(translation);
        }
    }

    @Override
    public void setRightRotation(Quaternionfc rightRotation) {
        if (this.trackedData.updateRightRotation(rightRotation)) {
            super.setRightRotation(rightRotation);
        }
    }

    @Override
    public void setLeftRotation(Quaternionfc leftRotation) {
        if (this.trackedData.updateLeftRotation(leftRotation)) {
            super.setLeftRotation(leftRotation);
        }
    }

    @Override
    public Vector3f getScale() {
        return this.trackedData.getScale();
    }

    @Override
    public Vector3f getTranslation() {
        return this.trackedData.getTranslation();
    }

    @Override
    public Quaternionf getRightRotation() {
        return this.trackedData.getRightRotation();
    }

    @Override
    public Quaternionf getLeftRotation() {
        return this.trackedData.getLeftRotation();
    }
}
