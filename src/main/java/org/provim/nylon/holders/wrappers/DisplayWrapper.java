/*
 * Nylon
 * Copyright (C) 2023, 2024 Provim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.provim.nylon.holders.wrappers;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
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

    public void setScale(Vector3fc scale) {
        if (this.trackedData.updateScale(scale)) {
            this.element.getDataTracker().set(DisplayTrackedData.SCALE, this.getScale(), true);
        }
    }

    public void setTranslation(Vector3fc translation) {
        if (this.trackedData.updateTranslation(translation)) {
            this.element.getDataTracker().set(DisplayTrackedData.TRANSLATION, this.getTranslation(), true);
        }
    }

    public void setRightRotation(Quaternionfc rightRotation) {
        if (this.trackedData.updateRightRotation(rightRotation)) {
            this.element.getDataTracker().set(DisplayTrackedData.RIGHT_ROTATION, this.getRightRotation(), true);
        }
    }

    public void setLeftRotation(Quaternionfc leftRotation) {
        if (this.trackedData.updateLeftRotation(leftRotation)) {
            this.element.getDataTracker().set(DisplayTrackedData.LEFT_ROTATION, this.getLeftRotation(), true);
        }
    }

    public Vector3f getScale() {
        return this.trackedData.scale;
    }

    public Vector3f getTranslation() {
        return this.trackedData.translation;
    }

    public Quaternionf getRightRotation() {
        return this.trackedData.rightRotation;
    }

    public Quaternionf getLeftRotation() {
        return this.trackedData.leftRotation;
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
    }
}
