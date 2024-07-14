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
import org.provim.nylon.util.ModelResources;

import java.util.Base64;
import java.util.UUID;

public class AjResourceGenerator {

    public static void generate(AjModel ajModel) {
        var models = ajModel.resources().models();
        for (UUID nodeUuid : models.keySet()) {
            var modelJson = models.get(nodeUuid);
            var node = ajModel.rig().nodeMap().get(nodeUuid);
            ModelResources.addResource("%s/%s.json".formatted(ajModel.resources().modelExportFolder(), node.name()), modelJson);
        }

        var variants = ajModel.resources().variantModels();
        for (UUID variantUuid : variants.keySet()) {
            var variantModels = variants.get(variantUuid);
            for (UUID nodeUuid : variantModels.keySet()) {
                var variantModel = variantModels.get(nodeUuid);
                ModelResources.addResource(variantModel.modelPath(), variantModel.model());
            }
        }

        var textures = ajModel.resources().textures();
        for (String textureName : textures.keySet()) {
            var texture = textures.get(textureName);
            ModelResources.addResource(
                    texture.expectedPath(),
                    Base64.getDecoder().decode(texture.src().substring("data:image/png;base64,".length()))
            );
        }
    }
}
