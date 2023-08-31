package org.provim.animatedmobs.api.model.component;

import org.jetbrains.annotations.Nullable;
import org.provim.animatedmobs.api.model.*;

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
    public AjPose findCurrentAnimationPose(int tickCount, AjNode node) {
        if (this.currentAnimation == null ||
            this.currentAnimation.frames().length == 0 ||
            this.currentAnimation.isUnaffected(node.name())
        ) {
            return null;
        }

        int index = (tickCount / 2) % (this.currentAnimation.frames().length - 1);
        AjFrame currentFrame = this.currentAnimation.frames()[index];
        AjPose pose = currentFrame.poses().get(node.uuid());
        if (pose == null) {
            pose = this.lastAvailable(node.uuid(), index);
        }
        return pose;
    }

    @Nullable
    private AjPose lastAvailable(UUID uuid, int currentIndex) {
        if (this.currentAnimation != null) {
            for (int index = currentIndex - 1; index >= 0; index--) {
                AjFrame frame = this.currentAnimation.frames()[index];
                AjPose pose = frame.poses().get(uuid);
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

    public AjPose findExtraAnimationPose(AjNode node) {
        if (this.extraAnimation == null ||
            this.extraAnimationTicks <= 0 ||
            this.extraAnimation.isUnaffected(node.name())
        ) {
            return null;
        }

        int index = this.extraAnimation.frames().length - this.extraAnimationTicks;
        AjFrame currentFrame = this.extraAnimation.frames()[index];
        return currentFrame.poses().get(node.uuid());
    }

    public void startExtraAnimation(String animationName) {
        this.extraAnimation = this.model.animations().get(animationName);
        if (this.extraAnimation != null) {
            this.extraAnimationTicks = this.extraAnimation.frames().length - 1;
        }
    }

    public void decreaseCounter() {
        if (this.extraAnimationTicks >= 0) {
            // 2 if ticked every other tick..
            // todo: tickCount based instead of decreasing numbers :P
            this.extraAnimationTicks -= 2;
        }
    }
}
