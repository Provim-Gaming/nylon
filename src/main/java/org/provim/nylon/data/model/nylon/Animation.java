package org.provim.nylon.data.model.nylon;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

public class Animation {
    public final Frame[] frames;
    public final int duration;
    public final int loopDelay;
    public final LoopMode loopMode;
    private final ReferenceOpenHashSet<UUID> includedNodes;

    public Animation(
            Frame[] frames,
            int loopDelay,
            LoopMode loopMode,
            ReferenceOpenHashSet<UUID> includedNodes
    ) {
        Validate.notNull(frames, "Frames cannot be null");
        Validate.notNull(includedNodes, "Affected bones cannot be null");
        Validate.notNull(loopMode, "Loop mode cannot be null");
        Validate.notNull(includedNodes, "Affected bones cannot be null");

        this.frames = frames;
        this.duration = frames.length;
        this.loopDelay = loopDelay;
        this.loopMode = loopMode;
        this.includedNodes = includedNodes;
    }

    public boolean isAffected(UUID boneUuid) {
        return this.includedNodes.contains(boneUuid);
    }

    public enum LoopMode {
        ONCE, HOLD, LOOP
    }
}
