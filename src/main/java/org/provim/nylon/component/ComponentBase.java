package org.provim.nylon.component;

import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.model.AjModel;

public abstract class ComponentBase {
    protected final AjModel model;
    protected final AbstractAjHolder holder;

    public ComponentBase(AjModel model, AbstractAjHolder holder) {
        this.model = model;
        this.holder = holder;
    }
}
