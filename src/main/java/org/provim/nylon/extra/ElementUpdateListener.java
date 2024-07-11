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

package org.provim.nylon.extra;

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.provim.nylon.data.model.nylon.Transform;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.entity.EntityHolder;
import org.provim.nylon.holders.wrappers.Locator;

/**
 * Listener for locators, updates a single GenericEntityElement
 */
public class ElementUpdateListener implements Locator.LocatorListener {
    protected final GenericEntityElement element;

    public ElementUpdateListener(GenericEntityElement element) {
        this.element = element;
    }

    @Override
    public void update(AbstractAjHolder holder, Transform transform) {
        if (this.element.isSendingPositionUpdates()) {
            if (holder instanceof EntityHolder<?> entityHolder) {
                this.updateEntityBasedHolder(entityHolder, transform);
            } else {
                this.updateNonEntityBasedHolder(holder, transform);
            }
        }
    }

    private void updateEntityBasedHolder(EntityHolder<?> holder, Transform transform) {
        Entity parent = holder.getParent();
        float scale = holder.getScale();
        float yRot = parent.getYRot();
        float angle = yRot * Mth.DEG_TO_RAD;

        Vector3f offset = transform.translation();
        if (scale != 1F) {
            offset.mul(scale);
        }
        offset.rotateY(-angle);

        Vec3 pos = holder.getPos().add(offset.x, offset.y, offset.z);
        holder.sendPacket(VirtualEntityUtils.createMovePacket(
                this.element.getEntityId(),
                pos,
                pos,
                true,
                yRot,
                0F
        ));
    }

    private void updateNonEntityBasedHolder(AbstractAjHolder holder, Transform transform) {
        float scale = holder.getScale();
        Vector3fc offset = scale != 1F
                ? transform.translation().mul(scale)
                : transform.readOnlyTranslation();

        Vec3 pos = holder.getPos().add(offset.x(), offset.y(), offset.z());
        holder.sendPacket(VirtualEntityUtils.createMovePacket(
                this.element.getEntityId(),
                pos,
                pos,
                false,
                0F,
                0F
        ));
    }
}