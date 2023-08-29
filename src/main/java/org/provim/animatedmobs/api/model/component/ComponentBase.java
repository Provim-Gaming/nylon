package org.provim.animatedmobs.api.model.component;

import org.provim.animatedmobs.api.model.AjModel;

public abstract class ComponentBase {
    protected final AjModel model;

    public ComponentBase(AjModel model) {
        this.model = model;
    }
}
