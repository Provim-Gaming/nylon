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
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class NylonModel {
    public final Item rigItem;
    public final Node[] nodes;
    public final Reference2ObjectOpenHashMap<UUID, Variant> variants;
    public final Object2ObjectOpenHashMap<String, Animation> animations;

    public NylonModel(
            Item rigItem,
            Node[] nodes,
            Reference2ObjectOpenHashMap<UUID, Variant> variants,
            Object2ObjectOpenHashMap<String, Animation> animations
    ) {
        Validate.notNull(rigItem, "Rig item cannot be null");
        Validate.notNull(nodes, "Nodes cannot be null");
        Validate.notNull(variants, "Variants cannot be null");
        Validate.notNull(animations, "Animations cannot be null");

        this.rigItem = rigItem;
        this.nodes = nodes;
        this.variants = variants;
        this.animations = animations;
    }
}
