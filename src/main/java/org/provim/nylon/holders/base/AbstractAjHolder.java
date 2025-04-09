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

package org.provim.nylon.holders.base;

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import org.provim.nylon.api.AjHolder;
import org.provim.nylon.component.AnimationComponent;
import org.provim.nylon.component.VariantComponent;
import org.provim.nylon.data.model.nylon.Node;
import org.provim.nylon.data.model.nylon.NylonModel;
import org.provim.nylon.data.model.nylon.Transform;
import org.provim.nylon.holders.wrappers.Bone;
import org.provim.nylon.holders.wrappers.DisplayWrapper;
import org.provim.nylon.holders.wrappers.Locator;

import java.util.List;

public abstract class AbstractAjHolder extends AjElementHolder implements AjHolder {

    protected final NylonModel model;
    protected final AnimationComponent animation;
    protected final VariantComponent variant;
    protected final Object2ObjectOpenHashMap<String, Locator> locatorMap;

    protected Bone[] bones;
    protected Locator[] locators;
    protected float scale = 1F;
    protected int color = -1;

    protected AbstractAjHolder(NylonModel model, ServerLevel level) {
        super(level);
        this.model = model;
        this.animation = new AnimationComponent(model, this);
        this.variant = new VariantComponent(model, this);
        this.locatorMap = new Object2ObjectOpenHashMap<>();
    }

    @Override
    protected final void initializeElements() {
        ObjectArrayList<Bone> bones = new ObjectArrayList<>();
        this.setupElements(bones);

        this.locators = new Locator[this.locatorMap.size()];
        this.bones = new Bone[bones.size()];

        int index = 0;
        for (Locator locator : this.locatorMap.values()) {
            this.locators[index++] = locator;
        }

        for (index = 0; index < bones.size(); index++) {
            this.bones[index] = bones.get(index);
        }
    }

    protected void setupElements(List<Bone> bones) {
        Item rigItem = this.model.rigItem;
        for (Node node : this.model.nodes) {
            switch (node.type) {
                case BONE -> {
                    ItemDisplayElement bone = this.createBone(node, rigItem);
                    if (bone != null) {
                        bones.add(Bone.of(bone, node));
                        this.addElement(bone);
                    }
                }
                case LOCATOR -> {
                    this.locatorMap.put(node.name, Locator.of(node));
                }
            }
        }
    }

    @Nullable
    protected ItemDisplayElement createBone(Node node, Item rigItem) {
        ItemDisplayElement element = new ItemDisplayElement();
        element.setItemDisplayContext(ItemDisplayContext.FIXED);
        element.setSendPositionUpdates(false);
        element.setInvisible(true);
        element.setInterpolationDuration(1);
        element.setTeleportDuration(3);

        ItemStack stack = new ItemStack(rigItem);
        stack.set(DataComponents.ITEM_MODEL, node.model);
        if (stack.is(ItemTags.DYEABLE)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(-1));
        }

        element.setItem(stack);
        return element;
    }

    @Override
    protected void onDataLoaded() {
        for (Bone bone : this.bones) {
            this.initializeDisplay(bone);
        }
    }

    @Override
    protected boolean shouldSkipTick() {
        return false;
    }

    @Override
    protected void onTick() {
        this.animation.tickAnimations();
    }

    @Override
    protected void onAsyncTick() {
        for (Bone bone : this.bones) {
            this.updateElement(bone);
        }

        for (Locator locator : this.locators) {
            this.updateLocator(locator);
        }
    }

    protected void updateElement(DisplayWrapper<?> display) {
        this.updateElement(display, this.animation.findCurrentTransform(display));
    }

    public void initializeDisplay(DisplayWrapper<?> display) {
        this.updateElement(display, display.getDefaultTransform());
    }

    public void updateElement(DisplayWrapper<?> display, @Nullable Transform transform) {
        if (transform != null) {
            this.applyTransform(transform, display);
        }
    }

    protected void updateLocator(Locator locator) {
        Transform transform = this.animation.findCurrentTransform(locator);
        if (transform != null) {
            locator.update(this, transform);
        }
    }

    protected void applyTransform(Transform transform, DisplayWrapper<?> display) {
        if (this.scale != 1F) {
            display.setScale(transform.scale().mul(this.scale));
            display.setTranslation(transform.translation().mul(this.scale));
        } else {
            display.setScale(transform.readOnlyScale());
            display.setTranslation(transform.readOnlyTranslation());
        }

        display.setLeftRotation(transform.readOnlyLeftRotation());

        display.startInterpolation();
    }

    @Override
    public void setColor(int color) {
        if (color != this.color) {
            this.color = color;
            for (Bone bone : this.bones) {
                bone.updateColor(color);
            }
        }
    }

    public Vec3 getTransformOffsetPos(Transform transform) {
        float scale = this.getScale();
        Vector3fc offset = scale != 1F
                ? transform.translation().mul(scale)
                : transform.readOnlyTranslation();

        return this.getPos().add(offset.x(), offset.y(), offset.z());
    }

    @Override
    public NylonModel getModel() {
        return this.model;
    }

    @Override
    public Locator getLocator(String name) {
        return this.locatorMap.get(name);
    }

    @Override
    public VariantComponent getVariantController() {
        return this.variant;
    }

    @Override
    public AnimationComponent getAnimator() {
        return this.animation;
    }

    @Override
    public float getScale() {
        return this.scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    public Bone[] getBones() {
        return this.bones;
    }

    public Locator[] getLocators() {
        return this.locators;
    }

    abstract public CommandSourceStack createCommandSourceStack();
}
