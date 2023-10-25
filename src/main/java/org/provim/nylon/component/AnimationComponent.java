package org.provim.nylon.component;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.api.Animator;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.wrappers.AbstractWrapper;
import org.provim.nylon.model.*;

import java.util.Collections;

public class AnimationComponent extends ComponentBase implements Animator {
    private final Object2ObjectOpenHashMap<String, Animation> animationMap = new Object2ObjectOpenHashMap<>();
    private final ObjectArrayList<Animation> animationList = new ObjectArrayList<>();
    private final ObjectArrayList<String> toRemove = new ObjectArrayList<>();

    public AnimationComponent(AjModel model, AbstractAjHolder<?> holder) {
        super(model, holder);
    }

    @Override
    public void playAnimation(String name, int priority, Runnable onFinished) {
        Animation animation = this.animationMap.get(name);

        if (animation == null) {
            AjAnimation anim = this.model.animations().get(name);
            if (anim != null) {
                animation = new Animation(name, anim, this.holder, priority);
                this.addAnimationInternal(name, animation);
            }
        } else if (animation.state == Animation.State.PAUSED) {
            animation.state = Animation.State.PLAYING;
        }

        if (animation != null) {
            animation.setOnFinishedCallback(onFinished);
        }
    }

    @Override
    public void pauseAnimation(String name) {
        Animation animation = this.animationMap.get(name);
        if (animation != null && animation.state == Animation.State.PLAYING) {
            animation.state = Animation.State.PAUSED;
        }
    }

    @Override
    public void stopAnimation(String name) {
        this.removeAnimationInternal(name);
    }

    private void addAnimationInternal(String name, Animation animation) {
        this.animationMap.put(name, animation);

        if (this.animationList.size() > 0 && animation.priority > 0) {
            int index = Collections.binarySearch(this.animationList, animation);
            this.animationList.add(index < 0 ? -index - 1 : index, animation);
        } else {
            this.animationList.add(animation);
        }
    }

    private void removeAnimationInternal(String name) {
        Animation anim = this.animationMap.remove(name);
        this.animationList.remove(anim);
    }

    public void tickAnimations() {
        this.toRemove.clear();

        for (int i = this.animationList.size() - 1; i >= 0; i--) {
            Animation animation = this.animationList.get(i);
            if (animation.hasFinished()) {
                animation.onFinished();
                this.toRemove.add(animation.name);
            } else {
                animation.tick();
            }
        }

        for (int i = 0; i < this.toRemove.size(); i++) {
            this.removeAnimationInternal(this.toRemove.get(i));
        }
    }

    @Nullable
    public AjPose findPose(AbstractWrapper wrapper) {
        AjPose pose = null;

        for (int i = 0; i < this.animationList.size(); i++) {
            Animation animation = this.animationList.get(i);
            if (animation.inResetState()) {
                pose = wrapper.getDefaultPose();
            } else if (animation.shouldAnimate()) {
                AjPose animationPose = this.findAnimationPose(wrapper, animation);
                if (animationPose != null) {
                    return animationPose;
                }
            }
        }

        return pose;
    }

    @Nullable
    private AjPose findAnimationPose(AbstractWrapper wrapper, Animation anim) {
        AjNode node = wrapper.node();
        AjAnimation animation = anim.animation;
        AjFrame frame = anim.currentFrame;
        if (frame == null || !animation.isAffected(node.name())) {
            return null;
        }

        AjPose pose = frame.poses().get(node.uuid());
        if (pose != null) {
            wrapper.setLastPose(pose, animation);
            return pose;
        }

        return wrapper.getLastPose(animation);
    }

    private static class Animation implements Comparable<Animation> {
        @NotNull
        private final AjAnimation animation;
        private final AbstractAjHolder<?> holder;
        private final String name;
        private final int priority;

        private AjFrame currentFrame;
        private int frameCounter = -1;
        private boolean looped;
        private State state;
        private Runnable onFinishedCallback;

        private Animation(String name, AjAnimation animation, AbstractAjHolder<?> holder, int priority) {
            this.name = name;
            this.holder = holder;
            this.animation = animation;
            this.priority = Math.max(0, priority);
            this.state = State.PLAYING;
            this.updateFrame(animation.duration() - 1 + animation.startDelay());
        }

        public boolean inResetState() {
            return this.state == State.FINISHED_RESET_DEFAULT;
        }

        public void setOnFinishedCallback(Runnable onFinishedCallback) {
            this.onFinishedCallback = onFinishedCallback;
        }

        public void onFinished() {
            if (this.onFinishedCallback != null) {
                this.onFinishedCallback.run();
            }
        }

        private void tick() {
            if (this.frameCounter >= 0 && this.shouldAnimate()) {
                this.updateFrame(this.frameCounter - 1);
            }

            if (this.frameCounter < 0) {
                this.onFinish();
            }
        }

        private void updateFrame(int frame) {
            if (frame != this.frameCounter) {
                this.frameCounter = frame;

                AjFrame[] frames = this.animation.frames();
                if (frame >= 0 && frame < frames.length) {
                    this.currentFrame = frames[(frames.length - 1) - frame];

                    if (this.currentFrame.requiresUpdates()) {
                        this.currentFrame.run(this.holder);
                    }
                }
            }
        }

        private void onFinish() {
            switch (this.animation.loopMode()) {
                case once -> {
                    if (this.state == State.FINISHED_RESET_DEFAULT) {
                        this.state = State.FINISHED;
                    } else {
                        this.state = State.FINISHED_RESET_DEFAULT;
                    }
                }
                case hold -> {
                    this.state = State.FINISHED;
                }
                case loop -> {
                    this.updateFrame(this.animation.duration() - 1 + this.animation.loopDelay());
                    this.looped = true;
                }
            }
        }

        private boolean inLoopDelay() {
            return this.animation.loopDelay() > 0 && this.looped && this.frameCounter >= this.animation.duration() - this.animation.loopDelay();
        }

        private boolean inStartDelay() {
            return this.animation.startDelay() > 0 && this.frameCounter >= this.animation.duration() - (this.looped ? 0 : this.animation.startDelay());
        }

        public boolean hasFinished() {
            return this.state == State.FINISHED;
        }

        public boolean shouldAnimate() {
            return this.state != State.PAUSED && this.state != State.FINISHED && !this.inLoopDelay() && !this.inStartDelay();
        }

        @Override
        public int compareTo(@NotNull AnimationComponent.Animation other) {
            return Integer.compare(other.priority, this.priority);
        }

        private enum State {
            PLAYING,
            PAUSED,
            FINISHED_RESET_DEFAULT,
            FINISHED,
        }
    }
}
