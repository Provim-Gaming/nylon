package org.provim.nylon.model;

import com.google.gson.*;
import com.mojang.math.Axis;
import com.mojang.math.MatrixUtil;
import org.joml.*;

import java.lang.reflect.Type;
import java.util.UUID;

public record AjPose(
        UUID uuid,
        Vector3f translation,
        Vector3f scale,
        Quaternionf leftRotation,
        Quaternionf rightRotation
) {
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

    @Override
    public Vector3f translation() {
        return new Vector3f(this.translation);
    }

    @Override
    public Vector3f scale() {
        return new Vector3f(this.scale);
    }

    @Override
    public Quaternionf leftRotation() {
        return new Quaternionf(this.leftRotation);
    }

    @Override
    public Quaternionf rightRotation() {
        return new Quaternionf(this.rightRotation);
    }

    public static class Deserializer implements JsonDeserializer<AjPose> {
        private static final Quaternionf ROT_180 = Axis.YP.rotationDegrees(180.f);

        @Override
        public AjPose deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();

            UUID uuid = context.deserialize(object.get("uuid"), UUID.class);
            Matrix4f matrix4f = context.deserialize(object.get("matrix"), Matrix4f.class);
            Matrix3f matrix3f = new Matrix3f(matrix4f);
            Vector3f translation = matrix4f.getTranslation(new Vector3f());

            float multiplier = 1.0F / matrix4f.m33();
            if (multiplier != 1.0F) {
                matrix3f.scale(multiplier);
                translation.mul(multiplier);
            }

            var triple = MatrixUtil.svdDecompose(matrix3f);
            Vector3f scale = triple.getMiddle();
            Quaternionf leftRotation = triple.getLeft().mul(ROT_180);
            Quaternionf rightRotation = triple.getRight();

            return new AjPose(uuid, translation, scale, leftRotation, rightRotation);
        }
    }
}
