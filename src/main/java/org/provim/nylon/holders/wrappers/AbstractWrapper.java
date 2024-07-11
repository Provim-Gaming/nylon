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
import org.provim.nylon.data.model.nylon.Transform;

public abstract class AbstractWrapper {
    private final Node node;
    private final Transform defaultTransform;
    protected Animation lastAnimation;
    protected Transform lastTransform;

    public AbstractWrapper(Node node, Transform defaultTransform) {
        this.node = node;
        this.defaultTransform = defaultTransform;
        this.lastTransform = defaultTransform;
    }

    public Node node() {
        return this.node;
    }

    public String name() {
        return this.node.name;
    }

    public Transform getDefaultTransform() {
        return this.defaultTransform;
    }

    public Animation getLastAnimation() {
        return this.lastAnimation;
    }

    public Transform getLastTransform() {
        return this.lastTransform;
    }

    public void setLastTransform(Transform transform, Animation animation) {
        this.lastAnimation = animation;
        this.lastTransform = transform;
    }
}
