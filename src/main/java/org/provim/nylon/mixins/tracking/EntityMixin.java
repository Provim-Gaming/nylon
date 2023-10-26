package org.provim.nylon.mixins.tracking;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjHolderInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    private EntityDimensions dimensions;

    @Inject(method = "refreshDimensions", at = @At("RETURN"))
    private void nylon$onRefreshedDimensions(CallbackInfo ci) {
        if (this instanceof AjEntity ajEntity) {
            AjHolderInterface holder = ajEntity.getHolder();
            if (holder != null) {
                holder.onDimensionsUpdated(this.dimensions);
            }
        }
    }
}