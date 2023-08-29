package org.provim.animatedmobs.api.model.component;

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.AjNode;
import org.provim.animatedmobs.api.model.AjVariant;

import java.util.Map;
import java.util.UUID;

public class VariantComponent extends ComponentBase {
    @Nullable
    private AjVariant currentVariant = null;

    public VariantComponent(AjModel model) {
        super(model);
    }

    public void applyDefaultVariant(Map<UUID, ItemDisplayElement> itemDisplays) {
        if (this.currentVariant != null) {
            itemDisplays.forEach((uuid, element) -> {
                AjNode node = this.model.rig().nodeMap().get(uuid);
                this.updateItem(element, node.customModelData());
            });
            this.currentVariant = null;
        }
    }

    public void applyVariant(String variantName, Map<UUID, ItemDisplayElement> itemDisplays) {
        if (this.currentVariant != null && this.currentVariant.name().equals(variantName)) {
            return;
        }

        for (AjVariant variant : this.model.variants().values()) {
            if (variant.name().equals(variantName)) {
                this.currentVariant = variant;
            }
        }

        if (this.currentVariant != null) {
            itemDisplays.forEach((uuid, element) -> {
                AjVariant.ModelInfo modelInfo = this.currentVariant.models().get(uuid);
                if (modelInfo != null) {
                    this.updateItem(element, modelInfo.customModelData());
                }
            });
        }
    }

    private void updateItem(ItemDisplayElement element, int customModelData) {
        ItemStack stack = element.getItem();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("CustomModelData", customModelData);
        element.getDataTracker().set(DisplayTrackedData.Item.ITEM, stack, true);
    }
}
