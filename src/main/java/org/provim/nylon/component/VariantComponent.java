package org.provim.nylon.component;

import org.jetbrains.annotations.Nullable;
import org.provim.nylon.entities.holders.elements.Bone;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjVariant;

public class VariantComponent extends ComponentBase {
    @Nullable
    private AjVariant currentVariant = null;

    public VariantComponent(AjModel model) {
        super(model);
    }

    public void applyDefaultVariant(Bone[] bones) {
        if (this.currentVariant != null) {
            for (Bone bone : bones) {
                bone.updateItem(bone.node().customModelData());
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
                    bone.updateItem(modelInfo.customModelData());
                }
            }
        }
    }
}
