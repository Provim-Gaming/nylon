package org.provim.nylon.entities.holders.elements;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import org.joml.Matrix4f;
import org.provim.nylon.entities.holders.base.AbstractAjHolder;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

import java.util.function.Consumer;

public class LocatorDisplay extends DisplayWrapper<DisplayElement> {
    private final AbstractAjHolder<?> holder;
    private boolean isActive = true;

    private Consumer<Matrix4f> transformationUpdateConsumer;

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

    public void updateActivity(boolean isActive, boolean update) {
        if (this.isActive == isActive) {
            return;
        }

        this.isActive = isActive;
        if (isActive) {
            this.holder.activateLocator(this, update);
        } else {
            this.holder.deactivateLocator(this, update);
        }
    }

    public void setTransformationUpdateConsumer(Consumer<Matrix4f> transformationUpdateConsumer) {
        this.transformationUpdateConsumer = transformationUpdateConsumer;
    }

    public void updateTransformationConsumer() {
        if (this.transformationUpdateConsumer != null) {
            Matrix4f m = new Matrix4f();
            m.translate(this.getTranslation());
            m.rotate(this.getRightRotation().mul(this.getLeftRotation()));
            this.transformationUpdateConsumer.accept(m);
        }
    }
}
