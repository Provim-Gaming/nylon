package org.provim.nylon.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.provim.nylon.model.*;

import java.io.*;
import java.util.UUID;

public class AjLoader {
    private static final Gson GSON = new GsonBuilder()
            // Animated java models
            .registerTypeAdapter(AjPose.class, new AjPose.Deserializer())
            .registerTypeAdapter(AjRig.class, new AjRig.Deserializer())
            .registerTypeAdapter(AjFrame.class, new AjFrame.Deserializer())
            .registerTypeAdapter(AjFrame.Variant.class, new AjFrame.Variant.Deserializer())
            .registerTypeAdapter(AjFrame.Command.class, new AjFrame.Command.Deserializer())

            // Reference equality
            .registerTypeAdapter(UUID.class, new ReferenceUuidDeserializer())

            // Custom deserializers
            .registerTypeAdapter(Matrix4f.class, new Matrix4fDeserializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fDeserializer())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(Item.class, new RegistryDeserializer<>(BuiltInRegistries.ITEM))
            .registerTypeAdapter(SoundEvent.class, new RegistryDeserializer<>(BuiltInRegistries.SOUND_EVENT))
            .create();

    public static AjModel require(ResourceLocation id) throws IllegalArgumentException, JsonParseException {
        return AjLoader.require(id, true);
    }

    public static AjModel require(String path) throws IllegalArgumentException, JsonParseException {
        return AjLoader.require(path, true);
    }

    public static AjModel require(ResourceLocation id, boolean replaceModelData) throws IllegalArgumentException, JsonParseException {
        String path = String.format("/ajmodels/%s/%s.json", id.getNamespace(), id.getPath());
        InputStream input = AjLoader.class.getResourceAsStream(path);
        if (input == null) {
            throw new IllegalArgumentException("Model doesn't exist: " + path);
        }

        return AjLoader.load(path, input, replaceModelData);
    }

    public static AjModel require(String path, boolean replaceModelData) throws IllegalArgumentException, JsonParseException {
        try (InputStream input = new FileInputStream(path)) {
            return AjLoader.load(path, input, replaceModelData);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Model doesn't exist: " + path);
        }
    }

    private static AjModel load(String path, InputStream input, boolean replaceModelData) throws JsonParseException {
        try (Reader reader = new InputStreamReader(input)) {
            AjModel model = GSON.fromJson(reader, AjModel.class);
            if (replaceModelData) {
                AjLoader.replaceModelData(model);
            }

            return model;
        } catch (Throwable throwable) {
            throw new JsonParseException("Failed to parse model: " + path, throwable);
        }
    }

    private static void replaceModelData(AjModel model) {
        Item rigItem = model.projectSettings().rigItem();

        // Node model data
        Object2ObjectOpenHashMap<UUID, AjNode> nodeMap = model.rig().nodeMap();
        for (AjNode entry : nodeMap.values()) {
            if (entry.type().hasModelData()) {
                nodeMap.computeIfPresent(entry.uuid(), ((id, node) -> new AjNode(
                        node.type(),
                        node.name(),
                        node.uuid(),
                        PolymerResourcePackUtils.requestModel(rigItem, node.resourceLocation()).value(),
                        node.resourceLocation(),
                        node.entityType()
                )));
            }
        }

        // Variant model data
        for (AjVariant variant : model.variants().values()) {
            Object2ObjectOpenHashMap<UUID, AjVariant.ModelInfo> models = variant.models();
            for (UUID uuid : models.keySet()) {
                models.computeIfPresent(uuid, ((id, modelInfo) -> new AjVariant.ModelInfo(
                        PolymerResourcePackUtils.requestModel(rigItem, modelInfo.resourceLocation()).value(),
                        modelInfo.resourceLocation()
                )));
            }
        }
    }
}
