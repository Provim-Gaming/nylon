package org.provim.nylon.extra;

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.wrappers.Locator;
import org.provim.nylon.model.AjPose;

/**
 * Listener for locators, updates a single GenericEntityElement
 */
public class ElementUpdateListener implements Locator.LocatorListener {
    protected final GenericEntityElement element;

    public ElementUpdateListener(GenericEntityElement element) {
        this.element = element;
    }

    @Override
    public void update(AbstractAjHolder<?> holder, AjPose pose) {
        if (this.element.isSendingPositionUpdates()) {
            Entity parent = holder.getParent();
            float yRot = parent.getYRot();
            float angle = yRot * Mth.DEG_TO_RAD;

            Vector3f offset = pose.translation().rotateY(-angle);
            Vec3 pos = holder.getPos().add(offset.x, offset.y, offset.z);

            holder.sendPacket(VirtualEntityUtils.createMovePacket(
                    this.element.getEntityId(),
                    pos,
                    pos,
                    true,
                    yRot,
                    0F
            ));
        }
    }
}