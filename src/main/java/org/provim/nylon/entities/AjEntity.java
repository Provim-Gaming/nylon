package org.provim.nylon.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.mixin.SlimeEntityAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import org.provim.nylon.entities.holders.AjHolderInterface;

import java.util.List;

public interface AjEntity extends PolymerEntity {
    AjHolderInterface getHolder();

    @Override
    default EntityType<?> getPolymerEntityType(ServerPlayer player) {
        return EntityType.SLIME;
    }

    @Override
    default void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        data.add(SynchedEntityData.DataValue.create(SlimeEntityAccessor.getSLIME_SIZE(), 0));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.SILENT, true));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.NO_GRAVITY, true));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, (byte) ((1 << EntityTrackedData.INVISIBLE_FLAG_INDEX))));
    }
}
