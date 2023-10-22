package org.provim.nylon.mixins.packets;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundSetPassengersPacket.class)
public class ClientboundSetPassengersPacketMixin {
    @Mutable
    @Shadow
    @Final
    private int vehicle;
    @Mutable
    @Shadow
    @Final
    private int[] passengers;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
    private void nylon$modifyRidePacket(Entity entity, CallbackInfo ci) {
        if (entity instanceof AjEntity ajEntity) {
            AbstractAjHolder holder = ajEntity.getHolder();
            if (holder != null) {
                this.vehicle = holder.getVehicleId();

                int displayVehicle = holder.getDisplayVehicleId();
                if (this.vehicle == displayVehicle) {
                    IntList il = new IntArrayList();
                    il.addAll(IntList.of(this.passengers));
                    il.addAll(holder.getDisplayIds());
                    this.passengers = il.toIntArray();
                }
            }
        }
    }
}
