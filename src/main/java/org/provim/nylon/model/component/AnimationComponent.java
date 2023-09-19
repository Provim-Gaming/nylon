package org.provim.nylon.model.component;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.entities.holders.elements.DisplayWrapper;
import org.provim.nylon.model.*;

import java.util.Map;

public class AnimationComponent extends ComponentBase {
    private final Object2ObjectOpenHashMap<AjAnimation, Animation> animationList = new Object2ObjectOpenHashMap<>();

    public AnimationComponent(AjModel model) {
        super(model);
    }

    public Animation playAnimation(String name) {
        return this.playAnimation(name, null);
    }

    public Animation playAnimation(String name, Runnable onFinished) {
        AjAnimation anim = this.model.animations().get(name);
        if (anim != null && !this.animationList.containsKey(anim)) {
            this.animationList.put(anim, new Animation(anim));
        }
        else if (this.animationList.containsKey(anim)) {
            this.animationList.get(anim).paused = false;
        }

        this.animationList.get(anim).setOnFinishedCB(onFinished);

        return this.animationList.get(anim);
    }

    public Animation pauseAnimation(String name) {
        AjAnimation anim = this.model.animations().get(name);
        if (anim != null && !this.animationList.containsKey(anim))
            return null;

        Animation animation = this.animationList.get(anim);
        animation.paused = true;
        return animation;
    }

    public Animation stopAnimation(String name) {
        return this.animationList.remove(this.model.animations().get(name));
    }

    @Nullable
    public AjPose firstPose(DisplayWrapper<?> display) {
        AjNode node = display.node();
        AjPose pose = display.getDefaultPose();

        for (Map.Entry<AjAnimation,Animation> entry : this.animationList.entrySet()) {
            if (!entry.getValue().paused) {
                pose = display.getLastAnimationPose(entry.getKey());
            }

            if (entry.getValue().canPlay()) {
                AjPose pose2 = this.findAnimationPose(node, entry.getKey(), entry.getValue().frameCounter);
                if (pose2 != null) {
                    pose = pose2;
                    display.setAnimationPose(pose, entry.getKey());
                }
            }
        }

        return pose;
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
        this.animationList.forEach((key,animation) -> animation.tick());
        this.animationList.entrySet().removeIf(entry -> entry.getValue().hasFinished());
    }

    private static class Animation {
        @NotNull
        private AjAnimation animation;
        private int frameCounter;
        private boolean paused;

        private boolean looped = false;

        private boolean finished = false;

        private Runnable onFinishedCB = null;

        public Animation(AjAnimation animation) {
            this.animation = animation;
            this.frameCounter = this.animation.duration() + animation.startDelay();
            this.paused = false;
        }

        public void setOnFinishedCB(Runnable onFinishedCB) {
            this.onFinishedCB = onFinishedCB;
        }

        private void tick() {
            if (this.frameCounter >= 0 && this.canPlay()) {
                if (--this.frameCounter <= 0) {
                    this.onFinish();
                }
            }
        }

        private void onFinish() {
            switch (animation.loopMode()) {
                // todo: reset to "first frame"
                case once -> // play the animation once, and then reset to the first frame.
                        this.finished = true;
                case hold -> // play the animation once, and then hold on the last frame.
                        this.finished = true;
                case loop -> {
                    this.frameCounter = animation.duration()-1 + animation.loopDelay();
                    this.looped = true;
                }
            }

            if (this.onFinishedCB != null)
                this.onFinishedCB.run();
        }

        private boolean inLoopDelay() {
            return animation.loopDelay() > 0 && looped ? this.frameCounter >= animation.duration() - (looped ? 0 : animation.startDelay()) - animation.loopDelay() : false;
        }

        private boolean inStartDelay() {
            return animation.startDelay() > 0 ? this.frameCounter >= animation.duration() - (looped ? 0 : animation.startDelay()) : false;
        }

        public boolean hasFinished() {
            return this.finished;
        }

        public boolean canPlay() {
            return !this.paused && !this.finished && !this.inLoopDelay() && !this.inStartDelay();
        }
    }
}
