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

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.data.model.nylon.Frame;
import org.provim.nylon.data.model.nylon.Transform;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.util.Utils;
import org.provim.nylon.util.commands.ParsedCommand;

import java.util.UUID;

public class FrameWithEffects extends Frame {
    private final @Nullable Variant variant;

    public FrameWithEffects(Reference2ObjectOpenHashMap<UUID, Transform> transforms, @Nullable Variant variant) {
        super(transforms);
        this.variant = variant;
    }

    @Override
    public void run(AbstractAjHolder holder) {
        if (this.variant == null) {
            return;
        }

        if (this.variant.condition == null || Utils.satisfiesCondition(holder.createCommandSourceStack()
                .withMaximumPermission(LevelBasedPermissionSet.GAMEMASTER)
                .withSuppressedOutput(), this.variant.condition)
        ) {
            holder.getVariantController().setVariant(this.variant.uuid);
        }
    }

    public static final class Variant {
        private final UUID uuid;
        @Nullable
        private final ParsedCommand condition;

        public Variant(
                UUID uuid,
                @Nullable ParsedCommand condition
        ) {
            Validate.notNull(uuid, "UUID cannot be null");

            this.uuid = uuid;
            this.condition = condition;
        }
    }
}