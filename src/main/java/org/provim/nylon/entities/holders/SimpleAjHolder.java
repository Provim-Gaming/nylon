package org.provim.nylon.entities.holders;

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.entities.holders.base.AbstractAjHolder;
import org.provim.nylon.model.AjModel;

import java.util.function.Consumer;

public class SimpleAjHolder extends AbstractAjHolder<Entity> {
    public SimpleAjHolder(Entity parent, AjModel model) {
        this(parent, model, false);
    }

    public SimpleAjHolder(Entity parent, AjModel model, boolean updateElementsAsync) {
        super(parent, model, updateElementsAsync);
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        consumer.accept(new ClientboundUpdateMobEffectPacket(this.parent.getId(), new MobEffectInstance(MobEffects.WATER_BREATHING, -1, 0, false, false)));
        consumer.accept(VirtualEntityUtils.createRidePacket(this.getDisplayVehicleId(), this.getDisplayIds()));
    }
}
