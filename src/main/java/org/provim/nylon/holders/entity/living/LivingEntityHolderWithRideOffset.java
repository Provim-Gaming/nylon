/*
 * Nylon
 * Copyright (C) 2023, 2024 Provim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.provim.nylon.holders.entity.living;

import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.data.model.nylon.NylonModel;
import org.provim.nylon.util.Utils;

import java.util.function.Consumer;

/**
 * Extension of {@link LivingEntityHolder} that allows for custom passenger riding offsets.
 * <p>
 * It works by adding an extra zero bounding boxed {@link InteractionElement} to the parent entity,
 * which is used as the vehicle. This will add a very minor overhead on the client.
 */
public class LivingEntityHolderWithRideOffset<T extends LivingEntity & AjEntity> extends LivingEntityHolder<T> {
    private static final EntityDimensions ZERO = EntityDimensions.fixed(0, 0);
    protected final InteractionElement rideInteraction;

    public LivingEntityHolderWithRideOffset(T parent, NylonModel model) {
        super(parent, model);

        this.rideInteraction = new InteractionElement();
        this.rideInteraction.setSendPositionUpdates(false);
        this.rideInteraction.setInvisible(true);
        this.addElement(this.rideInteraction);
    }

    protected float getRideOffset() {
        Vec3 offset = this.dimensions.attachments().getNullable(EntityAttachment.PASSENGER, 0, this.parent.getYRot());
        return offset == null ? 0F : (float) offset.y();
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        for (var packet : Utils.updateClientInteraction(this.rideInteraction, ZERO, this.getRideOffset())) {
            // noinspection unchecked
            consumer.accept((Packet<ClientGamePacketListener>) packet);
        }
    }

    @Override
    protected void addDirectPassengers(IntList passengers) {
        super.addDirectPassengers(passengers);
        passengers.add(this.rideInteraction.getEntityId());
    }

    @Override
    public void onDimensionsUpdated(EntityDimensions dimensions) {
        super.onDimensionsUpdated(dimensions);
        this.sendPacket(new ClientboundBundlePacket(Utils.updateClientInteraction(this.rideInteraction, ZERO, this.getRideOffset())));
    }

    @Override
    public int getVehicleId() {
        return this.rideInteraction.getEntityId();
    }
}
