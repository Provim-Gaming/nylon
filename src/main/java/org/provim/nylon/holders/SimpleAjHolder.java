package org.provim.nylon.holders;

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.model.AjModel;

import java.util.function.Consumer;

public class SimpleAjHolder<T extends Entity & AjEntity> extends AbstractAjHolder<T> {
    public SimpleAjHolder(T parent, AjModel model, boolean updateElementsAsync) {
        super(parent, model, updateElementsAsync);
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);
        consumer.accept(VirtualEntityUtils.createRidePacket(this.getDisplayVehicleId(), this.getDisplayIds()));
    }

    @Override
    public void updateOnFire(boolean displayFire) {
    }
}
