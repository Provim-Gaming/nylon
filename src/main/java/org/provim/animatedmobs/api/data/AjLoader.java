package org.provim.animatedmobs.api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.provim.animatedmobs.api.model.AjModel;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class AjLoader {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(Matrix4f.class, new Matrix4fDeserializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fDeserializer())
            .registerTypeAdapter(Item.class, new RegistryDeserializer<>(BuiltInRegistries.ITEM))
            .create();

    public static AjModel require(EntityType<?> type) throws IllegalArgumentException, JsonParseException {
        ResourceLocation id = EntityType.getKey(type);
        String path = String.format("/ajmodels/%s/%s.json", id.getNamespace(), id.getPath());

        InputStream input = AjLoader.class.getResourceAsStream(path);
        if (input == null) {
            throw new IllegalArgumentException("Model doesn't exist: " + path);
        }

        try (Reader reader = new InputStreamReader(input)) {
            return GSON.fromJson(reader, AjModel.class);
        } catch (Throwable throwable) {
            throw new JsonParseException("Failed to parse model: " + path, throwable);
        }
    }
}
