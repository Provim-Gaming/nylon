package org.provim.nylon.model;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.holders.base.AjElementHolder;

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
        MutableObject<CommandSourceStack> mutable = new MutableObject<>();
        Commands executor = holder.getServer().getCommands();

        if (this.variant != null) {
            if (this.satisfiesCondition(this.variant.condition(), executor, this.getOrCreateCommandSource(holder, mutable))) {
                holder.setCurrentVariant(this.variant.uuid());
            }
        }

        if (this.soundEffect != null) {
            if (this.satisfiesCondition(this.soundEffect.condition(), executor, this.getOrCreateCommandSource(holder, mutable))) {
                holder.getParent().playSound(this.soundEffect.event());
            }
        }

        if (this.commands != null && this.commands.commands().length > 0) {
            var source = this.getOrCreateCommandSource(holder, mutable);

            if (this.satisfiesCondition(this.commands.condition(), executor, source)) {
                for (String command : this.commands.commands()) {
                    if (!command.isEmpty()) {
                        executor.performPrefixedCommand(source, command);
                    }
                }
            }
        }
    }

    private boolean satisfiesCondition(@Nullable String condition, Commands executor, CommandSourceStack source) {
        if (condition == null || condition.isEmpty()) {
            return true;
        }
        return executor.performPrefixedCommand(source, "execute " + condition) > 0;
    }

    private CommandSourceStack getOrCreateCommandSource(AjElementHolder<?> holder, MutableObject<CommandSourceStack> mutable) {
        if (mutable.getValue() == null) {
            mutable.setValue(holder.getParent().createCommandSourceStack().withPermission(2).withSuppressedOutput());
        }
        return mutable.getValue();
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

            SoundEffect soundEffect = null; // FIXME: not implemented in animated-java yet
            if (object.has("sound")) {
                soundEffect = context.deserialize(object.get("sound"), SoundEffect.class);
            }

            boolean requiresUpdates = variant != null || soundEffect != null || (command != null && command.commands.length > 0);
            return new AjFrame(time, nodeMap, variant, command, soundEffect, requiresUpdates);
        }
    }

    public record Variant(
            @SerializedName("variant") UUID uuid,
            @SerializedName("execute_condition") @Nullable String condition
    ) {
    }

    public record SoundEffect(
            @SerializedName("id") SoundEvent event,
            @SerializedName("execute_condition") @Nullable String condition
    ) {
    }

    public record Command(
            String[] commands,
            @Nullable String condition
    ) {
        public static class Deserializer implements JsonDeserializer<Command> {
            @Override
            public Command deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = jsonElement.getAsJsonObject();

                JsonElement conditionElement = object.get("execute_condition");
                String condition = conditionElement != null ? conditionElement.getAsString() : null;

                String commandString = GsonHelper.getAsString(object, "commands");
                String[] commands = StringUtils.split(commandString.trim(), "\n");
                for (int i = 0; i < commands.length; i++) {
                    commands[i] = commands[i].trim();
                }

                return new Command(commands, condition);
            }
        }
    }
}