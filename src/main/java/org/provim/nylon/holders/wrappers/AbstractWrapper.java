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

package org.provim.nylon.holders.wrappers;

import org.provim.nylon.data.model.nylon.Animation;
import org.provim.nylon.data.model.nylon.Node;
import org.provim.nylon.data.model.nylon.Pose;

public abstract class AbstractWrapper {
    private final Node node;
    private final Pose defaultPose;
    protected Animation lastAnimation;
    protected Pose lastPose;

    public AbstractWrapper(Node node, Pose defaultPose) {
        this.node = node;
        this.defaultPose = defaultPose;
        this.lastPose = defaultPose;
    }

    public Node node() {
        return this.node;
    }

    public String name() {
        return this.node.name;
    }

    public Pose getDefaultPose() {
        return this.defaultPose;
    }

    public Animation getLastAnimation() {
        return this.lastAnimation;
    }

    public Pose getLastPose() {
        return this.lastPose;
    }

    public void setLastPose(Pose pose, Animation animation) {
        this.lastAnimation = animation;
        this.lastPose = pose;
    }
}
