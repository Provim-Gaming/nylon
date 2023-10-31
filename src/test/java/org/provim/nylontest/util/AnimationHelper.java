package org.provim.nylontest.util;

import net.minecraft.world.entity.LivingEntity;
import org.provim.nylon.api.AjHolderInterface;
import org.provim.nylon.api.Animator;
import org.provim.nylon.api.Variant;

public class AnimationHelper {

    public static void updateWalkAnimation(LivingEntity entity, AjHolderInterface holder) {
        updateWalkAnimation(entity, holder, 1);
    }

    public static void updateWalkAnimation(LivingEntity entity, AjHolderInterface holder, int priority) {
        Animator animator = holder.getAnimator();
        if (entity.walkAnimation.isMoving() && entity.walkAnimation.speed() > 0.02) {
            animator.playAnimation("walk", priority);
            animator.pauseAnimation("idle");
        } else {
            animator.pauseAnimation("walk");
            animator.playAnimation("idle", priority);
        }
    }

    public static void updateHurtVariant(LivingEntity entity, AjHolderInterface holder) {
        Variant variant = holder.getVariant();
        if (entity.hurtTime > 0 || entity.deathTime > 0 && variant.isDefault()) {
            variant.apply("hurt");
        } else if (variant.is("hurt")) {
            variant.applyDefault();
        }
    }
}