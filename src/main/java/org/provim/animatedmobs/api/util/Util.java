package org.provim.animatedmobs.api.util;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.InteractionTrackedData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.provim.animatedmobs.api.entities.holders.elements.FastBlockDisplayElement;
import org.provim.animatedmobs.api.entities.holders.elements.FastItemDisplayElement;
import org.provim.animatedmobs.api.entities.holders.elements.FastTextDisplayElement;
import org.provim.animatedmobs.api.model.AjNode;
import org.provim.animatedmobs.api.model.AjPose;

import java.util.List;

public class Util {

    public static List<Packet<ClientGamePacketListener>> updateClientInteraction(InteractionElement interaction, Vector2f size) {
        // Updates the dimensions and bounding box of the interaction on the client. Note that the interactions dimensions and bounding box are two different things.
        // - The bounding box is primarily used for detecting player attacks, interactions and rendering the hitbox.
        // - The dimensions are used for certain other properties, such as the passenger riding height or the fire animation.
        return List.of(
                // We update the POSE in this packet, which makes the client refresh the interactions dimensions.
                // We use this to move the passenger riding height of the interaction upwards. This is raised to height * 1.325 to match the "actual" top of the interaction.
                new ClientboundSetEntityDataPacket(interaction.getEntityId(), List.of(
                        SynchedEntityData.DataValue.create(InteractionTrackedData.HEIGHT, size.y * 1.325F),
                        SynchedEntityData.DataValue.create(InteractionTrackedData.WIDTH, size.x),
                        SynchedEntityData.DataValue.create(EntityTrackedData.POSE, Pose.STANDING)
                )),
                // Afterward, we send another packet that only updates the bounding box height back to its original value, without updating its dimensions.
                // This lets us turn the bounding box back into the correct size whilst keeping the raised passenger riding height.
                new ClientboundSetEntityDataPacket(interaction.getEntityId(), List.of(
                        SynchedEntityData.DataValue.create(InteractionTrackedData.HEIGHT, size.y)
                ))
        );
    }

    @Nullable
    public static DisplayElement toDisplayElement(AjNode node, AjPose defaultPose) {
        if (node.entityType() == null) {
            return null;
        }

        return switch (node.entityType().getPath()) {
            case "item_display" -> new FastItemDisplayElement();
            case "block_display" -> new FastBlockDisplayElement();
            case "text_display" -> {
                TextDisplayElement element = new FastTextDisplayElement();
                element.setTransformation(defaultPose.matrix());
                element.setInvisible(true);
                element.setBackground(0);
                yield element;
            }
            default -> null;
        };
    }
}
