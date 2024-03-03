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

import org.provim.nylon.model.AjAnimation;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

public abstract class AbstractWrapper {
    private final AjNode node;
    private final AjPose defaultPose;
    protected AjAnimation lastAnimation;
    protected AjPose lastPose;

    public AbstractWrapper(AjNode node, AjPose defaultPose) {
        this.node = node;
        this.defaultPose = defaultPose;
        this.lastPose = defaultPose;
    }

    public AjNode node() {
        return this.node;
    }

    public String name() {
        return this.node.name();
    }

    public AjPose getDefaultPose() {
        return this.defaultPose;
    }

    public AjAnimation getLastAnimation() {
        return this.lastAnimation;
    }

    public AjPose getLastPose() {
        return this.lastPose;
    }

    public void setLastPose(AjPose pose, AjAnimation animation) {
        this.lastAnimation = animation;
        this.lastPose = pose;
    }
}
