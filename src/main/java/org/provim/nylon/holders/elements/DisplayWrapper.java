package org.provim.nylon.holders.elements;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

public abstract class DisplayWrapper<T extends DisplayElement> {
    private final TrackedData trackedData = new TrackedData();
    private final AjPose defaultPose;
    private final AjNode node;
    private final T element;

    protected DisplayWrapper(T element, AjNode node, AjPose defaultPose) {
        this.defaultPose = defaultPose;
        this.element = element;
        this.node = node;
    }

    public T element() {
        return this.element;
    }

    public AjNode node() {
        return this.node;
    }

    public AjPose getDefaultPose() {
        return this.defaultPose;
    }

    abstract public boolean isHead();

    /**
     * Starts the interpolation of the display element.
     * This method should be called after all the needed data values have been set.
     */
    public void startInterpolation() {
        if (this.trackedData.isDirty()) {
            this.element.startInterpolation();
            this.trackedData.setDirty(false);
        }
    }

    public void setScale(Vector3fc scale) {
        if (this.trackedData.updateScale(scale)) {
            this.element.setScale(scale);
        }
    }

    public void setTranslation(Vector3fc translation) {
        if (this.trackedData.updateTranslation(translation)) {
            this.element.setTranslation(translation);
        }
    }

    public void setRightRotation(Quaternionfc rightRotation) {
        if (this.trackedData.updateRightRotation(rightRotation)) {
            this.element.setRightRotation(rightRotation);
        }
    }

    public void setLeftRotation(Quaternionfc leftRotation) {
        if (this.trackedData.updateLeftRotation(leftRotation)) {
            this.element.setLeftRotation(leftRotation);
        }
    }

    public Vector3f getScale() {
        return this.trackedData.getScale();
    }

    public Vector3f getTranslation() {
        return this.trackedData.getTranslation();
    }

    public Quaternionf getRightRotation() {
        return this.trackedData.getRightRotation();
    }

    public Quaternionf getLeftRotation() {
        return this.trackedData.getLeftRotation();
    }

    public static class TrackedData {
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
}
