package org.provim.animatedmobs.api.model.component;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.provim.animatedmobs.api.model.AjAnimation;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.AjPose;

import java.util.UUID;

public class AnimationComponent extends ComponentBase {
    @Nullable
    private AjAnimation currentAnimation = null;
    @Nullable
    private AjAnimation extraAnimation = null;

    private int extraAnimationTicks = -1;

    public AnimationComponent(AjModel model) {
        super(model);
    }

    public void setCurrentAnimation(String currentAnimation) {
        this.currentAnimation = this.model.animations().get(currentAnimation);
    }

    @Nullable
    public AjAnimation getCurrentAnimation() {
        return currentAnimation;
    }

    public AjPose findCurrentAnimationPose(int tickCount, UUID uuid) {
        if (this.currentAnimation == null || this.currentAnimation.frames().isEmpty()) {
            return null;
        }

        int index = (tickCount / 2) % (this.currentAnimation.frames().size() - 1);
        AjAnimation.AjFrame currentFrame = this.currentAnimation.frames().get(index);
        AjPose pose = currentFrame.findPose(uuid);
        if (pose == null) {
            pose = lastAvailable(uuid, index);
        }
        return pose;
    }

    private AjPose lastAvailable(UUID uuid, int currentIndex) {
        if (this.currentAnimation != null) {
            for (int i = currentIndex - 1; i >= 0; i--) {
                AjAnimation.AjFrame frame = this.currentAnimation.frames().get(i);
                AjPose pose = frame.findPose(uuid);
                if (pose != null) {
                    return pose;
                }
            }
        }
        return null;
    }

    public boolean extraAnimationAvailable() {
        return this.extraAnimationTicks >= 0;
    }

    public AjPose findExtraAnimationPose(UUID uuid) {
        if (this.extraAnimation == null || extraAnimationTicks <= 0) {
            return null;
        }

        int index = this.extraAnimation.frames().size() - this.extraAnimationTicks;
        AjAnimation.AjFrame currentFrame = this.extraAnimation.frames().get(index);
        return currentFrame.findPose(uuid);
    }

    public void startExtraAnimation(String animationName) {
        this.extraAnimation = this.model.animations().get(animationName);
        if (this.extraAnimation != null) {
            this.extraAnimationTicks = this.extraAnimation.frames().size() - 1;
        }
    }

    public AnimationTransform getInterpolatedAnimationTransform(AjPose pose) {
        Vector3f pos = pose.getPos();
        Quaternionf rightRotation = pose.getRot();
        Vector3f scale = pose.getScale();
        return new AnimationTransform(pos, rightRotation, scale);
    }

    public void decreaseCounter() {
        if (this.extraAnimationTicks >= 0) {
            // 2 if ticked every other tick..
            // todo: tickCount based instead of decreasing numbers :P
            this.extraAnimationTicks -= 2;
        }
    }

    public record AnimationTransform(Vector3f pos, Quaternionf rot, Vector3f scale) {
    }
}
