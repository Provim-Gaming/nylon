package org.provim.nylon.api;

import org.jetbrains.annotations.Nullable;
import org.provim.nylon.model.AjVariant;

import java.util.UUID;

public interface Variant {
    /**
     * Returns the current variant of the entity.
     */
    @Nullable AjVariant current();

    /**
     * Applies the default variant to the model of the entity.
     */
    void applyDefault();

    /**
     * Applies the given variant to the model of the entity.
     */
    void apply(String variantName);

    /**
     * Applies the given variant to the model of the entity.
     */
    void apply(UUID variantUuid);

    default boolean is(String variantName) {
        AjVariant current = this.current();
        return current != null && current.name().equals(variantName);
    }

    default boolean isDefault() {
        return this.current() == null;
    }
}
