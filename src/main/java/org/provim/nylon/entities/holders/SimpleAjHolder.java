package org.provim.animatedmobs.api.entities.holders;

import net.minecraft.world.entity.Entity;
import org.provim.animatedmobs.api.model.AjModel;

public class SimpleAjHolder extends AbstractAjHolder<Entity> {
    public SimpleAjHolder(Entity parent, AjModel model) {
        this(parent, model, false);
    }

    public SimpleAjHolder(Entity parent, AjModel model, boolean updateElementsAsync) {
        super(parent, model, updateElementsAsync);
    }
}
