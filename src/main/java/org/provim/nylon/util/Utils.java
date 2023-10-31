package org.provim.nylon.util;

import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.InteractionTrackedData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Utils {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static float getRideOffset(Entity entity) {
        return entity.getBbHeight() + entity.getMyRidingOffset(entity);
    }

    public static int toSlimeSize(float size) {
        return Mth.floor(size / 2.04F / 0.255F);
    }

    public static boolean getSharedFlag(byte value, int flag) {
        return (value & 1 << flag) != 0;
    }

    public static List<Packet<ClientGamePacketListener>> updateClientInteraction(InteractionElement interaction, EntityDimensions dimensions) {
        return updateClientInteraction(interaction, dimensions, dimensions.height);
    }

    public static List<Packet<ClientGamePacketListener>> updateClientInteraction(InteractionElement interaction, EntityDimensions dimensions, float height) {
        // Updates the dimensions and bounding box of the interaction on the client. Note that the interactions dimensions and bounding box are two different things.
        // - The bounding box is primarily used for detecting player attacks, interactions and rendering the hitbox.
        // - The dimensions are used for certain other properties, such as the passenger riding height or the fire animation.
        return List.of(
                // We update the POSE in this packet, which makes the client refresh the interactions dimensions.
                // We use this to move the passenger riding height of the interaction upwards.
                new ClientboundSetEntityDataPacket(interaction.getEntityId(), List.of(
                        SynchedEntityData.DataValue.create(InteractionTrackedData.HEIGHT, height),
                        SynchedEntityData.DataValue.create(InteractionTrackedData.WIDTH, dimensions.width),
                        SynchedEntityData.DataValue.create(EntityTrackedData.POSE, Pose.STANDING)
                )),
                // Afterward, we send another packet that only updates the bounding box height back to its original value, without updating its dimensions.
                // This lets us turn the bounding box back into the correct size whilst keeping the raised passenger riding height.
                new ClientboundSetEntityDataPacket(interaction.getEntityId(), List.of(
                        SynchedEntityData.DataValue.create(InteractionTrackedData.HEIGHT, dimensions.height)
                ))
        );
    }

    public static String[] parseCommands(String commandString) {
        return parseCommands(commandString, null);
    }

    public static String[] parseCommands(String commandString, @Nullable String prefix) {
        String[] commands = StringUtils.split(commandString.trim(), "\n");

        ObjectArrayList<String> list = new ObjectArrayList<>(commands.length);
        for (String command : commands) {
            String trimmed = command.trim();
            if (!trimmed.isEmpty()) {
                list.add(prefix != null ? prefix + command : command);
            }
        }

        return list.toArray(EMPTY_STRING_ARRAY);
    }
}
