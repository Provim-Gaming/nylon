package org.provim.animatedmobs.api.mixins;

import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import org.provim.animatedmobs.api.entities.AjEntity;
import org.provim.animatedmobs.api.entities.holders.AjHolderInterface;
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
    private void am_modifyRidePacket(Entity entity, CallbackInfo ci) {
        if (entity instanceof AjEntity ajEntity) {
            AjHolderInterface holder = ajEntity.getHolder();
            int[] displays = holder.getDisplayIds();
            int oldLength = this.passengers.length;

            this.vehicle = holder.getVehicleId();
            this.passengers = Arrays.copyOf(this.passengers, oldLength + displays.length);
            System.arraycopy(displays, 0, this.passengers, oldLength, displays.length);
        }
    }
}
