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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

public final class CommandParser {
    private static final ParsedCommand[] EMPTY_COMMAND_ARRAY = new ParsedCommand[0];

    public static ParsedCommand[] parse(String commandString) {
        return parse(commandString, null);
    }

    public static ParsedCommand[] parse(String commandString, @Nullable String prefix) {
        String[] commands = commandString.trim().split("(\r\n|\r|\n)", -1);

        ObjectArrayList<ParsedCommand> list = new ObjectArrayList<>(commands.length);
        for (String command : commands) {
            String sanitized = sanitizeCommand(command, prefix);
            if (sanitized != null) {
                list.add(new ParsedCommand(sanitized));
            }
        }

        return list.toArray(EMPTY_COMMAND_ARRAY);
    }

    @Nullable
    private static String sanitizeCommand(String command, @Nullable String prefix) {
        command = command.trim();
        if (command.isEmpty()) {
            return null;
        }

        if (command.charAt(0) == '/') {
            command = command.substring(1);
            if (command.isEmpty()) {
                return null;
            }
        }

        if (prefix != null) {
            command = prefix + command;
        }

        return command;
    }
}
