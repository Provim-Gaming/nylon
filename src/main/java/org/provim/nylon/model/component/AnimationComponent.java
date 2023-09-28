package org.provim.nylon.model.component;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.entities.holders.elements.DisplayWrapper;
import org.provim.nylon.model.*;

import java.util.Map;

public class AnimationComponent extends ComponentBase {
    private final Object2ObjectLinkedOpenHashMap<AjAnimation, Animation> animationList = new Object2ObjectLinkedOpenHashMap<>();

    public AnimationComponent(AjModel model) {
        super(model);
    }

    public Animation playAnimation(String name, Runnable onFinished) {
        return this.playAnimation(name, 1, onFinished);
    }

    public Animation playAnimation(String name, int speed, Runnable onFinished) {
        AjAnimation anim = this.model.animations().get(name);
        if (anim != null && !this.animationList.containsKey(anim)) {
            this.animationList.put(anim, new Animation(anim, speed));
        } else if (this.animationList.containsKey(anim) && this.animationList.get(anim).state == Animation.State.PAUSED) {
            this.animationList.get(anim).state = Animation.State.PLAYING;
        }

        this.animationList.get(anim).setOnFinishedCB(onFinished);

        return this.animationList.get(anim);
    }

    public Animation pauseAnimation(String name) {
        AjAnimation anim = this.model.animations().get(name);
        boolean contains = this.animationList.containsKey(anim);
        if (anim != null && !contains || contains && this.animationList.get(anim).state != Animation.State.PLAYING)
            return null;

        Animation animation = this.animationList.get(anim);
        animation.state = Animation.State.PAUSED;
        return animation;
    }

    public Animation stopAnimation(String name) {
        return this.animationList.remove(this.model.animations().get(name));
    }

    @Nullable
    public AjPose firstPose(DisplayWrapper<?> display) {
        AjNode node = display.node();
        AjPose pose = null;

        for (Map.Entry<AjAnimation, Animation> entry : this.animationList.entrySet()) {
            if (entry.getValue().inResetState()) {
                pose = display.getDefaultPose();
            } else if (entry.getValue().shouldAnimate()) {
                AjPose pose2 = this.findAnimationPose(node, entry.getKey(), entry.getValue().frameCounter);
                if (pose2 != null) {
                    pose = pose2;
                }
            }
        }

        return pose;
    }

    @Nullable
    private AjPose findAnimationPose(AjNode node, AjAnimation current, int counter) {
        if (current.isAffected(node.name())) {
            int index = (current.duration() - 1) - Math.max(counter, 0);
            AjFrame frame = current.frames()[index];
            return frame.poses().get(node.uuid());
        }
        return null;
    }

    public void tickAnimations() {
        this.animationList.entrySet().removeIf(entry -> entry.getValue().hasFinished());
        this.animationList.forEach((key, animation) -> animation.tick());
    }

    private static class Animation {
        enum State {
            PLAYING,
            PAUSED,
            FINISHED_RESET_DEFAULT,
            FINISHED,
        }

        @NotNull
        private final AjAnimation animation;
        private int frameCounter;
        private final int speed;
        private State state;

        private boolean looped = false;

        private Runnable onFinishedCB = null;

        public Animation(AjAnimation animation, int speed) {
            this.animation = animation;
            this.frameCounter = this.animation.duration() - 1 + animation.startDelay();
            this.speed = speed;
            this.state = State.PLAYING;
        }

        public boolean inResetState() {
            return this.state == State.FINISHED_RESET_DEFAULT;
        }

        public void setOnFinishedCB(Runnable onFinishedCB) {
            this.onFinishedCB = onFinishedCB;
        }

        private void tick() {
            if (this.frameCounter + speed >= 0 && this.shouldAnimate()) {
                this.frameCounter -= speed;
                if (this.frameCounter < 0) {
                    this.onFinish();
                }
            }
        }

        private void onFinish() {
            switch (animation.loopMode()) {
                // todo: reset to "first frame"
                case once -> {
                    // play the animation once, and then reset to the first frame.
                    if (this.state == State.FINISHED_RESET_DEFAULT) {
                        this.state = State.FINISHED;
                    } else {
                        this.state = State.FINISHED_RESET_DEFAULT;
                        this.frameCounter = 1;
                    }
                }
                case hold -> // play the animation once, and then hold on the last frame.
                        this.state = State.FINISHED;
                case loop -> {
                    this.frameCounter = animation.duration() - 1 + animation.loopDelay();
                    this.looped = true;
                }
            }

            if (this.onFinishedCB != null)
                this.onFinishedCB.run();
        }

        private boolean inLoopDelay() {
            return animation.loopDelay() > 0 && looped && this.frameCounter >= animation.duration() - animation.loopDelay();
        }

        private boolean inStartDelay() {
            return animation.startDelay() > 0 && this.frameCounter >= animation.duration() - (looped ? 0 : animation.startDelay());
        }

        public boolean hasFinished() {
            return this.state == State.FINISHED;
        }

        public boolean shouldAnimate() {
            return this.state != State.PAUSED && this.state != State.FINISHED && !this.inLoopDelay() && !this.inStartDelay();
        }
    }
}
