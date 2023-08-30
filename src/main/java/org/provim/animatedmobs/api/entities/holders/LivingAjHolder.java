package org.provim.animatedmobs.api.entities.holders;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
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
import org.provim.animatedmobs.api.util.Utils;
import org.provim.animatedmobs.api.util.WrappedDisplay;

import java.util.function.Consumer;

public class LivingAjHolder extends AbstractAjHolder<LivingEntity> {
    private final InteractionElement hitboxInteraction;
    private final Vector2f scaledSize;
    private float deathAngle;
    private float scale;
    private boolean isGlowing;
    private boolean displayFire;

    public LivingAjHolder(LivingEntity parent, AjModel model) {
        this(parent, model, false);
    }

    public LivingAjHolder(LivingEntity parent, AjModel model, boolean updateElementsAsync) {
        super(parent, model, updateElementsAsync);
        this.scaledSize = new Vector2f(this.size);

        this.hitboxInteraction = InteractionElement.redirect(parent);
        this.addElement(this.hitboxInteraction);

        if (this.bones.length > 0) {
            ItemDisplayElement element = this.bones[0].element();
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
    public void applyTransformWithCurrentEntityTransformation(AnimationComponent.AnimationTransform transform, WrappedDisplay<?> wrapped) {
        Quaternionf bodyRotation = Axis.YP.rotationDegrees(-Mth.rotLerp(1.f, this.parent.yBodyRotO, this.parent.yBodyRot));
        if (this.parent.deathTime > 0) {
            bodyRotation.mul(Axis.ZP.rotation(-this.deathAngle * Mth.HALF_PI));
        }

        Vector3f scale = transform.scale();
        Vector3f translation = transform.translation().rotate(bodyRotation);
        if (this.scale != 1.0f) {
            translation.mul(this.scale);
            scale.mul(this.scale);
        }
        translation.add(0, -this.scaledSize.y + 0.0125f, 0);

        Quaternionf rightRotation = transform.rot().mul(Axis.YP.rotationDegrees(180.f)).normalize();
        if (wrapped.isHead()) {
            bodyRotation.mul(Axis.YP.rotation((float) -Math.toRadians(Mth.rotLerp(0.5f, this.parent.yHeadRotO - this.parent.yBodyRotO, this.parent.yHeadRot - this.parent.yBodyRot))));
            bodyRotation.mul(Axis.XP.rotation((float) Math.toRadians(Mth.rotLerp(0.5f, this.parent.getXRot(), this.parent.xRotO))));
        }

        // Update data tracker values
        wrapped.setTranslation(translation);
        wrapped.setRightRotation(rightRotation);
        wrapped.setScale(scale);
        wrapped.setLeftRotation(bodyRotation);

        wrapped.startInterpolation();
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        for (Packet<ClientGamePacketListener> packet : Utils.updateClientInteraction(this.hitboxInteraction, this.scaledSize)) {
            consumer.accept(packet);
        }

        if (this.parent.canBreatheUnderwater()) {
            consumer.accept(new ClientboundUpdateMobEffectPacket(this.parent.getId(), new MobEffectInstance(MobEffects.WATER_BREATHING, -1, 0, false, false)));
        }

        consumer.accept(VirtualEntityUtils.createRidePacket(this.parent.getId(), this.hitboxInteraction.getEntityIds()));
        consumer.accept(VirtualEntityUtils.createRidePacket(this.getVehicleId(), this.getDisplayIds()));
    }

    @Override
    public int getVehicleId() {
        return this.hitboxInteraction.getEntityId();
    }

    @Override
    public void updateElements() {
        if (this.parent.deathTime > 0) {
            this.deathAngle = Math.min((float) Math.sqrt((this.parent.deathTime) / 20.0F * 1.6F), 1.f);
        }

        this.updateTrackedData();
        super.updateElements();
    }

    private void updateTrackedData() {
        boolean displayFire = !this.parent.fireImmune() && (this.parent.getRemainingFireTicks() > 0 || this.parent instanceof EntityAccessor entity && entity.am_hasVisualFire());
        if (displayFire != this.displayFire) {
            this.updateFire(displayFire);
        }

        boolean isGlowing = this.parent.isCurrentlyGlowing();
        if (isGlowing != this.isGlowing) {
            this.updateGlow(isGlowing);
        }

        float scale = this.parent.getScale();
        if (scale != this.scale) {
            this.updateScale(scale);
            this.sendPacket(new ClientboundBundlePacket(Utils.updateClientInteraction(this.hitboxInteraction, this.scaledSize)));
        }
    }

    private void updateFire(boolean displayFire) {
        this.displayFire = displayFire;
        this.hitboxInteraction.setOnFire(displayFire);
    }

    private void updateGlow(boolean isGlowing) {
        this.isGlowing = isGlowing;
        for (WrappedDisplay<ItemDisplayElement> wrapped : this.bones) {
            wrapped.element().setGlowing(isGlowing);
        }
    }

    private void updateScale(float scale) {
        this.scale = scale;
        this.size.mul(this.scale, this.scaledSize);
        for (WrappedDisplay<ItemDisplayElement> wrapped : this.bones) {
            wrapped.element().setDisplaySize(this.scaledSize.x * 2, -this.scaledSize.y - 1);
        }
    }
}