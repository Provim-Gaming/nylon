package org.provim.nylon.data.model.nylon;


import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class Variant {
    public final String name;
    public final Object2ObjectOpenHashMap<UUID, Model> models;
    public final ReferenceOpenHashSet<UUID> excludedNodes;

    public Variant(
            String name,
            Object2ObjectOpenHashMap<UUID, Model> models,
            ReferenceOpenHashSet<UUID> excludedNodes
    ) {
        Validate.notNull(name, "Name cannot be null");
        Validate.notNull(models, "Models cannot be null");
        Validate.notNull(excludedNodes, "Excluded nodes cannot be null");

        this.name = name;
        this.models = models;
        this.excludedNodes = excludedNodes;
    }

    public boolean isAffected(UUID boneUuid) {
        return !this.excludedNodes.contains(boneUuid);
    }

    public static final class Model {
        public final int customModelData;
        public final ResourceLocation resourceLocation;

        public Model(
                int customModelData,
                ResourceLocation resourceLocation
        ) {
            this.customModelData = customModelData;
            this.resourceLocation = resourceLocation;
        }
    }
}

