package org.provim.nylon.util;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.provim.nylon.holders.wrapper.Locator;

/**
 * Listener for locators, updates a single DisplayElement
 */
public class EntityRotatedElementUpdateListener extends ElementUpdateListener implements Locator.LocatorListener {
    private final Entity parent;
    private final boolean head;

    public static EntityRotatedElementUpdateListener of(DisplayElement element, Entity parent) {
        return new EntityRotatedElementUpdateListener(element, parent, false);
    }

    public static EntityRotatedElementUpdateListener of(DisplayElement element, Entity parent, boolean head) {
        return new EntityRotatedElementUpdateListener(element, parent, head);
    }

    public EntityRotatedElementUpdateListener(DisplayElement element, Entity parent, boolean head) {
        super(element);
        this.parent = parent;
        this.head = head;
    }

    @Override
    public void update(Vector3f position, Quaternionf rotation) {
        super.update(position, rotation);
        this.element.setYaw(this.parent instanceof LivingEntity livingEntity ? (this.head ? Mth.rotLerp(0.5f, livingEntity.yHeadRotO, livingEntity.yHeadRot) : livingEntity.yBodyRot) : this.parent.getYRot());
    }
}