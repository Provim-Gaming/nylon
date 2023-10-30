package org.provim.nylontest.util;

import net.minecraft.world.entity.LivingEntity;
import org.provim.nylon.api.AjHolderInterface;

public class AnimationHelper {

    public static void updateWalkAnimation(LivingEntity entity, AjHolderInterface holder) {
        updateWalkAnimation(entity, holder, 1);
    }

    public static void updateWalkAnimation(LivingEntity entity, AjHolderInterface holder, int priority) {
        if (entity.walkAnimation.isMoving() && entity.walkAnimation.speed() > 0.02) {
            holder.getAnimator().playAnimation("walk", priority);
            holder.getAnimator().pauseAnimation("idle");
        } else {
            holder.getAnimator().pauseAnimation("walk");
            holder.getAnimator().playAnimation("idle", priority);
        }
    }

    public static void updateHurtVariant(LivingEntity entity, AjHolderInterface holder) {
        if (entity.hurtTime > 0 || entity.deathTime > 0) {
            holder.setCurrentVariant("hurt");
        } else {
            holder.setDefaultVariant();
        }
    }
}