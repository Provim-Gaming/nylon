package org.provim.nylon.api;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.mixin.SlimeEntityAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.provim.nylon.NylonConfig;
import org.provim.nylon.holders.base.AbstractAjHolder;

import java.util.List;

public interface AjEntity extends PolymerEntity {
    AbstractAjHolder getHolder();

    default float getShadowRadius() {
        if (this instanceof Entity entity) {
            return entity.getBbWidth() * 0.65f;
        }
        return 0;
    }

    default int getTeleportDuration() {
        return 4;
    }

    @Override
    default EntityType<?> getPolymerEntityType(ServerPlayer player) {
        return NylonConfig.SLIME_BASED_ENTITY ? EntityType.SLIME : EntityType.BLOCK_DISPLAY;
    }

    @Override
    default void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        if (NylonConfig.SLIME_BASED_ENTITY) {
            data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, (byte) (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
            data.add(SynchedEntityData.DataValue.create(SlimeEntityAccessor.getSLIME_SIZE(), 0));
        } else {
            if (this instanceof Entity entity) {
                data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.WIDTH, entity.getBbWidth()));
                data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.HEIGHT, entity.getBbHeight()));
            }

            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.SHADOW_RADIUS, this.getShadowRadius()));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TELEPORTATION_DURATION, Math.max(0, this.getTeleportDuration())));
        }

        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.SILENT, true));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.NO_GRAVITY, true));
    }
}
