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

package org.provim.nylon.elements;

import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.mixin.SlimeEntityAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class CollisionElement extends GenericEntityElement {
    private InteractionHandler handler = InteractionHandler.EMPTY;

    public CollisionElement(InteractionHandler handler) {
        this.dataTracker.set(EntityTrackedData.SILENT, true);
        this.dataTracker.set(EntityTrackedData.NO_GRAVITY, true);
        this.dataTracker.set(EntityTrackedData.FLAGS, (byte) ((1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
        this.setHandler(handler);
    }

    public static CollisionElement createWithRedirect(Entity redirectedEntity) {
        return new CollisionElement(InteractionHandler.redirect(redirectedEntity));
    }

    public void setHandler(InteractionHandler handler) {
        this.handler = handler;
    }

    @Override
    public InteractionHandler getInteractionHandler(ServerPlayer player) {
        return this.handler;
    }

    @Override
    protected final EntityType<? extends Entity> getEntityType() {
        return EntityType.SLIME;
    }

    public int getSize() {
        return this.dataTracker.get(SlimeEntityAccessor.getSLIME_SIZE());
    }

    public void setSize(int size) {
        this.dataTracker.set(SlimeEntityAccessor.getSLIME_SIZE(), size);
    }
}