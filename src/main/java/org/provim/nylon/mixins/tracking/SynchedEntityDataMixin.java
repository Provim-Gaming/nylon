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

package org.provim.nylon.mixins.tracking;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjEntityHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SynchedEntityData.class)
public class SynchedEntityDataMixin {
    @Shadow
    @Final
    private SyncedDataHolder entity;

    @Inject(
            method = "set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/syncher/SyncedDataHolder;onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V",
                    shift = At.Shift.AFTER
            )
    )
    private <T> void nylon$onSetEntityData(EntityDataAccessor<T> key, T value, boolean force, CallbackInfo ci) {
        AjEntityHolder holder = AjEntity.getHolder(this.entity);
        if (holder != null) {
            holder.onSyncedDataUpdated(key, value);
        }
    }
}
