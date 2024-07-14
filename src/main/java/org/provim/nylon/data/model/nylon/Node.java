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

package org.provim.nylon.data.model.nylon;

import org.apache.commons.lang3.Validate;

import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class Node {
    public final NodeType type;
    public final String name;
    public final UUID uuid;
    public final int customModelData;

    public Node(
            NodeType type,
            String name,
            UUID uuid,
            int customModelData
    ) {
        Validate.notNull(type, "Type cannot be null");
        Validate.notNull(name, "Name cannot be null");
        Validate.notNull(uuid, "UUID cannot be null");

        this.type = type;
        this.name = name;
        this.uuid = uuid;
        this.customModelData = customModelData;
    }

    public enum NodeType {
        BONE,
        LOCATOR
    }
}