package org.provim.animatedmobs.api.model.component;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.AjPose;

import java.util.UUID;

public class PoseComponent extends ComponentBase {
    private final Object2ObjectOpenHashMap<UUID, AjPose> defaultPoses = new Object2ObjectOpenHashMap<>();

    public PoseComponent(AjModel model) {
        super(model);
    }

    public void putDefault(UUID uuid, AjPose pose) {
        this.defaultPoses.put(uuid, pose);
    }

    public AjPose getDefault(UUID uuid) {
        return this.defaultPoses.get(uuid);
    }
}
