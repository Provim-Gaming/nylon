package org.provim.nylon.mixins.accessors;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundAnimatePacket.class)
public interface ClientboundAnimatePacketAccessor {
    @Mutable
    @Accessor("id")
    void setId(int id);

    @Mutable
    @Accessor("action")
    void setAction(int action);
}
