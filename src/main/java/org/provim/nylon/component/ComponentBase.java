package org.provim.nylon.component;

import org.provim.nylon.model.AjModel;

public abstract class ComponentBase {
    protected final AjModel model;

    public ComponentBase(AjModel model) {
        this.model = model;
    }
}
