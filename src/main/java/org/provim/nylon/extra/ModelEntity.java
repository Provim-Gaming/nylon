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

package org.provim.nylon.extra;

import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.tracker.InteractionTrackedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjEntityHolder;
import org.provim.nylon.holders.entity.EntityHolder;
import org.provim.nylon.holders.entity.simple.SimpleEntityHolder;
import org.provim.nylon.holders.wrappers.DisplayWrapper;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjPose;

import java.util.List;

public class ModelEntity extends Interaction implements AjEntity {
    private final EntityHolder<?> holder;

    public ModelEntity(Level level, AjModel model) {
        super(EntityType.INTERACTION, level);
        this.holder = new SimpleEntityHolder<>(this, model) {
            @Override
            public void updateElement(DisplayWrapper<?> display, @Nullable AjPose pose) {
                display.element().setYaw(this.parent.getYRot());
                display.element().setPitch(this.parent.getXRot());
                if (pose == null) {
                    this.applyPose(display.getLastPose(), display);
                } else {
                    this.applyPose(pose, display);
                }
            }

            @Override
            protected void updateCullingBox() {
                // Do nothing, we want to prevent culling.
            }
        };

        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public AjEntityHolder getHolder() {
        return this.holder;
    }

    @Override
    public boolean saveAsPassenger(CompoundTag compoundTag) {
        // Don't save this entity.
        return false;
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayer player) {
        return EntityType.INTERACTION;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        data.add(SynchedEntityData.DataValue.create(InteractionTrackedData.HEIGHT, 2f));
        data.add(SynchedEntityData.DataValue.create(InteractionTrackedData.WIDTH, 2f));
    }
}
