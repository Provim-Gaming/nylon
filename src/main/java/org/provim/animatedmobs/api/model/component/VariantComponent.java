package org.provim.animatedmobs.api.model.component;

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
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
                ItemStack itemStack = new ItemStack(element.getItem().getItem());
                CompoundTag tag = itemStack.getOrCreateTag();
                tag.putInt("CustomModelData", node.customModelData());
                element.setItem(itemStack);
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
                    ItemStack itemStack = new ItemStack(element.getItem().getItem());
                    CompoundTag tag = itemStack.getOrCreateTag();
                    tag.putInt("CustomModelData", modelInfo.customModelData());
                    element.setItem(itemStack);
                }
            });
        }
    }
}
