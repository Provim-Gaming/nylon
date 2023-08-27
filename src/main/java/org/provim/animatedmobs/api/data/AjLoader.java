package org.provim.animatedmobs.api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.AjNode;
import org.provim.animatedmobs.api.model.AjVariant;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.UUID;

public class AjLoader {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(Matrix4f.class, new Matrix4fDeserializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fDeserializer())
            .registerTypeAdapter(Item.class, new RegistryDeserializer<>(BuiltInRegistries.ITEM))
            .create();

    public static AjModel require(ResourceLocation id) throws IllegalArgumentException, JsonParseException {
        String path = String.format("/ajmodels/%s/%s.json", id.getNamespace(), id.getPath());

        InputStream input = AjLoader.class.getResourceAsStream(path);
        if (input == null) {
            throw new IllegalArgumentException("Model doesn't exist: " + path);
        }

        try (Reader reader = new InputStreamReader(input)) {
            AjModel model = GSON.fromJson(reader, AjModel.class);
            AjLoader.replaceModelData(model);
            return model;
        } catch (Throwable throwable) {
            throw new JsonParseException("Failed to parse model: " + path, throwable);
        }
    }

    private static void replaceModelData(AjModel ajModel) {
        Item rigItem = ajModel.projectSettings().rigItem();

        // Node models
        Object2ObjectOpenHashMap<UUID, AjNode> nodeMap = ajModel.rig().nodeMap();
        for (Map.Entry<UUID, AjNode> entry : nodeMap.entrySet()) {
            if (entry.getValue().type() == AjNode.NodeType.bone) {
                nodeMap.computeIfPresent(entry.getKey(), ((id, node) -> new AjNode(
                        node.type(),
                        node.name(),
                        node.uuid(),
                        PolymerResourcePackUtils.requestModel(rigItem, node.resourceLocation()).value(),
                        node.resourceLocation(),
                        node.entityType()
                )));
            }
        }

        // Variant models
        for (AjVariant variant : ajModel.variants().values()) {
            Object2ObjectOpenHashMap<UUID, AjVariant.ModelInfo> models = variant.models();
            for (UUID uuid : models.keySet()) {
                models.computeIfPresent(uuid, ((id, model) -> new AjVariant.ModelInfo(
                        PolymerResourcePackUtils.requestModel(rigItem, model.resourceLocation()).value(),
                        model.resourceLocation()
                )));
            }
        }
    }
}
