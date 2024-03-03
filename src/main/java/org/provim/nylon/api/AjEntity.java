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

package org.provim.nylon.api;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface AjEntity extends PolymerEntity {
    AjEntityHolder getHolder();

    default float getShadowRadius() {
        if (this instanceof Entity entity) {
            return entity.getBbWidth() * 0.6f;
        }
        return 0;
    }

    default int getTeleportDuration() {
        return 4;
    }

    @Override
    default EntityType<?> getPolymerEntityType(ServerPlayer player) {
        return EntityType.BLOCK_DISPLAY;
    }

    @Override
    default void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        if (this instanceof Entity entity) {
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.WIDTH, entity.getBbWidth()));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.HEIGHT, entity.getBbHeight()));
        }

        data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.SHADOW_RADIUS, this.getShadowRadius()));
        data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TELEPORTATION_DURATION, Math.max(0, this.getTeleportDuration())));

        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.SILENT, true));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.NO_GRAVITY, true));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.NAME_VISIBLE, false));
    }

    @Override
    default void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        if (packet instanceof ClientboundUpdateAttributesPacket) {
            return;
        }

        PolymerEntity.super.onEntityPacketSent(consumer, packet);
    }

    @Nullable
    static AjEntityHolder getHolder(Object obj) {
        return obj instanceof AjEntity ajEntity ? ajEntity.getHolder() : null;
    }
}
