/*
 * Nylon
 * Copyright (C) 2023, 2024 Provim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
