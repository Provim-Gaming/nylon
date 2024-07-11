package org.provim.nylon.data.model.converter;

import com.mojang.math.MatrixUtil;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.provim.nylon.data.model.animated_java.*;
import org.provim.nylon.data.model.nylon.*;
import org.provim.nylon.data.model.nylon.animated_java.AnimatedJavaFrame;
import org.provim.nylon.util.commands.CommandParser;

import java.util.Map;
import java.util.UUID;

public class AjModelConverter {

    public static NylonModel convert(AjModel ajModel) {
        var displayItem = ajModel.blueprintSettings().displayItem();
        var nodes = new Node[ajModel.rig().nodeMap().size()];
        var animations = new Object2ObjectOpenHashMap<String, Animation>();
        var variants = new Reference2ObjectOpenHashMap<UUID, Variant>();
        var defaultTransforms = new Reference2ObjectOpenHashMap<UUID, Transform>();

        int index = 0;
        for (AjNode node : ajModel.rig().nodeMap().values()) {
            nodes[index++] = convert(node, displayItem);
        }

        ajModel.rig().variants().forEach((uuid, ajVariant) -> {
            var variantModels = ajModel.resources().variantModels().get(uuid);
            if (variantModels != null) {
                variants.put(uuid, convert(ajVariant, displayItem, variantModels));
            }
        });

        for (AjAnimation animation : ajModel.animations()) {
            animations.put(animation.name(), convert(animation));
        }

        for (AjTransform transform : ajModel.rig().defaultTransforms()) {
            defaultTransforms.put(transform.uuid(), convert(transform));
        }

        AjResourceGenerator.generate(ajModel);

        return new NylonModel(displayItem, nodes, defaultTransforms, variants, animations);
    }

    private static Variant convert(AjVariant ajVariant, Item displayItem, Map<UUID, AjResources.VariantModel> variantModels) {
        Object2ObjectOpenHashMap<UUID, Variant.Model> models = new Object2ObjectOpenHashMap<>();
        variantModels.forEach((uuid, model) -> models.put(uuid, new Variant.Model(
                PolymerResourcePackUtils.requestModel(displayItem, model.resourceLocation()).value(),
                model.resourceLocation()
        )));

        return new Variant(
                ajVariant.name(),
                models,
                ajVariant.excludedNodes()
        );
    }

    private static Node convert(AjNode ajNode, Item displayItem) {
        Node.NodeType type = Node.NodeType.valueOf(ajNode.type().name().toUpperCase());
        return new Node(
                type,
                ajNode.name(),
                ajNode.uuid(),
                type == Node.NodeType.BONE ? PolymerResourcePackUtils.requestModel(displayItem, ajNode.resourceLocation()).value() : 0
        );
    }

    private static Animation convert(AjAnimation ajAnimation) {
        Frame[] frames = new Frame[ajAnimation.frames().length];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = convert(ajAnimation.frames()[i]);
        }

        return new Animation(
                frames,
                ajAnimation.loopDelay(),
                Animation.LoopMode.valueOf(ajAnimation.loopMode().name().toUpperCase()),
                ajAnimation.includedNodes()
        );
    }

    private static Frame convert(AjFrame ajFrame) {
        Reference2ObjectOpenHashMap<UUID, Transform> transforms = new Reference2ObjectOpenHashMap<>();
        for (AjTransform transform : ajFrame.transforms()) {
            transforms.put(transform.uuid(), convert(transform));
        }

        return new AnimatedJavaFrame(
                transforms,
                convert(ajFrame.variant()),
                convert(ajFrame.commands())
        );
    }

    private static Transform convert(AjTransform ajTransform) {
        Matrix4f matrix4f = ajTransform.matrix();
        Matrix3f matrix3f = new Matrix3f(matrix4f);
        Vector3f translation = matrix4f.getTranslation(new Vector3f());

        float multiplier = 1.0F / matrix4f.m33();
        if (multiplier != 1.0F) {
            matrix3f.scale(multiplier);
            translation.mul(multiplier);
        }

        var triple = MatrixUtil.svdDecompose(matrix3f);
        Vector3f scale = triple.getMiddle();
        Quaternionf leftRotation = triple.getLeft().rotateY(Mth.DEG_TO_RAD * 180F);
        Quaternionf rightRotation = triple.getRight();

        return new Transform(
                translation,
                scale,
                leftRotation,
                rightRotation
        );
    }

    @Nullable
    private static AnimatedJavaFrame.Commands convert(@Nullable AjFrame.Commands ajCommands) {
        if (ajCommands == null) {
            return null;
        }

        String condition = ajCommands.executeCondition();
        return new AnimatedJavaFrame.Commands(
                CommandParser.parse(ajCommands.commands()),
                condition != null ? CommandParser.parse(condition, "execute ") : null
        );
    }

    @Nullable
    private static AnimatedJavaFrame.Variant convert(@Nullable AjFrame.Variant ajVariant) {
        if (ajVariant == null) {
            return null;
        }

        String condition = ajVariant.executeCondition();
        return new AnimatedJavaFrame.Variant(
                ajVariant.uuid(),
                condition != null ? CommandParser.parse(condition, "execute ") : null
        );
    }
}
