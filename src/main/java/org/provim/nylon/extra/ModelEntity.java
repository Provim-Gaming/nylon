package org.provim.nylon.extra;

import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.tracker.InteractionTrackedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.Level;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjHolderInterface;
import org.provim.nylon.holders.base.AjElementHolder;
import org.provim.nylon.holders.simple.SimpleAjHolder;
import org.provim.nylon.model.AjModel;

import java.util.List;

public class ModelEntity extends Interaction implements AjEntity {
    private final AjElementHolder<ModelEntity> holder;
    private final AjModel model;

    public ModelEntity(Level level, AjModel model) {
        super(EntityType.INTERACTION, level);
        this.model = model;
        this.holder = new SimpleAjHolder<>(this, model);

        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public AjHolderInterface getHolder() {
        return this.holder;
    }

    public AjModel getModel() {
        return this.model;
    }

    @Override
    public boolean saveAsPassenger(CompoundTag compoundTag) {
        // Don't save this entity.
        return false;
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayer player) {
        return EntityType.INTERACTION;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        data.add(SynchedEntityData.DataValue.create(InteractionTrackedData.HEIGHT, 2f));
        data.add(SynchedEntityData.DataValue.create(InteractionTrackedData.WIDTH, 2f));
    }
}
