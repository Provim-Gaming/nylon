package org.provim.nylon.holders.elements;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import org.joml.Matrix4f;
import org.provim.nylon.api.Locator;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

import java.util.function.Consumer;

public class LocatorDisplay extends DisplayWrapper<DisplayElement> implements Locator {
    private final AbstractAjHolder<?> holder;
    private Consumer<Matrix4f> transformationUpdateConsumer;
    private boolean isActive = true;

    public static LocatorDisplay of(DisplayElement element, AjNode node, AjPose defaultPose, AbstractAjHolder<?> holder) {
        return new LocatorDisplay(element, node, defaultPose, holder);
    }

    protected LocatorDisplay(DisplayElement element, AjNode node, AjPose defaultPose, AbstractAjHolder<?> holder) {
        super(element, node, defaultPose);
        this.holder = holder;
    }

    @Override
    public boolean isHead() {
        return false;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void updateActivity(boolean isActive, boolean isServerOnly) {
        if (this.isActive == isActive) {
            return;
        }

        this.isActive = isActive;
        if (isActive) {
            this.holder.activateLocator(this, isServerOnly);
        } else {
            this.holder.deactivateLocator(this);
        }
    }

    @Override
    public void setTransformationUpdateConsumer(Consumer<Matrix4f> consumer) {
        this.transformationUpdateConsumer = consumer;
    }

    public void updateTransformationConsumer() {
        if (this.transformationUpdateConsumer != null) {
            Matrix4f matrix = new Matrix4f();
            matrix.translate(this.getTranslation());
            matrix.rotate(this.getRightRotation().mul(this.getLeftRotation()));
            this.transformationUpdateConsumer.accept(matrix);
        }
    }
}
