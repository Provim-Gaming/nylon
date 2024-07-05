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

package org.provim.nylon.data.model.animated_java;

/**
 * Represents an Animated Java JSON model exported from Blockbench.
 * <a href="https://github.com/Animated-Java/animated-java/blob/main/schemas/jsonExport.schema.json">JSON Structure</a>
 */
public record AjModel(
        AjBlueprintSettings blueprint_settings,
        AjResources resources,
        AjRig rig,
        AjAnimation[] animations
) {
}
