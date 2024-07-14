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

package org.provim.nylon.data.model.nylon;

import org.apache.commons.lang3.Validate;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.provim.nylon.holders.base.AbstractAjHolder;

public class Transform {
    private final Vector3fc translation;
    private final Vector3fc scale;
    private final Quaternionfc leftRotation;
    private final Quaternionfc rightRotation;

    public Transform(
            Vector3fc translation,
            Vector3fc scale,
            Quaternionfc leftRotation,
            Quaternionfc rightRotation
    ) {
        Validate.notNull(translation, "Translation cannot be null");
        Validate.notNull(scale, "Scale cannot be null");
        Validate.notNull(leftRotation, "Left rotation cannot be null");
        Validate.notNull(rightRotation, "Right rotation cannot be null");

        this.translation = translation;
        this.scale = scale;
        this.leftRotation = leftRotation;
        this.rightRotation = rightRotation;
    }

    public Vector3fc readOnlyTranslation() {
        return this.translation;
    }

    public Vector3fc readOnlyScale() {
        return this.scale;
    }

    public Quaternionfc readOnlyLeftRotation() {
        return this.leftRotation;
    }

    public Quaternionfc readOnlyRightRotation() {
        return this.rightRotation;
    }

    public Vector3f translation() {
        return new Vector3f(this.translation);
    }

    public Vector3f scale() {
        return new Vector3f(this.scale);
    }

    public Quaternionf leftRotation() {
        return new Quaternionf(this.leftRotation);
    }

    public Quaternionf rightRotation() {
        return new Quaternionf(this.rightRotation);
    }

    public void run(AbstractAjHolder holder) {
    }
}