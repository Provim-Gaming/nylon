package org.provim.nylon.data.model.nylon;

import org.apache.commons.lang3.Validate;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@SuppressWarnings("ClassCanBeRecord")
public class Transform {
    private final Vector3f translation;
    private final Vector3f scale;
    private final Quaternionf leftRotation;
    private final Quaternionf rightRotation;

    public Transform(
            Vector3f translation,
            Vector3f scale,
            Quaternionf leftRotation,
            Quaternionf rightRotation
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
}