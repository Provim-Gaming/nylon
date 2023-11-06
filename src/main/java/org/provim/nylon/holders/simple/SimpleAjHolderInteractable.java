package org.provim.nylon.holders.simple;

import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.wrappers.DisplayWrapper;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjPose;
import org.provim.nylon.util.Utils;

import java.util.function.Consumer;

public class SimpleAjHolderInteractable<T extends Entity & AjEntity> extends AbstractAjHolder<T> {
    private final InteractionElement hitboxInteraction;

    public SimpleAjHolderInteractable(T parent, AjModel model) {
        super(parent, model);

        this.hitboxInteraction = InteractionElement.redirect(parent);
        this.addElement(this.hitboxInteraction);
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        for (var packet : Utils.updateClientInteraction(this.hitboxInteraction, this.dimensions)) {
            consumer.accept(packet);
        }

        consumer.accept(new ClientboundSetPassengersPacket(this.parent));
    }

    @Override
    protected void updateElement(DisplayWrapper<?> display) {
        AjPose pose = this.animation.findPose(display);
        if (pose == null) {
            this.applyPose(display.getLastPose(), display);
        } else {
            this.applyPose(pose, display);
        }
    }

    @Override
    public void applyPose(AjPose pose, DisplayWrapper<?> display) {
        display.element().setYaw(this.parent.getYRot());
        display.element().setPitch(this.parent.getXRot());

        display.setScale(pose.readOnlyScale());
        display.setTranslation(pose.translation().sub(0, this.dimensions.height - 0.01f, 0));

        display.startInterpolation();
    }

    @Override
    protected void addDirectPassengers(IntList passengers) {
        super.addDirectPassengers(passengers);
        passengers.add(this.hitboxInteraction.getEntityId());
    }

    @Override
    public int getDisplayVehicleId() {
        return this.hitboxInteraction.getEntityId();
    }

    @Override
    public int getVehicleId() {
        return this.hitboxInteraction.getEntityId();
    }

    @Override
    public int getCritParticleId() {
        return this.hitboxInteraction.getEntityId();
    }
}
