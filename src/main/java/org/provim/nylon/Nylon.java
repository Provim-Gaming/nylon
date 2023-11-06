package org.provim.nylon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.provim.nylon.commands.NylonCommand;

public class Nylon implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            NylonCommand.register(dispatcher);
        });
    }
}
