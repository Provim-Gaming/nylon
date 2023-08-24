package org.provim.animatedmobs.api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.provim.animatedmobs.api.model.AjModel;

import java.io.*;

public class AjLoader {
    public static final File MODEL_DIR = FabricLoader.getInstance().getConfigDir().resolve("AnimatedMobs/models").toFile();

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(Matrix4f.class, new Matrix4fDeserializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fDeserializer())
            .registerTypeAdapter(Item.class, new RegistryDeserializer<>(BuiltInRegistries.ITEM))
            .create();

    public static AjModel require(String name) throws IllegalArgumentException, JsonParseException {
        if (!MODEL_DIR.exists()) {
            MODEL_DIR.mkdirs();
        }

        File file = new File(MODEL_DIR, name + ".json");
        if (!file.exists()) {
            throw new IllegalArgumentException("Model doesn't exist: " + name);
        }

        try (Reader reader = new FileReader(file)) {
            return GSON.fromJson(reader, AjModel.class);
        } catch (Throwable throwable) {
            throw new JsonParseException("Failed to parse model: " + name, throwable);
        }
    }
}
