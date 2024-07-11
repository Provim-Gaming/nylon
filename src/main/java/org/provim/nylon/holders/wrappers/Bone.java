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

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import org.provim.nylon.data.model.nylon.Node;
import org.provim.nylon.data.model.nylon.Transform;

public class Bone extends DisplayWrapper<ItemDisplayElement> {
    private final ItemStack item;
    private boolean invisible;

    public static Bone of(ItemDisplayElement element, Node node, Transform defaultTransform, boolean isHead) {
        return new Bone(element, node, defaultTransform, isHead);
    }

    public static Bone of(ItemDisplayElement element, Node node, Transform defaultTransform) {
        return new Bone(element, node, defaultTransform, node.name.startsWith("head"));
    }

    protected Bone(ItemDisplayElement element, Node node, Transform defaultTransform, boolean isHead) {
        super(element, node, defaultTransform, isHead);
        this.item = element.getItem();
    }

    public void setInvisible(boolean invisible) {
        if (this.invisible == invisible) {
            return;
        }

        this.invisible = invisible;
        if (invisible) {
            this.setTrackedItem(ItemStack.EMPTY);
        } else {
            this.setTrackedItem(this.item);
        }
    }

    public void updateColor(int color) {
        if (!this.item.is(ItemTags.DYEABLE)) {
            return;
        }

        this.item.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
        if (!this.invisible) {
            this.setTrackedItem(this.item);
        }
    }

    public void updateModelData(int customModelData) {
        this.item.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));

        if (!this.invisible) {
            this.setTrackedItem(this.item);
        }
    }

    private void setTrackedItem(ItemStack item) {
        this.element().getDataTracker().set(DisplayTrackedData.Item.ITEM, item, true);
    }
}
