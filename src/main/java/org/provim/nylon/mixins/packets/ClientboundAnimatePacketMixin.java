package org.provim.nylon.mixins.packets;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjHolderInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundAnimatePacket.class)
public class ClientboundAnimatePacketMixin {
    @Mutable
    @Shadow
    @Final
    private int id;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;I)V", at = @At("RETURN"))
    private void nylon$modifyAnimatePacket(Entity entity, int action, CallbackInfo ci) {
        if (action != 4 && action != 5) {
            return;
        }

        AjHolderInterface holder = AjEntity.getHolder(entity);
        if (holder != null) {
            this.id = holder.getCritParticleId();
        }
    }
}
