/*
 * Nylon
 * Copyright (C) 2023, 2024 Provim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.provim.nylontest.util;

import net.minecraft.world.entity.LivingEntity;
import org.provim.nylon.api.AjHolder;
import org.provim.nylon.api.Animator;
import org.provim.nylon.util.NylonConstants;

public class AnimationHelper {

    public static void updateWalkAnimation(LivingEntity entity, AjHolder holder) {
        updateWalkAnimation(entity, holder, 0);
    }

    public static void updateWalkAnimation(LivingEntity entity, AjHolder holder, int priority) {
        Animator animator = holder.getAnimator();
        if (entity.walkAnimation.isMoving() && entity.walkAnimation.speed() > 0.02) {
            animator.playAnimation("walk", priority);
            animator.pauseAnimation("idle");
        } else {
            animator.pauseAnimation("walk");
            animator.playAnimation("idle", priority, true);
        }
    }

    public static void updateHurtColor(LivingEntity entity, AjHolder holder) {
        if (entity.hurtTime > 0 || entity.deathTime > 0) {
            holder.setColor(NylonConstants.DAMAGE_TINT_COLOR);
        } else {
            holder.clearColor();
        }
    }
}