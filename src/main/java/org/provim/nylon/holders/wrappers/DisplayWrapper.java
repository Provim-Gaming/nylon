package org.provim.nylon.holders.wrappers;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

public class DisplayWrapper<T extends DisplayElement> extends AbstractWrapper {
    private final TrackedData trackedData = new TrackedData();
    private final T element;
    private final boolean isHead;

    public DisplayWrapper(T element, AbstractWrapper wrapper, boolean isHead) {
        this(element, wrapper.node(), wrapper.getDefaultPose(), isHead);
    }

    public DisplayWrapper(T element, AjNode node, AjPose defaultPose, boolean isHead) {
        super(node, defaultPose);
        this.element = element;
        this.isHead = isHead;
    }

    public T element() {
        return this.element;
    }

    public boolean isHead() {
        return this.isHead;
    }

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

    public void setScale(Vector3f scale) {
        if (this.trackedData.updateScale(scale)) {
            this.element.getDataTracker().set(DisplayTrackedData.SCALE, scale, true);
        }
    }

    public void setTranslation(Vector3f translation) {
        if (this.trackedData.updateTranslation(translation)) {
            this.element.getDataTracker().set(DisplayTrackedData.TRANSLATION, translation, true);
        }
    }

    public void setRightRotation(Quaternionf rightRotation) {
        if (this.trackedData.updateRightRotation(rightRotation)) {
            this.element.getDataTracker().set(DisplayTrackedData.RIGHT_ROTATION, rightRotation, true);
        }
    }

    public void setLeftRotation(Quaternionf leftRotation) {
        if (this.trackedData.updateLeftRotation(leftRotation)) {
            this.element.getDataTracker().set(DisplayTrackedData.LEFT_ROTATION, leftRotation, true);
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

        public boolean updateScale(Vector3f scale) {
            if (!this.scale.equals(scale)) {
                this.scale.set(scale);
                this.dirty = true;
                return true;
            }
            return false;
        }

        public boolean updateTranslation(Vector3f translation) {
            if (!this.translation.equals(translation)) {
                this.translation.set(translation);
                this.dirty = true;
                return true;
            }
            return false;
        }

        public boolean updateRightRotation(Quaternionf rightRotation) {
            if (!this.rightRotation.equals(rightRotation)) {
                this.rightRotation.set(rightRotation);
                this.dirty = true;
                return true;
            }
            return false;
        }

        public boolean updateLeftRotation(Quaternionf leftRotation) {
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
