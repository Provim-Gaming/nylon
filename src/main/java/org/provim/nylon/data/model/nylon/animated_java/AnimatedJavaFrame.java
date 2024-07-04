package org.provim.nylon.data.model.nylon.animated_java;

import com.mojang.brigadier.CommandDispatcher;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.data.model.nylon.Frame;
import org.provim.nylon.data.model.nylon.Pose;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.util.commands.ParsedCommand;

import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class AnimatedJavaFrame extends Frame {
    private final @Nullable Variant variant;
    private final @Nullable Commands commands;
    private final boolean requiresUpdates;

    public AnimatedJavaFrame(Reference2ObjectOpenHashMap<UUID, Pose> poses, @Nullable Variant variant, @Nullable Commands commands) {
        super(poses);
        this.variant = variant;
        this.commands = commands;
        this.requiresUpdates = variant != null || (commands != null && commands.commands.length > 0);
    }

    @Override
    public boolean requiresUpdates() {
        return this.requiresUpdates;
    }

    @Override
    public void run(AbstractAjHolder holder) {
        CommandDispatcher<CommandSourceStack> dispatcher = holder.getServer().getCommands().getDispatcher();
        CommandSourceStack source = holder.createCommandSourceStack().withPermission(2).withSuppressedOutput();

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

    public static final class Variant {
        private final UUID uuid;
        @Nullable
        private final ParsedCommand[] conditions;

        public Variant(
                UUID uuid,
                @Nullable ParsedCommand[] conditions
        ) {
            Validate.notNull(uuid, "UUID cannot be null");

            this.uuid = uuid;
            this.conditions = conditions;
        }
    }

    public static final class Commands {
        private final ParsedCommand[] commands;
        @Nullable
        private final ParsedCommand[] conditions;

        public Commands(
                ParsedCommand[] commands,
                @Nullable ParsedCommand[] conditions
        ) {
            Validate.notNull(commands, "Commands cannot be null");

            this.commands = commands;
            this.conditions = conditions;
        }
    }
}