package org.provim.nylon.extra;

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.entity.EntityHolder;
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
    public void update(AbstractAjHolder holder, AjPose pose) {
        if (this.element.isSendingPositionUpdates()) {
            if (holder instanceof EntityHolder<?> entityHolder) {
                this.updateEntityBasedHolder(entityHolder, pose);
            } else {
                this.updateNonEntityBasedHolder(holder, pose);
            }
        }
    }

    private void updateEntityBasedHolder(EntityHolder<?> holder, AjPose pose) {
        Entity parent = holder.getParent();
        float scale = holder.getScale();
        float yRot = parent.getYRot();
        float angle = yRot * Mth.DEG_TO_RAD;

        Vector3f offset = pose.translation();
        if (scale != 1F) {
            offset.mul(scale);
        }
        offset.rotateY(-angle);

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

    private void updateNonEntityBasedHolder(AbstractAjHolder holder, AjPose pose) {
        float scale = holder.getScale();
        Vector3fc offset = scale != 1F
                ? pose.translation().mul(scale)
                : pose.readOnlyTranslation();

        Vec3 pos = holder.getPos().add(offset.x(), offset.y(), offset.z());
        holder.sendPacket(VirtualEntityUtils.createMovePacket(
                this.element.getEntityId(),
                pos,
                pos,
                false,
                0F,
                0F
        ));
    }
}