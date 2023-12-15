package org.provim.nylon.mixins.accessors;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    static EntityDataAccessor<Optional<Component>> getDATA_CUSTOM_NAME() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Boolean> getDATA_CUSTOM_NAME_VISIBLE() {
        throw new UnsupportedOperationException();
    }
}
