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

package org.provim.nylon.util.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.Nylon;

public class ParsedCommand {
    private static final CommandSourceStack PARSING_SOURCE = new CommandSourceStack(
            CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, 2, "", CommonComponents.EMPTY, null, null
    );
    private final String command;
    @Nullable
    private ParseResults<CommandSourceStack> parsed;
    private boolean isInvalid;

    protected ParsedCommand(String command) {
        this.command = command;
    }

    public int execute(CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack source) {
        if (this.parsed == null) {
            this.parsed = dispatcher.parse(this.command, PARSING_SOURCE);

            if (this.parsed.getReader().canRead()) {
                Nylon.LOGGER.error("[Nylon] Unable to parse command: {}", this.command);
                this.isInvalid = true;
            }
        }

        if (!this.isInvalid) {
            try {
                return dispatcher.execute(Commands.mapSource(this.parsed, (s) -> source));
            } catch (Throwable ignored) {
            }
        }
        return 0;
    }
}
