package org.provim.nylon.mixins.accessors;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ElementHolder.class)
public interface ElementHolderAccessor {
    @Accessor
    List<VirtualElement> getElements();
}
