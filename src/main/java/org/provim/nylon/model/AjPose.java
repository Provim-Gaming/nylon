package org.provim.nylon.model;

import com.google.gson.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.UUID;

public record AjPose(
        UUID uuid,
        Matrix4f matrix,

        Vector3f translation,
        Quaternionf rotation,
        Vector3f scale
) {
    @Override
    public Vector3f translation() {
        return new Vector3f(this.translation);
    }

    @Override
    public Quaternionf rotation() {
        return new Quaternionf(this.rotation);
    }

    @Override
    public Vector3f scale() {
        return new Vector3f(this.scale);
    }

    public static class Deserializer implements JsonDeserializer<AjPose> {
        @Override
        public AjPose deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();

            UUID uuid = context.deserialize(object.get("uuid"), UUID.class);
            Matrix4f matrix = context.deserialize(object.get("matrix"), Matrix4f.class);

            Vector3f scale = matrix.getScale(new Vector3f());
            Vector3f translation = matrix.getTranslation(new Vector3f());
            Quaternionf rotation = matrix.getNormalizedRotation(new Quaternionf());

            return new AjPose(uuid, matrix, translation, rotation, scale);
        }
    }
}
