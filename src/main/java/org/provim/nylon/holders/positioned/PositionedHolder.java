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

package org.provim.nylon.holders.positioned;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.provim.nylon.data.model.nylon.NylonModel;
import org.provim.nylon.holders.base.AbstractAjHolder;

public class PositionedHolder extends AbstractAjHolder {
    protected final Vec3 pos;

    public PositionedHolder(ServerLevel level, Vec3 pos, NylonModel model) {
        super(model, level);
        this.pos = pos;
    }

    @Override
    public CommandSourceStack createCommandSourceStack() {
        String name = String.format("PositionedHolder[%.1f, %.1f, %.1f]", this.pos.x, this.pos.y, this.pos.z);
        return new CommandSourceStack(
                this.getServer(),
                this.pos,
                Vec2.ZERO,
                this.level,
                LevelBasedPermissionSet.ALL,
                name,
                Component.literal(name),
                this.getServer(),
                null
        );
    }
}
