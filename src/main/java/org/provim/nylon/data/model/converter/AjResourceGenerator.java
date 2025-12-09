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

package org.provim.nylon.data.model.converter;

import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import net.minecraft.resources.Identifier;
import org.provim.nylon.data.model.animated_java.AjModel;
import org.provim.nylon.data.model.animated_java.AjNode;
import org.provim.nylon.data.model.animated_java.AjTexture;
import org.provim.nylon.data.model.animated_java.AjVariant;
import org.provim.nylon.util.ModelResources;

import java.util.Base64;
import java.util.UUID;

public class AjResourceGenerator {
    private static final String RESOURCE_NAMESPACE = "animated_java";
    private static final String RESOURCE_PATH = "blueprint";

    public static void generate(AjModel ajModel) {
        String namespace = ajModel.settings().exportNamespace();

        for (AjVariant variant : ajModel.variants().values()) {
            for (UUID nodeUuid : variant.models().keySet()) {
                AjVariant.ModelData data = variant.models().get(nodeUuid);
                AjNode node = ajModel.nodes().get(nodeUuid);

                String modelPath;
                Identifier modelId;
                if (variant.isDefault()) {
                    modelPath = defaultModelPath(namespace, node.name());
                    modelId = AjModelConverter.defaultModelId(namespace, node.name());
                } else {
                    modelPath = modelPath(namespace, node.name(), variant.name());
                    modelId = AjModelConverter.modelId(namespace, node.name(), variant.name());
                }

                ModelResources.addResource(
                        modelPath,
                        data.model()
                );

                BasicItemModel itemModel = new BasicItemModel(modelId);
                ItemAsset asset = new ItemAsset(itemModel, ItemAsset.Properties.DEFAULT);
                ModelResources.addResource(
                        AssetPaths.itemAsset(modelId),
                        asset.toBytes()
                );
            }
        }

        for (AjTexture texture : ajModel.textures().values()) {
            ModelResources.addResource(
                    texturePath(namespace, texture.name()),
                    Base64.getDecoder().decode(texture.src().substring("data:image/png;base64,".length()))
            );
        }
    }

    private static String texturePath(String namespace, String textureName) {
        return AssetPaths.texture(RESOURCE_NAMESPACE, "%s/%s/%s".formatted(RESOURCE_PATH, namespace, textureName));
    }

    private static String defaultModelPath(String namespace, String nodeName) {
        return AssetPaths.model(RESOURCE_NAMESPACE, "%s/%s/%s.json".formatted(RESOURCE_PATH, namespace, nodeName));
    }

    private static String modelPath(String namespace, String nodeName, String variantName) {
        return AssetPaths.model(RESOURCE_NAMESPACE, "%s/%s/%s/%s.json".formatted(RESOURCE_PATH, namespace, variantName, nodeName));
    }
}
