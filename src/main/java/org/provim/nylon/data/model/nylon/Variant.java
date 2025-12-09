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


import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class Variant {
    public final String name;
    public final Object2ObjectOpenHashMap<UUID, Identifier> models;
    public final ReferenceOpenHashSet<UUID> excludedNodes;

    public Variant(
            String name,
            Object2ObjectOpenHashMap<UUID, Identifier> models,
            ReferenceOpenHashSet<UUID> excludedNodes
    ) {
        Validate.notNull(name, "Name cannot be null");
        Validate.notNull(models, "Models cannot be null");
        Validate.notNull(excludedNodes, "Excluded nodes cannot be null");

        this.name = name;
        this.models = models;
        this.excludedNodes = excludedNodes;
    }

    public boolean isAffected(UUID boneUuid) {
        return !this.excludedNodes.contains(boneUuid);
    }
}

