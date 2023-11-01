package org.provim.nylon.api;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface Animator {

    /**
     * Starts playing an animation on the model.
     */
    default void playAnimation(String name) {
        this.playAnimation(name, 0, false, null);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param priority: The priority of the animation.
     */
    default void playAnimation(String name, int priority) {
        this.playAnimation(name, priority, false, null);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param restartPaused: Whether to restart paused animations, rather than resuming them where they left off.
     */
    default void playAnimation(String name, boolean restartPaused) {
        this.playAnimation(name, 0, restartPaused, null);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param onFinished: Callback to be executed on the last frame of the animation.
     */
    default void playAnimation(String name, @Nullable Runnable onFinished) {
        this.playAnimation(name, 0, false, onFinished);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param priority:      The priority of the animation.
     * @param restartPaused: Whether to restart paused animations, rather than resuming them where they left off.
     */
    default void playAnimation(String name, int priority, boolean restartPaused) {
        this.playAnimation(name, priority, restartPaused, null);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param priority:   The priority of the animation.
     * @param onFinished: Callback to be executed on the last frame of the animation.
     */
    default void playAnimation(String name, int priority, @Nullable Runnable onFinished) {
        this.playAnimation(name, priority, false, onFinished);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param onFinished:    Callback to be executed on the last frame of the animation.
     * @param restartPaused: Whether to restart paused animations, rather than resuming them where they left off.
     */
    default void playAnimation(String name, boolean restartPaused, @Nullable Runnable onFinished) {
        this.playAnimation(name, 0, restartPaused, onFinished);
    }

    /**
     * Starts playing an animation on the model.
     *
     * @param priority:      The priority of the animation.
     * @param onFinished:    Callback to be executed on the last frame of the animation.
     * @param restartPaused: Whether to restart paused animations, rather than resuming them where they left off.
     */
    void playAnimation(String name, int priority, boolean restartPaused, @Nullable Runnable onFinished);

    /**
     * Pauses the animation with the given name.
     */
    void pauseAnimation(String name);

    /**
     * Stops the animation with the given name.
     */
    void stopAnimation(String name);
}
