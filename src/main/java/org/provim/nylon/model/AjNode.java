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

package org.provim.nylon.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomModelData;

import java.util.UUID;

public record AjNode(
        NodeType type,
        String name,
        UUID uuid,
        @SerializedName("custom_model_data") CustomModelData customModelData,
        @SerializedName("resource_location") ResourceLocation resourceLocation,
        @SerializedName("entity_type") ResourceLocation entityType
) {
    public enum NodeType {
        bone(true),
        locator(false);

        private final boolean hasModelData;

        NodeType(boolean hasModelData) {
            this.hasModelData = hasModelData;
        }

        public boolean hasModelData() {
            return this.hasModelData;
        }
    }
}