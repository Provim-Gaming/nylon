package org.provim.nylon.entities.holders;

import net.minecraft.world.entity.Entity;
import org.provim.nylon.entities.holders.base.AbstractAjHolder;
import org.provim.nylon.model.AjModel;

public class SimpleAjHolder extends AbstractAjHolder<Entity> {
    public SimpleAjHolder(Entity parent, AjModel model) {
        this(parent, model, false);
    }

    public SimpleAjHolder(Entity parent, AjModel model, boolean updateElementsAsync) {
        super(parent, model, updateElementsAsync);
    }
}
