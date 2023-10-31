package org.provim.nylon.api;

import org.jetbrains.annotations.Nullable;
import org.provim.nylon.model.AjVariant;

import java.util.UUID;

public interface VariantController {
    /**
     * Returns the current variant of the entity.
     */
    @Nullable AjVariant getCurrentVariant();

    /**
     * Applies the default variant to the model of the entity.
     */
    void setDefaultVariant();

    /**
     * Applies the given variant to the model of the entity.
     */
    void setVariant(String variantName);

    /**
     * Applies the given variant to the model of the entity.
     */
    void setVariant(UUID variantUuid);

    default boolean isSameVariant(String variantName) {
        AjVariant current = this.getCurrentVariant();
        return current != null && current.name().equals(variantName);
    }

    default boolean isDefaultVariant() {
        return this.getCurrentVariant() == null;
    }
}
