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
