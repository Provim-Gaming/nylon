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

import org.provim.nylon.data.model.animated_java.AjModel;
import org.provim.nylon.data.model.animated_java.AjNode;
import org.provim.nylon.data.model.animated_java.AjTexture;
import org.provim.nylon.data.model.animated_java.AjVariant;
import org.provim.nylon.util.ModelResources;

import java.util.Base64;
import java.util.UUID;

public class AjResourceGenerator {
    private static final String BASE_PATH = "assets/animated_java/";
    private static final String BASE_MODEL_PATH = BASE_PATH + "models/item/";
    private static final String BASE_TEXTURE_PATH = BASE_PATH + "textures/item/";

    public static void generate(AjModel ajModel) {
        String namespace = ajModel.settings().exportNamespace();

        for (AjVariant variant : ajModel.variants().values()) {
            for (UUID nodeUuid : variant.models().keySet()) {
                AjVariant.ModelData data = variant.models().get(nodeUuid);
                AjNode node = ajModel.nodes().get(nodeUuid);
                ModelResources.addResource(
                        variant.isDefault() ? modelPath(namespace, node.name()) : modelPath(namespace, node.name(), variant.name()),
                        data.model()
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
        return BASE_TEXTURE_PATH + "%s/%s".formatted(namespace, textureName);
    }

    private static String modelPath(String namespace, String nodeName) {
        return BASE_MODEL_PATH + "%s/%s.json".formatted(namespace, nodeName);
    }

    private static String modelPath(String namespace, String nodeName, String variantName) {
        return BASE_MODEL_PATH + "%s/%s/%s.json".formatted(namespace, variantName, nodeName);
    }
}
