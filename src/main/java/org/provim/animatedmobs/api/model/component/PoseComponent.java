package org.provim.animatedmobs.api.model.component;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.AjPose;

public class PoseComponent extends ComponentBase {
    private final Object2ObjectOpenHashMap<DisplayElement, AjPose> defaultPoses = new Object2ObjectOpenHashMap<>();

    public PoseComponent(AjModel model) {
        super(model);
    }

    public void putDefault(DisplayElement element, AjPose pose) {
        this.defaultPoses.put(element, pose);
    }

    public AjPose getDefault(DisplayElement element) {
        return this.defaultPoses.get(element);
    }
}
