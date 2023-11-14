package org.provim.nylon.component;

import org.jetbrains.annotations.Nullable;
import org.provim.nylon.api.VariantController;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.wrappers.Bone;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjVariant;

import java.util.UUID;

public class VariantComponent extends ComponentBase implements VariantController {
    @Nullable
    private AjVariant currentVariant;

    public VariantComponent(AjModel model, AbstractAjHolder holder) {
        super(model, holder);
    }

    @Nullable
    @Override
    public AjVariant getCurrentVariant() {
        return this.currentVariant;
    }

    @Override
    public void setDefaultVariant() {
        if (this.currentVariant != null) {
            this.currentVariant = null;
            for (Bone bone : this.holder.getBones()) {
                bone.updateItem(bone.node().customModelData());
            }
        }
    }

    @Override
    public void setVariant(String variantName) {
        if (this.isCurrentVariant(variantName)) {
            return;
        }

        AjVariant variant = this.findByName(variantName);
        if (variant != null) {
            this.currentVariant = variant;
            this.applyVariantToBones(variant);
        }
    }

    @Override
    public void setVariant(UUID variantUuid) {
        AjVariant variant = this.model.variants().get(variantUuid);
        if (variant == null || variant == this.currentVariant) {
            return;
        }

        this.currentVariant = variant;
        this.applyVariantToBones(variant);
    }

    @Nullable
    private AjVariant findByName(String variantName) {
        for (AjVariant variant : this.model.variants().values()) {
            if (variant.name().equals(variantName)) {
                return variant;
            }
        }
        return null;
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
