package org.provim.nylon.model;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.holders.base.AjElementHolder;
import org.provim.nylon.util.Utils;

import java.lang.reflect.Type;
import java.util.UUID;

public record AjFrame(
        float time,
        Reference2ObjectOpenHashMap<UUID, AjPose> poses,

        @Nullable Variant variant,
        @Nullable Command commands,
        @Nullable SoundEffect soundEffect,
        boolean requiresUpdates
) {

    public void run(AjElementHolder<?> holder) {
        Commands executor = holder.getServer().getCommands();
        CommandSourceStack source = holder.getParent().createCommandSourceStack().withPermission(2).withSuppressedOutput();

        if (this.soundEffect != null) {
            holder.getParent().playSound(this.soundEffect.event());
        }

        if (this.variant != null) {
            if (this.satisfiesConditions(this.variant.conditions(), executor, source)) {
                holder.getVariantController().setVariant(this.variant.uuid());
            }
        }

        if (this.commands != null && this.commands.commands().length > 0) {
            if (this.satisfiesConditions(this.commands.conditions(), executor, source)) {
                for (String command : this.commands.commands()) {
                    executor.performPrefixedCommand(source, command);
                }
            }
        }
    }

    private boolean satisfiesConditions(@Nullable String[] conditions, Commands executor, CommandSourceStack source) {
        if (conditions != null) {
            for (String condition : conditions) {
                if (executor.performPrefixedCommand(source, condition) <= 0) {
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

            Command command = null;
            if (object.has("commands")) {
                command = context.deserialize(object.get("commands"), Command.class);
            }

            SoundEffect soundEffect = null;
            if (object.has("sound")) {
                soundEffect = context.deserialize(object.get("sound"), SoundEffect.class);
            }

            boolean requiresUpdates = variant != null || soundEffect != null || (command != null && command.commands.length > 0);
            return new AjFrame(time, nodeMap, variant, command, soundEffect, requiresUpdates);
        }
    }

    public record SoundEffect(
            // FIXME: not implemented in animated-java's json exporter yet
            @SerializedName("id") SoundEvent event
    ) {
    }

    public record Variant(
            UUID uuid,
            @Nullable String[] conditions
    ) {
        public static class Deserializer implements JsonDeserializer<Variant> {
            @Override
            public Variant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = jsonElement.getAsJsonObject();
                UUID uuid = context.deserialize(object.get("uuid"), UUID.class);

                JsonElement conditionElement = object.get("executeCondition");
                String[] conditions = conditionElement != null ? Utils.parseCommands(conditionElement.getAsString(), "execute ") : null;

                return new Variant(uuid, conditions);
            }
        }
    }

    public record Command(
            String[] commands,
            @Nullable String[] conditions
    ) {
        public static class Deserializer implements JsonDeserializer<Command> {
            @Override
            public Command deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = jsonElement.getAsJsonObject();
                String[] commands = Utils.parseCommands(GsonHelper.getAsString(object, "commands"));

                JsonElement conditionElement = object.get("executeCondition");
                String[] conditions = conditionElement != null ? Utils.parseCommands(conditionElement.getAsString(), "execute ") : null;

                return new Command(commands, conditions);
            }
        }
    }
}