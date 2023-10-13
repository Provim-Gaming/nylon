package org.provim.nylon.component;

import net.minecraft.server.MinecraftServer;
import org.provim.nylon.model.AjModel;

public abstract class ComponentBase {
    protected final AjModel model;
    protected final MinecraftServer server;

    public ComponentBase(AjModel model, MinecraftServer server) {
        this.model = model;
        this.server = server;
    }
}
