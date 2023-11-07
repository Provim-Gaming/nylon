package org.provim.nylon.holders.entity.simple;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.holders.entity.EntityHolder;
import org.provim.nylon.holders.wrappers.Bone;
import org.provim.nylon.model.AjModel;

public class SimpleEntityHolder<T extends Entity & AjEntity> extends EntityHolder<T> {
    public SimpleEntityHolder(T parent, AjModel model) {
        super(parent, model);
    }

    @Override
    protected void addDirectPassengers(IntList passengers) {
        super.addDirectPassengers(passengers);
        for (int display : this.getDisplayIds()) {
            passengers.add(display);
        }
    }

    @Override
    public void onDimensionsUpdated(EntityDimensions dimensions) {
        super.onDimensionsUpdated(dimensions);

        for (Bone bone : this.bones) {
            bone.element().setDisplaySize(dimensions.width * 2, dimensions.height + 1);
        }
    }
}
