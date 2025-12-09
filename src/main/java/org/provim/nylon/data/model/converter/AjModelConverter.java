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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.provim.nylon.data.model.animated_java.*;
import org.provim.nylon.data.model.nylon.*;
import org.provim.nylon.data.model.nylon.animated_java.FrameWithEffects;
import org.provim.nylon.data.model.nylon.animated_java.TransformWithCommands;
import org.provim.nylon.util.NylonConstants;
import org.provim.nylon.util.commands.CommandParser;

import java.util.Locale;
import java.util.UUID;

public class AjModelConverter {
    private static final String BASE_MODEL_ID = "animated_java:blueprint/";

    public static NylonModel convert(AjModel ajModel) {
        var nodes = new ObjectArrayList<Node>();
        var animations = new Object2ObjectOpenHashMap<String, Animation>();
        var variants = new Reference2ObjectOpenHashMap<UUID, Variant>();

        for (AjNode node : ajModel.nodes().values()) {
            try {
                Node converted = convert(node, ajModel);
                nodes.add(converted);
            } catch (Exception ignored) {
            }
        }

        for (UUID uuid : ajModel.variants().keySet()) {
            AjVariant ajVariant = ajModel.variants().get(uuid);
            if (!ajVariant.isDefault()) {
                variants.put(uuid, convert(ajVariant, ajModel));
            }
        }

        for (AjAnimation animation : ajModel.animations().values()) {
            animations.put(animation.name(), convert(animation));
        }

        AjResourceGenerator.generate(ajModel);

        return new NylonModel(NylonConstants.DISPLAY_ITEM, nodes.toArray(new Node[0]), variants, animations);
    }

    private static Variant convert(AjVariant ajVariant, AjModel ajModel) {
        Object2ObjectOpenHashMap<UUID, Identifier> models = new Object2ObjectOpenHashMap<>();
        ajVariant.models().forEach((uuid, modelJson) -> {
            String namespace = ajModel.settings().exportNamespace();
            AjNode node = ajModel.nodes().get(uuid);
            models.put(uuid, modelId(namespace, node.name(), ajVariant.name()));
        });

        return new Variant(
                ajVariant.name(),
                models,
                ajVariant.excludedNodes()
        );
    }

    private static Node convert(AjNode ajNode, AjModel ajModel) {
        Node.NodeType type = Node.NodeType.valueOf(ajNode.type().toUpperCase(Locale.ENGLISH));
        Identifier modelId = defaultModelId(ajModel.settings().exportNamespace(), ajNode.name());
        return new Node(
                type,
                ajNode.name(),
                ajNode.uuid(),
                convert(ajNode.defaultTransform()),
                modelId
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
                Animation.LoopMode.valueOf(ajAnimation.loopMode().toUpperCase(Locale.ENGLISH)),
                ajAnimation.modifiedNodes()
        );
    }

    private static Frame convert(AjFrame ajFrame) {
        Reference2ObjectOpenHashMap<UUID, Transform> transforms = new Reference2ObjectOpenHashMap<>();
        for (UUID uuid : ajFrame.transforms().keySet()) {
            AjTransform transform = ajFrame.transforms().get(uuid);
            transforms.put(uuid, convert(transform));
        }

        return new FrameWithEffects(
                transforms,
                convert(ajFrame.variant())
        );
    }

    private static Transform convert(AjTransform ajTransform) {
        AjTransform.Decomposed decomposed = ajTransform.decomposed();
        Quaternionf leftRotation = decomposed.leftRotation().rotateY(Mth.DEG_TO_RAD * 180F);
        Vector3f translation = decomposed.translation();
        Vector3f scale = decomposed.scale();

        String commands = ajTransform.commands();
        if (commands == null || commands.isEmpty()) {
            return new Transform(
                    translation,
                    scale,
                    leftRotation
            );
        } else {
            String condition = ajTransform.executeCondition();
            return new TransformWithCommands(
                    translation,
                    scale,
                    leftRotation,
                    CommandParser.parse(commands),
                    CommandParser.parseCondition(condition)
            );
        }
    }

    @Nullable
    private static FrameWithEffects.Variant convert(@Nullable AjFrame.Variant ajVariant) {
        if (ajVariant == null) {
            return null;
        }

        String condition = ajVariant.executeCondition();
        return new FrameWithEffects.Variant(
                ajVariant.uuid(),
                CommandParser.parseCondition(condition)
        );
    }

    public static Identifier defaultModelId(String namespace, String nodeName) {
        return Identifier.parse(BASE_MODEL_ID + "%s/%s".formatted(namespace, nodeName));
    }

    public static Identifier modelId(String namespace, String nodeName, String variantName) {
        return Identifier.parse(BASE_MODEL_ID + "%s/%s/%s".formatted(namespace, variantName, nodeName));
    }
}
