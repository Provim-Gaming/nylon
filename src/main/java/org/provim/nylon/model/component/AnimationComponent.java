package org.provim.animatedmobs.api.model.component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.provim.animatedmobs.api.entities.holders.elements.DisplayWrapper;
import org.provim.animatedmobs.api.model.*;

public class AnimationComponent extends ComponentBase {
    private final Animation currentAnimation = new Animation(1);
    private final Animation extraAnimation = new Animation(2);

    public AnimationComponent(AjModel model) {
        super(model);
    }

    public void scheduleAnimation(String name) {
        this.scheduleAnimation(name, this.currentAnimation);
    }

    public void scheduleExtraAnimation(String name) {
        this.scheduleAnimation(name, this.extraAnimation);
    }

    private void scheduleAnimation(String name, Animation anim) {
        AjAnimation animation = this.model.animations().get(name);
        if (anim.frameCounter >= 0 && !anim.frozen) {
            anim.schedule(animation);
        } else {
            anim.set(animation);
        }
    }

    public void setCurrentAnimation(String name) {
        AjAnimation anim = this.model.animations().get(name);
        this.currentAnimation.set(anim);
    }

    public void setExtraAnimation(String name) {
        AjAnimation anim = this.model.animations().get(name);
        this.extraAnimation.set(anim);
    }

    @NotNull
    public AjPose findCurrentAnimationPose(DisplayWrapper<?> display) {
        AjNode node = display.node();

        // Extra animations
        Animation anim = this.extraAnimation;
        int counter = anim.frameCounter;
        AjAnimation extraAnim = anim.currentAnim;
        boolean hasExtraAnimation = extraAnim != null && counter > 0;

        AjPose frozenExtraPose = null;
        if (hasExtraAnimation) {
            AjPose pose = this.findAnimationPose(node, extraAnim, counter);
            if (pose != null) {
                if (!anim.frozen) {
                    display.setAnimationPose(pose, extraAnim);
                    return pose;
                } else {
                    frozenExtraPose = pose;
                }
            }
        }

        // Regular animations
        anim = this.currentAnimation;
        counter = anim.frameCounter;
        AjAnimation regularAnim = anim.currentAnim;
        boolean hasRegularAnimation = regularAnim != null && counter > 0;

        AjPose defaultPose = display.getDefaultPose();
        if (!hasExtraAnimation && !hasRegularAnimation) {
            display.setAnimationPose(defaultPose, regularAnim);
            return defaultPose;
        }

        if (hasRegularAnimation) {
            AjPose pose = this.findAnimationPose(node, regularAnim, counter);
            if (pose != null) {
                display.setAnimationPose(pose, regularAnim);
                return pose;
            }
        }

        if (frozenExtraPose != null) {
            display.setAnimationPose(frozenExtraPose, extraAnim);
            return frozenExtraPose;
        }

        return display.getLastAnimationPose(regularAnim, extraAnim);
    }

    @Nullable
    private AjPose findAnimationPose(AjNode node, AjAnimation current, int counter) {
        if (current.isAffected(node.name())) {
            int index = current.length() - counter;
            AjFrame frame = current.frames()[index];
            return frame.poses().get(node.uuid());
        }
        return null;
    }

    public void tickAnimations() {
        this.currentAnimation.tick();
        this.extraAnimation.tick();
    }

    private static class Animation {
        private final int speed;

        @Nullable
        private AjAnimation currentAnim;
        @Nullable
        private AjAnimation scheduledAnim;

        private int frameCounter = -1;
        private boolean frozen;

        public Animation(int speed) {
            this.speed = speed;
        }

        private void tick() {
            if (this.frameCounter >= 0 && !this.frozen) {
                this.frameCounter -= this.speed;
                if (this.frameCounter <= 0 && this.currentAnim != null) {
                    this.onFinish(this.currentAnim);
                }
            }
        }

        private void onFinish(AjAnimation current) {
            AjAnimation next = this.scheduledAnim;
            if (next != null && next != current) {
                this.set(next);
                return;
            }

            switch (current.loopMode()) {
                case once -> {
                    // play the animation once, and then reset to the first frame.
                    this.frozen = true;
                    this.frameCounter = current.length() - 1;
                }
                case hold -> {
                    // play the animation once, and then hold on the last frame.
                    this.frozen = true;
                    this.frameCounter = 1;
                }
                case loop -> {
                    // todo: implement loop delay
                    this.frameCounter = current.length() - 1;
                }
            }
        }

        private void schedule(AjAnimation anim) {
            if (this.scheduledAnim == anim) {
                return;
            }

            this.scheduledAnim = anim;
        }

        private void set(AjAnimation anim) {
            if (this.currentAnim == anim && !this.frozen) {
                return;
            }

            this.scheduledAnim = null;
            this.currentAnim = anim;
            this.frameCounter = anim != null ? anim.length() - 1 : -1;
            this.frozen = false;
        }
    }
}
