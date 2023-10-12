package org.provim.nylon.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.provim.nylon.entities.holders.base.AjHolderInterface;

import java.util.List;

public interface AjEntity extends PolymerEntity {
    AjHolderInterface getHolder();

    @Override
    default EntityType<?> getPolymerEntityType(ServerPlayer player) {
        return EntityType.BLOCK_DISPLAY;
    }

    @Override
    default void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        Entity parent = this.getHolder().getParent();

        // Adds entity shadows
        data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.SHADOW_RADIUS, parent.getBbWidth() * 0.65f));
        data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.WIDTH, parent.getBbWidth()));
        data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.HEIGHT, parent.getBbHeight()));

        // Adds movement and rotation interpolation
        data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TELEPORTATION_DURATION, 3));

        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.SILENT, true));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.NO_GRAVITY, true));
    }
}
