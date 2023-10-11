package org.provim.nylon.mixins.packets;

import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.entities.AjEntity;
import org.provim.nylon.entities.holders.base.AjHolderInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

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
            AjHolderInterface holder = ajEntity.getHolder();
            this.vehicle = holder.getVehicleId();

            int displayVehicle = holder.getDisplayVehicleId();
            if (this.vehicle == displayVehicle) {
                int[] displays = holder.getDisplayIds();
                int oldLength = this.passengers.length;
                this.passengers = Arrays.copyOf(this.passengers, oldLength + displays.length);
                System.arraycopy(displays, 0, this.passengers, oldLength, displays.length);
            }
        }
    }
}
