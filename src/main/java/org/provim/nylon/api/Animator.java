package org.provim.nylon.api;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface Animator {

    /**
     * Starts playing an animation on the model.
     */
    default void playAnimation(String name) {
        this.playAnimation(name, 1, 0, null);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param speed: The animation speed.
     */
    default void playAnimation(String name, int speed) {
        this.playAnimation(name, speed, 0, null);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param speed:    The animation speed.
     * @param priority: The priority of the animation.
     */
    default void playAnimation(String name, int speed, int priority) {
        this.playAnimation(name, speed, priority, null);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param onFinished: Callback to be executed on the last frame of the animation.
     */
    default void playAnimation(String name, @Nullable Runnable onFinished) {
        this.playAnimation(name, 1, 0, onFinished);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param speed:      The animation speed.
     * @param onFinished: Callback to be executed on the last frame of the animation.
     */
    default void playAnimation(String name, int speed, @Nullable Runnable onFinished) {
        this.playAnimation(name, speed, 0, onFinished);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param speed:      The animation speed.
     * @param priority:   The priority of the animation.
     * @param onFinished: Callback to be executed on the last frame of the animation.
     */
    void playAnimation(String name, int speed, int priority, @Nullable Runnable onFinished);

    /**
     * Pauses the current animation with the given name.
     * The animation can be continued using `runAnimation`.
     */
    void pauseAnimation(String name);

    /**
     * Stops the current animation with the given name.
     */
    void stopAnimation(String name);
}
