package org.provim.animatedmobs.api.model.component;

import org.provim.animatedmobs.api.model.AjModel;

public abstract class ComponentBase {
    protected AjModel model;

    public ComponentBase(AjModel model) {
        this.model = model;
    }
}
