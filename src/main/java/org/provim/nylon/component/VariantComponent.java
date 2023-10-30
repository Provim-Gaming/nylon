package org.provim.nylon.component;

import org.jetbrains.annotations.Nullable;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.wrappers.Bone;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjVariant;

import java.util.UUID;

public class VariantComponent extends ComponentBase {
    @Nullable
    private AjVariant currentVariant = null;

    public VariantComponent(AjModel model, AbstractAjHolder<?> holder) {
        super(model, holder);
    }

    public void applyDefaultVariant() {
        if (this.currentVariant != null) {
            this.currentVariant = null;
            for (Bone bone : this.holder.getBones()) {
                bone.updateItem(bone.node().customModelData());
            }
        }
    }

    public void applyVariant(String variantName) {
        if (this.currentVariant != null && this.currentVariant.name().equals(variantName)) {
            return;
        }

        for (AjVariant variant : this.model.variants().values()) {
            if (variant.name().equals(variantName)) {
                this.currentVariant = variant;
                this.applyVariantToBones(this.currentVariant);
                return;
            }
        }
    }

    public void applyVariant(UUID variantUuid) {
        AjVariant variant = this.model.variants().get(variantUuid);
        if (variant == null || variant == this.currentVariant) {
            return;
        }

        this.currentVariant = variant;
        this.applyVariantToBones(variant);
    }

    private void applyVariantToBones(AjVariant variant) {
        for (Bone bone : this.holder.getBones()) {
            UUID uuid = bone.node().uuid();
            AjVariant.ModelInfo modelInfo = variant.models().get(uuid);
            if (modelInfo != null && variant.isAffected(uuid)) {
                bone.updateItem(modelInfo.customModelData());
            }
        }
    }
}
