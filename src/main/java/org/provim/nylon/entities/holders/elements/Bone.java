package org.provim.nylon.entities.holders.elements;

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.world.item.ItemStack;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

public class Bone extends DisplayWrapper<ItemDisplayElement> {
    private final boolean isHead;
    private final ItemStack item;
    private boolean invisible;

    public static Bone of(ItemDisplayElement element, AjNode node, AjPose defaultPose, boolean isHead) {
        return new Bone(element, node, defaultPose, isHead);
    }

    public static Bone of(ItemDisplayElement element, AjNode node, AjPose defaultPose) {
        return new Bone(element, node, defaultPose, node.name().startsWith("head"));
    }

    protected Bone(ItemDisplayElement element, AjNode node, AjPose defaultPose, boolean isHead) {
        super(element, node, defaultPose);
        this.isHead = isHead;
        this.item = element.getItem();
    }

    @Override
    public boolean isHead() {
        return this.isHead;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;

        if (invisible) {
            this.setTrackedItem(ItemStack.EMPTY);
        } else {
            this.setTrackedItem(this.item);
        }
    }

    public void updateItem(int customModelData) {
        this.item.getOrCreateTag().putInt("CustomModelData", customModelData);

        if (!this.invisible) {
            this.setTrackedItem(this.item);
        }
    }

    private void setTrackedItem(ItemStack item) {
        this.element().getDataTracker().set(DisplayTrackedData.Item.ITEM, item, true);
    }
}
