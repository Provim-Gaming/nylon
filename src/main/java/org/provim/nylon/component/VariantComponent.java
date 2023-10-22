package org.provim.nylon.component;

import org.jetbrains.annotations.Nullable;
import org.provim.nylon.holders.wrapper.Bone;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjVariant;

import java.util.Collection;
import java.util.UUID;

public class VariantComponent extends ComponentBase {
    public AjVariant getCurrentVariant() {
        return currentVariant;
    }

    @Nullable
    private AjVariant currentVariant = null;

    public VariantComponent(AjModel model) {
        super(model);
    }

    public void applyDefaultVariant(Collection<Bone> bones) {
        if (this.currentVariant != null) {
            for (Bone bone : bones) {
                bone.updateItem(bone.node().customModelData());
            }
            this.currentVariant = null;
        }
    }

    public void applyVariant(String variantName, Collection<Bone> bones) {
        if (this.currentVariant != null && this.currentVariant.name().equals(variantName)) {
            return;
        }

        this.currentVariant = this.getVariant(variantName);
        this.applyCurrent(bones);
    }

    public void applyVariant(UUID variant, Collection<Bone> bones) {
        if (this.currentVariant != null && this.currentVariant.uuid().equals(variant)) {
            return;
        }

        this.currentVariant = this.getVariant(variant);
        if (this.currentVariant == null) {
            // "bug" in AnimatedJava - default variant doesn't have a uuid..!
            this.applyDefaultVariant(bones);
        } else {
            this.applyCurrent(bones);
        }
    }

    private void applyCurrent(Collection<Bone> bones) {
        if (this.currentVariant != null) {
            for (Bone bone : bones) {
                AjVariant.ModelInfo modelInfo = this.currentVariant.models().get(bone.node().uuid());
                if (modelInfo != null && this.currentVariant.isAffected(bone.node().name())) {
                    bone.updateItem(modelInfo.customModelData());
                }
            }
        }
    }

    AjVariant getVariant(UUID uuid) {
        return this.model.variants().get(uuid);
    }

    AjVariant getVariant(String name) {
        for (AjVariant variant : this.model.variants().values()) {
            if (variant.name().equals(name)) {
                return variant;
            }
        }
        return null;
    }
}
