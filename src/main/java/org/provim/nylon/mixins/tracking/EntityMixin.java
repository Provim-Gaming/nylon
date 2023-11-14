package org.provim.nylon.mixins.tracking;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.AjEntityHolder;
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
        AjEntityHolder holder = AjEntity.getHolder(this);
        if (holder != null) {
            holder.onDimensionsUpdated(this.dimensions);
        }
    }
}
