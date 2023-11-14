package org.provim.nylon.mixins.packets;

import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjEntityHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundEntityEventPacket.class)
public class ClientboundEntityEventPacketMixin {
    @Mutable
    @Shadow
    @Final
    private int entityId;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;B)V", at = @At("RETURN"))
    private void nylon$modifyEventPacket(Entity entity, byte b, CallbackInfo ci) {
        AjEntityHolder holder = AjEntity.getHolder(entity);
        if (holder != null) {
            this.entityId = holder.getEntityEventId();
        }
    }
}
