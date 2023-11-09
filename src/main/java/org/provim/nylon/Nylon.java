package org.provim.nylon;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.provim.nylon.commands.NylonCommand;
import org.slf4j.Logger;

public class Nylon implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            NylonCommand.register(dispatcher);
        });
    }
}
