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

package org.provim.nylon.data.model.nylon.animated_java;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.provim.nylon.data.model.nylon.Transform;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.util.Utils;
import org.provim.nylon.util.commands.ParsedCommand;

public class TransformWithCommands extends Transform {
    private final ParsedCommand[] commands;
    private final ParsedCommand condition;

    public TransformWithCommands(Vector3f translation, Vector3f scale, Quaternionf leftRotation, ParsedCommand[] commands, ParsedCommand condition) {
        super(translation, scale, leftRotation);
        this.commands = commands;
        this.condition = condition;
    }

    @Override
    public void run(AbstractAjHolder holder) {
        if (this.commands == null || this.commands.length == 0) {
            return;
        }

        Vec3 offsetPos = holder.getTransformOffsetPos(this);
        holder.getServer().execute(() -> {
            CommandSourceStack source = holder.createCommandSourceStack()
                    .withPosition(offsetPos)
                    .withMaximumPermission(LevelBasedPermissionSet.GAMEMASTER)
                    .withSuppressedOutput();
            if (this.condition == null || Utils.satisfiesCondition(source, this.condition)) {
                for (ParsedCommand command : this.commands) {
                    command.execute(source.dispatcher(), source);
                }
            }
        });
    }
}
