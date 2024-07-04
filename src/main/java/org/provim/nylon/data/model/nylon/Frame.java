package org.provim.nylon.data.model.nylon;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.apache.commons.lang3.Validate;
import org.provim.nylon.holders.base.AbstractAjHolder;

import java.util.UUID;

public class Frame {
    public final Reference2ObjectOpenHashMap<UUID, Pose> poses;

    public Frame(Reference2ObjectOpenHashMap<UUID, Pose> poses) {
        Validate.notNull(poses, "Poses cannot be null");

        this.poses = poses;
    }

    public boolean requiresUpdates() {
        return false;
    }

    public void run(AbstractAjHolder holder) {
    }
}