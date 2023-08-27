package org.provim.animatedmobs.api.entities.holders;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.provim.animatedmobs.api.mixins.EntityAccessor;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.component.AnimationComponent;
import org.provim.animatedmobs.api.util.Util;

import java.util.function.Consumer;

public class AjHolderLiving extends AjHolder<LivingEntity> implements AjHolderInterface {
    private final ReferenceOpenHashSet<ItemDisplayElement> headElements = new ReferenceOpenHashSet<>();
    private final InteractionElement hitboxInteraction;
    private final Vector2f scaledSize;
    private float deathAngle;
    private float scale;

    public AjHolderLiving(LivingEntity parent, AjModel model) {
        super(parent, model);
        this.scaledSize = new Vector2f(this.size);

        this.hitboxInteraction = InteractionElement.redirect(parent);
        this.addElement(this.hitboxInteraction);

        model.rig().nodeMap().forEach((key, node) -> {
            if (node.name().startsWith("head")) {
                this.headElements.add(this.itemDisplays.get(node.uuid()));
            }
        });

        if (!this.itemDisplays.isEmpty()) {
            ItemDisplayElement element = this.itemDisplays.values().iterator().next();
            element.setShadowRadius(this.size.x / 2.f);
            element.setShadowStrength(0.8f);
        }
    }

    @Override
    public void onEntityDataLoaded() {
        this.updateScale(this.parent.getScale());
        super.onEntityDataLoaded();
    }

    @Override
    public void applyTransformWithCurrentEntityTransformation(AnimationComponent.AnimationTransform transform, DisplayElement element) {
        Quaternionf bodyRotation = Axis.YP.rotationDegrees(-Mth.rotLerp(1.f, this.parent.yBodyRotO, this.parent.yBodyRot));
        if (this.parent.deathTime > 0) {
            bodyRotation.mul(Axis.ZP.rotation(-this.deathAngle * Mth.HALF_PI));
        }

        Vector3f scale = new Vector3f(transform.scale()).mul(this.scale);
        Vector3f translation = transform.pos().rotate(bodyRotation).mul(this.scale).add(0, -this.scaledSize.y + 0.0125f, 0);
        Quaternionf rightRotation = transform.rot().mul(Axis.YP.rotationDegrees(180.f)).normalize();

        if (this.headElements.contains(element)) {
            bodyRotation.mul(Axis.YP.rotation((float) -Math.toRadians(Mth.rotLerp(0.5f, this.parent.yHeadRotO - this.parent.yBodyRotO, this.parent.yHeadRot - this.parent.yBodyRot))));
            bodyRotation.mul(Axis.XP.rotation((float) Math.toRadians(Mth.rotLerp(0.5f, this.parent.getXRot(), this.parent.xRotO))));
        }

        if (!element.getScale().equals(scale) ||
            !element.getLeftRotation().equals(bodyRotation) ||
            !element.getRightRotation().equals(rightRotation) ||
            !element.getTranslation().equals(translation)
        ) {
            element.startInterpolation();
            element.setInterpolationDuration(2);

            element.setTranslation(translation);
            element.setRightRotation(rightRotation);
            element.setScale(scale);
            element.setLeftRotation(bodyRotation);
        }
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        for (Packet<ClientGamePacketListener> packet : Util.updateClientInteraction(this.hitboxInteraction, this.scaledSize)) {
            consumer.accept(packet);
        }

        if (this.parent.canBreatheUnderwater()) {
            consumer.accept(new ClientboundUpdateMobEffectPacket(this.parent.getId(), new MobEffectInstance(MobEffects.WATER_BREATHING, -1, 0, false, false)));
        }

        consumer.accept(VirtualEntityUtils.createRidePacket(this.parent.getId(), this.hitboxInteraction.getEntityIds()));
        consumer.accept(VirtualEntityUtils.createRidePacket(this.getVehicleId(), this.getDisplayIds()));
    }

    @Override
    public void updateElements() {
        if (this.parent.deathTime > 0) {
            this.deathAngle = Math.min((float) Math.sqrt((this.parent.deathTime) / 20.0F * 1.6F), 1.f);
        }

        boolean isGlowing = this.parent.isCurrentlyGlowing();
        boolean displayFire = !this.parent.fireImmune() && (this.parent.getRemainingFireTicks() > 0 || this.parent instanceof EntityAccessor entity && entity.am_hasVisualFire());

        float scale = this.parent.getScale();
        if (scale != this.scale) {
            this.updateScale(scale);
            this.sendPacket(new ClientboundBundlePacket(Util.updateClientInteraction(this.hitboxInteraction, this.scaledSize)));
        }

        super.updateElements();

        this.hitboxInteraction.setOnFire(displayFire);
        this.itemDisplays.forEach((uuid, element) -> {
            element.setGlowing(isGlowing);
        });

        this.animationComponent.decreaseCounter();
    }

    private void updateScale(float scale) {
        this.scale = scale;
        this.size.mul(this.scale, this.scaledSize);
        for (ItemDisplayElement element : this.itemDisplays.values()) {
            element.setDisplaySize(this.scaledSize.x * 2, -this.scaledSize.y - 1);
        }
    }

    @Override
    public int getVehicleId() {
        return this.hitboxInteraction.getEntityId();
    }
}
