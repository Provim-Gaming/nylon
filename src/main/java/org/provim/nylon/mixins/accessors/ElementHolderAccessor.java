package org.provim.nylon.mixins.accessors;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = ElementHolder.class, remap = false)
public interface ElementHolderAccessor {
    @Accessor
    List<VirtualElement> getElements();
}
