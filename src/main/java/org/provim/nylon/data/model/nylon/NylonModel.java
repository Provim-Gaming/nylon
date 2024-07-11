package org.provim.nylon.data.model.nylon;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class NylonModel {
    public final Item rigItem;
    public final Node[] nodes;
    public final Reference2ObjectOpenHashMap<UUID, Transform> defaultTransforms;
    public final Reference2ObjectOpenHashMap<UUID, Variant> variants;
    public final Object2ObjectOpenHashMap<String, Animation> animations;

    public NylonModel(
            Item rigItem,
            Node[] nodes,
            Reference2ObjectOpenHashMap<UUID, Transform> defaultTransforms,
            Reference2ObjectOpenHashMap<UUID, Variant> variants,
            Object2ObjectOpenHashMap<String, Animation> animations
    ) {
        Validate.notNull(rigItem, "Rig item cannot be null");
        Validate.notNull(nodes, "Nodes cannot be null");
        Validate.notNull(defaultTransforms, "Default transforms cannot be null");
        Validate.notNull(variants, "Variants cannot be null");
        Validate.notNull(animations, "Animations cannot be null");

        this.rigItem = rigItem;
        this.nodes = nodes;
        this.defaultTransforms = defaultTransforms;
        this.variants = variants;
        this.animations = animations;
    }
}
