package org.provim.nylon.holders.simple;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.entity.Entity;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.model.AjModel;

public class SimpleAjHolder<T extends Entity & AjEntity> extends AbstractAjHolder<T> {
    public SimpleAjHolder(T parent, AjModel model) {
        super(parent, model);
    }

    @Override
    protected void updateOnFire(boolean displayFire) {
    }

    @Override
    protected void addDirectPassengers(IntList passengers) {
        super.addDirectPassengers(passengers);
        for (int display : this.getDisplayIds()) {
            passengers.add(display);
        }
    }
}