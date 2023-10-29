package org.provim.nylon.mixins.packets;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjHolderInterface;
import org.provim.nylon.util.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Redirect(
            method = {"crit", "magicCrit"},
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/entity/Entity;I)Lnet/minecraft/network/protocol/game/ClientboundAnimatePacket;"
            )
    )
    private ClientboundAnimatePacket nylon$modifyCritParticles(Entity entity, int action) {
        if (entity instanceof AjEntity ajEntity) {
            AjHolderInterface holder = ajEntity.getHolder();
            if (holder != null) {
                return Utils.createAnimatePacket(holder.getCritParticleId(), action);
            }
        }
        return new ClientboundAnimatePacket(entity, action);
    }
}
