package org.provim.nylon.holders.living;

import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2f;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.util.Utils;

import java.util.function.Consumer;

/**
 * Extension of {@link LivingAjHolder} that allows for custom passenger riding offsets.
 * <p>
 * This will use {@link Entity#getBbHeight} + {@link Entity#getMyRidingOffset} as the ride height.
 * <p>
 * It works by adding an extra zero bounding boxed {@link InteractionElement} to the parent entity,
 * which is used as the vehicle. This will add a very minor overhead on the client.
 */
public class LivingAjHolderWithRideOffset<T extends LivingEntity & AjEntity> extends LivingAjHolder<T> {
    private static final Vector2f ZERO = new Vector2f(0, 0);
    private final InteractionElement rideInteraction;

    public LivingAjHolderWithRideOffset(T parent, AjModel model) {
        super(parent, model);

        this.rideInteraction = new InteractionElement();
        this.rideInteraction.setInvisible(true);
        this.addElement(this.rideInteraction);
    }

    @Override
    protected void addDirectPassengers(IntList passengers) {
        super.addDirectPassengers(passengers);
        passengers.add(this.rideInteraction.getEntityId());
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        for (var packet : Utils.updateClientInteraction(this.rideInteraction, ZERO, Utils.getRideOffset(this.parent))) {
            consumer.accept(packet);
        }
    }

    @Override
    protected void sendScaleUpdate() {
        super.sendScaleUpdate();
        this.sendPacket(new ClientboundBundlePacket(Utils.updateClientInteraction(this.rideInteraction, ZERO, Utils.getRideOffset(this.parent))));
    }

    @Override
    public int getVehicleId() {
        return this.rideInteraction.getEntityId();
    }
}
