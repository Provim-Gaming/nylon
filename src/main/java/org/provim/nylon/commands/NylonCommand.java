package org.provim.nylon.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class NylonCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var builder = Commands.literal("nylon");
        builder.then(ModelCommand.register());
        dispatcher.register(builder);
    }
}
