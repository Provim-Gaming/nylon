package org.provim.nylon.mixins.tracking;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.holders.base.AbstractAjHolder;
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
    private Entity entity;

    @Inject(
            method = "set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V",
                    shift = At.Shift.AFTER
            )
    )
    private <T> void nylon$onSetEntityData(EntityDataAccessor<T> key, T value, boolean force, CallbackInfo ci) {
        if (this.entity instanceof AjEntity ajEntity) {
            AbstractAjHolder holder = ajEntity.getHolder();
            if (holder != null) {
                holder.onSyncedDataUpdated(key, value);
            }
        }
    }
}
