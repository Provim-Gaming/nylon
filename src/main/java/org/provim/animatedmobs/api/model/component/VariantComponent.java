package org.provim.animatedmobs.api.model.component;

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.provim.animatedmobs.api.entities.holders.elements.Bone;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.AjVariant;

public class VariantComponent extends ComponentBase {
    @Nullable
    private AjVariant currentVariant = null;

    public VariantComponent(AjModel model) {
        super(model);
    }

    public void applyDefaultVariant(Bone[] bones) {
        if (this.currentVariant != null) {
            for (Bone bone : bones) {
                this.updateItem(bone.element(), bone.node().customModelData());
            }
            this.currentVariant = null;
        }
    }

    public void applyVariant(String variantName, Bone[] bones) {
        if (this.currentVariant != null && this.currentVariant.name().equals(variantName)) {
            return;
        }

        for (AjVariant variant : this.model.variants().values()) {
            if (variant.name().equals(variantName)) {
                this.currentVariant = variant;
            }
        }

        if (this.currentVariant != null) {
            for (Bone bone : bones) {
                AjVariant.ModelInfo modelInfo = this.currentVariant.models().get(bone.node().uuid());
                if (modelInfo != null && this.currentVariant.isAffected(bone.node().name())) {
                    this.updateItem(bone.element(), modelInfo.customModelData());
                }
            }
        }
    }

    private void updateItem(ItemDisplayElement element, int customModelData) {
        ItemStack stack = element.getItem();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("CustomModelData", customModelData);
        element.getDataTracker().set(DisplayTrackedData.Item.ITEM, stack, true);
    }
}
