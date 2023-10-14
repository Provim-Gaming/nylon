package org.provim.nylon.component;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.api.Animator;
import org.provim.nylon.holders.elements.DisplayWrapper;
import org.provim.nylon.model.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AnimationComponent extends ComponentBase implements Animator {
    private final Object2ObjectOpenHashMap<String, Animation> animationMap = new Object2ObjectOpenHashMap<>();
    private final List<Animation> animationList;

    public AnimationComponent(AjModel model, MinecraftServer server, boolean async) {
        super(model, server);
        this.animationList = async ? new CopyOnWriteArrayList<>() : new ObjectArrayList<>();
    }

    @Override
    public void playAnimation(String name, int speed, int priority, Runnable onFinished) {
        AjAnimation anim = this.model.animations().get(name);
        Animation animation = this.animationMap.get(name);

        if (anim != null && animation == null) {
            animation = new Animation(name, anim, speed, priority);
            this.addAnimationInternal(name, animation);
        } else if (animation != null && animation.state == Animation.State.PAUSED) {
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
        ObjectArrayList<String> toRemove = new ObjectArrayList<>();
        for (Animation animation : this.animationList) {
            if (animation.hasFinished()) {
                toRemove.add(animation.name);
            } else {
                animation.tick(this.server);
            }
        }

        for (String name : toRemove) {
            this.removeAnimationInternal(name);
        }
    }


    @Nullable
    public AjPose firstPose(DisplayWrapper<?> display) {
        AjPose pose = null;

        for (Animation anim : this.animationList) {
            if (anim.inResetState()) {
                pose = display.getDefaultPose();
            } else if (anim.shouldAnimate()) {
                AjPose animationPose = this.findAnimationPose(display, anim);
                if (animationPose != null) {
                    return animationPose;
                }
            }
        }

        return pose;
    }

    @Nullable
    private AjPose findAnimationPose(DisplayWrapper<?> display, Animation anim) {
        AjNode node = display.node();
        AjAnimation animation = anim.animation;

        if (!animation.isAffected(node.name())) {
            return null;
        }

        AjPose pose = anim.currentFrame.poses().get(node.uuid());
        if (pose != null) {
            display.setLastPose(pose, animation);
            return pose;
        }

        return display.getLastPose(animation);
    }

    private static class Animation implements Comparable<Animation> {
        @NotNull
        private final AjAnimation animation;
        private final String name;
        private final int speed;
        private final int priority;

        private AjFrame currentFrame;
        private int frameCounter;
        private boolean looped;
        private State state;
        private Runnable onFinishedCallback;

        public Animation(String name, AjAnimation animation, int speed, int priority) {
            this.name = name;
            this.animation = animation;
            this.speed = speed;
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

        private void tick(MinecraftServer server) {
            if (this.frameCounter + this.speed >= 0 && this.shouldAnimate()) {
                this.updateFrame(this.frameCounter - this.speed);

                if (this.frameCounter < 0) {
                    this.onFinish(server);
                }
            }
        }

        private void updateFrame(int frame) {
            this.frameCounter = frame;
            this.currentFrame = this.animation.frames()[(this.animation.duration() - 1) - Math.max(frame, 0)];
        }

        private void onFinish(MinecraftServer server) {
            switch (this.animation.loopMode()) {
                case once -> {
                    // todo: reset to "first frame"
                    // play the animation once, and then reset to the first frame.
                    if (this.state == State.FINISHED_RESET_DEFAULT) {
                        this.state = State.FINISHED;
                    } else {
                        this.state = State.FINISHED_RESET_DEFAULT;
                        this.updateFrame(1);
                    }
                }
                case hold -> {
                    // play the animation once, and then hold on the last frame.
                    this.state = State.FINISHED;
                }
                case loop -> {
                    this.updateFrame(this.animation.duration() - 1 + this.animation.loopDelay());
                    this.looped = true;
                }
            }

            if (this.onFinishedCallback != null) {
                server.execute(this.onFinishedCallback);
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
