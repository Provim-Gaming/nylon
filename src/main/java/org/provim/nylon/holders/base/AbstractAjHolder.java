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
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.api.AjHolder;
import org.provim.nylon.component.AnimationComponent;
import org.provim.nylon.component.VariantComponent;
import org.provim.nylon.holders.wrappers.Bone;
import org.provim.nylon.holders.wrappers.DisplayWrapper;
import org.provim.nylon.holders.wrappers.Locator;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

import java.util.List;

public abstract class AbstractAjHolder extends AjElementHolder implements AjHolder {

    protected final AjModel model;
    protected final AnimationComponent animation;
    protected final VariantComponent variant;
    protected final Object2ObjectOpenHashMap<String, Locator> locatorMap;

    protected Bone[] bones;
    protected Locator[] locators;
    protected float scale = 1F;
    protected int color = -1;

    protected AbstractAjHolder(AjModel model, ServerLevel level) {
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
        Item rigItem = this.model.projectSettings().rigItem();
        for (AjNode node : this.model.rig().nodeMap().values()) {
            AjPose defaultPose = this.model.rig().defaultPose().get(node.uuid());
            switch (node.type()) {
                case bone -> {
                    ItemDisplayElement bone = this.createBone(node, rigItem);
                    if (bone != null) {
                        bones.add(Bone.of(bone, node, defaultPose));
                        this.addElement(bone);
                    }
                }
                case locator -> {
                    this.locatorMap.put(node.name(), Locator.of(node, defaultPose));
                }
            }
        }
    }

    @Nullable
    protected ItemDisplayElement createBone(AjNode node, Item rigItem) {
        ItemDisplayElement element = new ItemDisplayElement();
        element.setModelTransformation(ItemDisplayContext.FIXED);
        element.setInvisible(true);
        element.setInterpolationDuration(2);
        element.setTeleportDuration(3);

        ItemStack stack = new ItemStack(rigItem);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(node.customModelData()));
        if (stack.is(ItemTags.DYEABLE)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(-1, false));
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
        this.updateElement(display, this.animation.findPose(display));
    }

    public void initializeDisplay(DisplayWrapper<?> display) {
        this.updateElement(display, display.getDefaultPose());
    }

    public void updateElement(DisplayWrapper<?> display, @Nullable AjPose pose) {
        if (pose != null) {
            this.applyPose(pose, display);
        }
    }

    protected void updateLocator(Locator locator) {
        if (locator.requiresUpdate()) {
            AjPose pose = this.animation.findPose(locator);
            if (pose != null) {
                locator.updateListeners(this, pose);
            }
        }
    }

    protected void applyPose(AjPose pose, DisplayWrapper<?> display) {
        if (this.scale != 1F) {
            display.setScale(pose.scale().mul(this.scale));
            display.setTranslation(pose.translation().mul(this.scale));
        } else {
            display.setScale(pose.readOnlyScale());
            display.setTranslation(pose.readOnlyTranslation());
        }

        display.setLeftRotation(pose.readOnlyLeftRotation());
        display.setRightRotation(pose.readOnlyRightRotation());

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

    @Override
    public AjModel getModel() {
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
