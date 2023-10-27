package org.provim.nylon.model;

import com.google.gson.*;
import com.mojang.math.MatrixUtil;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.*;

import java.lang.reflect.Type;
import java.util.UUID;

public record AjPose(
        UUID uuid,
        Matrix4fc matrix,

        Vector3fc translation,
        Quaternionfc leftRotation,
        Quaternionfc rightRotation,
        Vector3fc scale
) {
    public static class Deserializer implements JsonDeserializer<AjPose> {
        @Override
        public AjPose deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();

            UUID uuid = context.deserialize(object.get("uuid"), UUID.class);
            Matrix4f matrix = context.deserialize(object.get("matrix"), Matrix4f.class);

            float f = 1.0F / matrix.m33();
            Triple<Quaternionf, Vector3f, Quaternionf> triple;
            triple = MatrixUtil.svdDecompose(new Matrix3f().set(matrix).scale(f));

            Vector3f scale = triple.getMiddle();
            Vector3f translation = matrix.getTranslation(new Vector3f());
            Quaternionf leftRotation = triple.getLeft();
            Quaternionf rightRotation = triple.getRight();

            return new AjPose(uuid, matrix, translation, leftRotation, rightRotation, scale);
        }
    }
}
