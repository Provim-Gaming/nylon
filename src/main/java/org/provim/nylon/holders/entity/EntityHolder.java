package org.provim.nylon.holders.entity;

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjEntityHolder;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.wrappers.Bone;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.util.Utils;

import java.util.function.Consumer;

public abstract class EntityHolder<T extends Entity & AjEntity> extends AbstractAjHolder implements AjEntityHolder {
    protected final T parent;
    protected final ObjectOpenHashSet<DisplayElement> additionalDisplays;
    protected EntityDimensions dimensions;
    protected int tickCount;

    protected EntityHolder(T parent, AjModel model) {
        super(model, (ServerLevel) parent.level());
        this.parent = parent;
        this.tickCount = parent.tickCount - 1;
        this.dimensions = parent.getType().getDimensions();
        this.additionalDisplays = new ObjectOpenHashSet<>();
    }

    @Override
    @Nullable
    protected ItemDisplayElement createBone(AjNode node, Item rigItem) {
        ItemDisplayElement element = super.createBone(node, rigItem);
        if (element != null) {
            element.getDataTracker().set(DisplayTrackedData.TELEPORTATION_DURATION, this.parent.getTeleportDuration());
        }
        return element;
    }

    @Override
    public boolean addAdditionalDisplay(DisplayElement element) {
        if (this.additionalDisplays.add(element)) {
            this.addElement(element);
            this.sendPacket(new ClientboundSetPassengersPacket(this.parent));
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAdditionalDisplay(DisplayElement element) {
        if (this.additionalDisplays.remove(element)) {
            this.removeElement(element);
            return true;
        }
        return false;
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);
        this.sendDirectPassengers(consumer);
    }

    public void sendDirectPassengers(Consumer<Packet<ClientGamePacketListener>> consumer) {
        IntList passengers = new IntArrayList();
        this.addDirectPassengers(passengers);

        if (passengers.size() > 0) {
            consumer.accept(VirtualEntityUtils.createRidePacket(this.parent.getId(), passengers));
        }
    }

    protected void addDirectPassengers(IntList passengers) {
    }

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
    }

    @Override
    protected void onDataLoaded() {
        this.onDimensionsUpdated(this.parent.getDimensions(this.parent.getPose()));
        super.onDataLoaded();
    }

    @Override
    protected boolean shouldSkipTick() {
        int parentTickCount = this.parent.tickCount;
        if (parentTickCount < ++this.tickCount) {
            // If the parent entity is behind, they likely haven't been ticked - in which case we can skip this tick too.
            this.tickCount = parentTickCount;
            return true;
        }
        return super.shouldSkipTick();
    }

    @Override
    public CommandSourceStack createCommandSourceStack() {
        return this.parent.createCommandSourceStack();
    }

    @Override
    public void onDimensionsUpdated(EntityDimensions dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key, Object object) {
        if (key.equals(EntityTrackedData.FLAGS)) {
            byte value = (byte) object;
            this.updateOnFire(Utils.getSharedFlag(value, EntityTrackedData.ON_FIRE_FLAG_INDEX));
            this.updateGlowing(Utils.getSharedFlag(value, EntityTrackedData.GLOWING_FLAG_INDEX));
            this.updateInvisibility(Utils.getSharedFlag(value, EntityTrackedData.INVISIBLE_FLAG_INDEX));
        }
    }

    protected void updateOnFire(boolean displayFire) {
    }

    protected void updateInvisibility(boolean isInvisible) {
        for (Bone bone : this.bones) {
            bone.setInvisible(isInvisible);
        }
    }

    protected void updateGlowing(boolean isGlowing) {
        for (Bone bone : this.bones) {
            bone.element().setGlowing(isGlowing);
        }
    }

    @Override
    public int[] getDisplayIds() {
        int[] displays = new int[this.bones.length + this.additionalDisplays.size()];

        int index = 0;
        for (Bone bone : this.bones) {
            displays[index++] = bone.element().getEntityId();
        }

        for (DisplayElement element : this.additionalDisplays) {
            displays[index++] = element.getEntityId();
        }

        return displays;
    }

    @Override
    public int getDisplayVehicleId() {
        return this.parent.getId();
    }

    @Override
    public int getVehicleId() {
        return this.parent.getId();
    }

    @Override
    public int getLeashedId() {
        return this.parent.getId();
    }

    @Override
    public int getEntityEventId() {
        return this.parent.getId();
    }

    @Override
    public int getCritParticleId() {
        return this.parent.getId();
    }

    public T getParent() {
        return this.parent;
    }
}
