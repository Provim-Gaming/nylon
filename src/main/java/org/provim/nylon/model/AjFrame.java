package org.provim.nylon.model;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.CommandDispatcher;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.util.commands.CommandParser;
import org.provim.nylon.util.commands.ParsedCommand;

import java.lang.reflect.Type;
import java.util.UUID;

public record AjFrame(
        float time,
        Reference2ObjectOpenHashMap<UUID, AjPose> poses,

        @Nullable Variant variant,
        @Nullable Commands commands,
        @Nullable SoundEffect soundEffect,
        boolean requiresUpdates
) {

    public void run(AbstractAjHolder holder) {
        CommandDispatcher<CommandSourceStack> dispatcher = holder.getServer().getCommands().getDispatcher();
        CommandSourceStack source = holder.createCommandSourceStack().withPermission(2).withSuppressedOutput();

        if (this.soundEffect != null) {
            Entity entity = source.getEntity();
            if (entity != null) {
                entity.playSound(this.soundEffect.event);
            } else {
                holder.getLevel().playSound(
                        null, BlockPos.containing(source.getPosition()),
                        this.soundEffect.event, SoundSource.MASTER,
                        1.0F, 1.0F
                );
            }
        }

        if (this.variant != null) {
            if (satisfiesConditions(this.variant.conditions, dispatcher, source)) {
                holder.getVariantController().setVariant(this.variant.uuid);
            }
        }

        if (this.commands != null && this.commands.commands.length > 0) {
            if (satisfiesConditions(this.commands.conditions, dispatcher, source)) {
                for (ParsedCommand command : this.commands.commands) {
                    command.execute(dispatcher, source);
                }
            }
        }
    }

    private static boolean satisfiesConditions(ParsedCommand[] conditions, CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack source) {
        if (conditions != null) {
            for (ParsedCommand condition : conditions) {
                if (condition.execute(dispatcher, source) <= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static class Deserializer implements JsonDeserializer<AjFrame> {
        @Override
        public AjFrame deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();

            float time = context.deserialize(object.get("time"), float.class);
            AjPose[] nodes = context.deserialize(object.get("nodes"), AjPose[].class);

            Reference2ObjectOpenHashMap<UUID, AjPose> nodeMap = new Reference2ObjectOpenHashMap<>(nodes.length);
            for (AjPose pose : nodes) {
                nodeMap.put(pose.uuid(), pose);
            }

            Variant variant = null;
            if (object.has("variant")) {
                variant = context.deserialize(object.get("variant"), Variant.class);
            }

            Commands commands = null;
            if (object.has("commands")) {
                commands = context.deserialize(object.get("commands"), Commands.class);
            }

            SoundEffect soundEffect = null;
            if (object.has("sound")) {
                soundEffect = context.deserialize(object.get("sound"), SoundEffect.class);
            }

            boolean requiresUpdates = variant != null || soundEffect != null || (commands != null && commands.commands.length > 0);
            return new AjFrame(time, nodeMap, variant, commands, soundEffect, requiresUpdates);
        }
    }

    public record SoundEffect(
            // FIXME: not implemented in animated-java's json exporter yet
            @SerializedName("id") SoundEvent event
    ) {
    }

    public record Variant(
            UUID uuid,
            @Nullable ParsedCommand[] conditions
    ) {
        public static class Deserializer implements JsonDeserializer<Variant> {
            @Override
            public Variant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = jsonElement.getAsJsonObject();
                UUID uuid = context.deserialize(object.get("uuid"), UUID.class);

                JsonElement conditionElement = object.get("executeCondition");
                ParsedCommand[] conditions = conditionElement != null ? CommandParser.parse(conditionElement.getAsString(), "execute ") : null;

                return new Variant(uuid, conditions);
            }
        }
    }

    public record Commands(
            ParsedCommand[] commands,
            @Nullable ParsedCommand[] conditions
    ) {
        public static class Deserializer implements JsonDeserializer<Commands> {
            @Override
            public Commands deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = jsonElement.getAsJsonObject();
                ParsedCommand[] commands = CommandParser.parse(GsonHelper.getAsString(object, "commands"));

                JsonElement conditionElement = object.get("executeCondition");
                ParsedCommand[] conditions = conditionElement != null ? CommandParser.parse(conditionElement.getAsString(), "execute ") : null;

                return new Commands(commands, conditions);
            }
        }
    }
}