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

package org.provim.nylon.component;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.api.Animator;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.wrappers.AbstractWrapper;
import org.provim.nylon.model.AjAnimation;
import org.provim.nylon.model.AjFrame;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjPose;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntConsumer;

public class AnimationComponent extends ComponentBase implements Animator {
    private final Object2ObjectOpenHashMap<String, Animation> animationMap = new Object2ObjectOpenHashMap<>();
    private final CopyOnWriteArrayList<Animation> animationList = new CopyOnWriteArrayList<>();

    public AnimationComponent(AjModel model, AbstractAjHolder holder) {
        super(model, holder);
    }

    @Override
    public void playAnimation(String name, int priority, boolean restartPaused, IntConsumer onFrame, Runnable onFinish) {
        Animation animation = this.animationMap.get(name);
        if (priority < 0) {
            priority = 0;
        }

        if (animation == null) {
            AjAnimation anim = this.model.animations().get(name);
            if (anim != null) {
                this.addAnimation(new Animation(name, anim, this.holder, priority, onFrame, onFinish));
            }
        } else {
            // Update values of the existing animation.
            animation.onFrameCallback = onFrame;
            animation.onFinishCallback = onFinish;

            if (animation.state == Animation.State.PAUSED) {
                if (restartPaused) {
                    animation.resetFrameCounter(false);
                }
                animation.state = Animation.State.PLAYING;
            }

            if (priority != animation.priority) {
                animation.priority = priority;
                Collections.sort(this.animationList);
            }
        }
    }

    @Override
    public void setAnimationFrame(String name, int frame) {
        Animation animation = this.animationMap.get(name);
        if (animation != null) {
            animation.skipToFrame(frame);
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
        Animation animation = this.animationMap.remove(name);
        if (animation != null) {
            this.animationList.remove(animation);
        }
    }

    private void addAnimation(Animation animation) {
        this.animationMap.put(animation.name, animation);

        if (this.animationList.size() > 0 && animation.priority > 0) {
            int index = Collections.binarySearch(this.animationList, animation);
            this.animationList.add(index < 0 ? -index - 1 : index, animation);
        } else {
            this.animationList.add(animation);
        }
    }

    public void tickAnimations() {
        for (int index = this.animationList.size() - 1; index >= 0; index--) {
            Animation animation = this.animationList.get(index);
            if (animation.hasFinished()) {
                this.animationMap.remove(animation.name);
                this.animationList.remove(index);
                animation.onFinished();
            } else {
                animation.tick();
            }
        }
    }

    @Nullable
    public AjPose findPose(AbstractWrapper wrapper) {
        UUID uuid = wrapper.node().uuid();
        AjPose pose = null;

        for (Animation animation : this.animationList) {
            if (this.canAnimationAffect(animation, uuid)) {
                if (animation.inResetState()) {
                    pose = wrapper.getDefaultPose();
                } else {
                    pose = this.findAnimationPose(wrapper, animation, uuid);
                    if (pose != null) {
                        return pose;
                    }
                }
            }
        }

        if (pose != null) {
            wrapper.setLastPose(pose, null);
        }

        return pose;
    }

    private boolean canAnimationAffect(Animation anim, UUID uuid) {
        final boolean canAnimate = anim.inResetState() || anim.shouldAnimate();
        return canAnimate && anim.animation.isAffected(uuid);
    }

    @Nullable
    private AjPose findAnimationPose(AbstractWrapper wrapper, Animation anim, UUID uuid) {
        AjAnimation animation = anim.animation;
        AjFrame frame = anim.currentFrame;
        if (frame == null) {
            return null;
        }

        AjPose pose = frame.poses().get(uuid);
        if (pose != null) {
            wrapper.setLastPose(pose, animation);
            return pose;
        }

        if (animation == wrapper.getLastAnimation()) {
            return wrapper.getLastPose();
        }

        // Since the animation just switched, the last known pose is no longer valid.
        // To ensure that this node still gets updated properly, we must backtrack the new animation to find a valid pose.
        // This should preferably be avoided as much as possible, as it is a bit expensive.
        final AjFrame[] frames = animation.frames();
        final int startIndex = (frames.length - 1) - Math.max(anim.frameCounter - 1, 0);

        for (int i = startIndex; i >= 0; i--) {
            pose = frames[i].poses().get(uuid);
            if (pose != null) {
                wrapper.setLastPose(pose, animation);
                return pose;
            }
        }
        return null;
    }

    private static class Animation implements Comparable<Animation> {
        @NotNull
        private final AjAnimation animation;
        private final AbstractAjHolder holder;
        private final String name;

        private AjFrame currentFrame;
        private int frameCounter = -1;
        private int priority;
        private boolean looped;
        private State state;

        @Nullable
        private IntConsumer onFrameCallback;
        @Nullable
        private Runnable onFinishCallback;

        private Animation(String name, @NotNull AjAnimation animation, AbstractAjHolder holder, int priority, @Nullable IntConsumer onFrame, @Nullable Runnable onFinish) {
            this.name = name;
            this.holder = holder;
            this.animation = animation;
            this.state = State.PLAYING;
            this.priority = priority;
            this.onFrameCallback = onFrame;
            this.onFinishCallback = onFinish;
            this.resetFrameCounter(false);
        }

        private void onFinished() {
            if (this.onFinishCallback != null) {
                this.onFinishCallback.run();
            }
        }

        private void tick() {
            if (this.frameCounter < 0) {
                this.onFramesFinished();
                return;
            }

            if (this.shouldAnimate()) {
                this.updateFrame();
                this.frameCounter--;
            }
        }

        private void updateFrame() {
            AjFrame[] frames = this.animation.frames();
            if (this.frameCounter >= 0 && this.frameCounter < frames.length) {
                int index = (frames.length - 1) - this.frameCounter;
                this.currentFrame = frames[index];

                if (this.onFrameCallback != null) {
                    this.onFrameCallback.accept(index);
                }

                if (this.currentFrame.requiresUpdates()) {
                    this.currentFrame.run(this.holder);
                }
            }
        }

        private void skipToFrame(int frame) {
            this.frameCounter = this.animation.duration() - 1 - frame;
        }

        private void resetFrameCounter(boolean isLooping) {
            this.frameCounter = this.animation.duration() - 1 + (isLooping ? this.animation.loopDelay() : this.animation.startDelay());
        }

        private void onFramesFinished() {
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
                    this.resetFrameCounter(true);
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

        public boolean inResetState() {
            return this.state == State.FINISHED_RESET_DEFAULT;
        }

        public boolean hasFinished() {
            return this.state == State.FINISHED;
        }

        public boolean shouldAnimate() {
            return this.state != State.PAUSED && this.state != State.FINISHED && !this.inLoopDelay() && !this.inStartDelay();
        }

        @Override
        public int compareTo(@NotNull Animation other) {
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
