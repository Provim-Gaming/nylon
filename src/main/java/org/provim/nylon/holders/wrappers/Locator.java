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

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.provim.nylon.data.model.nylon.Node;
import org.provim.nylon.data.model.nylon.Pose;
import org.provim.nylon.holders.base.AbstractAjHolder;

public class Locator extends AbstractWrapper {
    private final ObjectSet<LocatorListener> listeners;

    public static Locator of(Node node, Pose defaultPose) {
        return new Locator(node, defaultPose);
    }

    public Locator(Node node, Pose defaultPose) {
        super(node, defaultPose);
        this.listeners = ObjectSets.synchronize(new ObjectArraySet<>());
    }

    public boolean requiresUpdate() {
        return this.listeners.size() > 0;
    }

    public void updateListeners(AbstractAjHolder holder, Pose pose) {
        this.listeners.forEach(listener -> listener.update(holder, pose));
    }

    public void addListener(LocatorListener newListener) {
        this.listeners.add(newListener);
    }

    public void removeListener(LocatorListener oldListener) {
        this.listeners.remove(oldListener);
    }

    public void removeAllListeners() {
        this.listeners.clear();
    }

    public interface LocatorListener {
        /**
         * Called whenever a locator is updated.
         * This method can be called asynchronously.
         */
        void update(AbstractAjHolder holder, Pose pose);
    }
}
