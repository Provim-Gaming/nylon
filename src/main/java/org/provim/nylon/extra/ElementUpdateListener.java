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
    protected static final float COS = Mth.cos(90F * Mth.DEG_TO_RAD);

    protected final GenericEntityElement element;

    public ElementUpdateListener(GenericEntityElement element) {
        this.element = element;
    }

    @Override
    public void update(AbstractAjHolder<?> holder, AjPose pose) {
        if (this.element.isSendingPositionUpdates()) {
            Entity parent = holder.getParent();
            Vector3f offset = pose.translation();
            float yRot = parent.getYRot();

            this.rotateOffset(offset, yRot);
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

    private void rotateOffset(Vector3f offset, float yRot) {
        float radians = (yRot + 90F) * Mth.DEG_TO_RAD;
        float cos = Mth.cos(radians);
        float sin = Mth.sin(radians);

        Vector3f vector = new Vector3f(cos, 0, sin);
        Vector3f vector2 = new Vector3f(cos * COS, 1, sin * COS);
        vector.cross(vector2).negate();

        float offsetX = cos * offset.z + vector2.x * offset.y + vector.x * offset.x;
        float offsetZ = sin * offset.z + vector2.z * offset.y + vector.z * offset.x;

        offset.x = offsetX;
        offset.z = offsetZ;
    }
}