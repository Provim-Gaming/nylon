package org.provim.nylon.mixins.packets;

import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjEntityHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundSetEntityLinkPacket.class)
public class ClientboundSetEntityLinkPacketMixin {
    @Mutable
    @Shadow
    @Final
    private int sourceId;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
    private void nylon$modifyLeashPacket(Entity leashed, @Nullable Entity leashHolder, CallbackInfo ci) {
        AjEntityHolder holder = AjEntity.getHolder(leashed);
        if (holder != null) {
            this.sourceId = holder.getLeashedId();
        }
    }
}
