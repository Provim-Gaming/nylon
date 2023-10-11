package org.provim.nylon.entities.holders.living;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.provim.nylon.entities.holders.base.AbstractAjHolder;
import org.provim.nylon.entities.holders.elements.Bone;
import org.provim.nylon.entities.holders.elements.CollisionElement;
import org.provim.nylon.entities.holders.elements.DisplayWrapper;
import org.provim.nylon.entities.holders.elements.LocatorDisplay;
import org.provim.nylon.mixins.accessors.LivingEntityAccessor;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjPose;
import org.provim.nylon.util.Utils;

import java.util.function.Consumer;

public class LivingAjHolder extends AbstractAjHolder<LivingEntity> {
    private final InteractionElement hitboxInteraction;
    private final CollisionElement collisionElement;
    private final Vector2f scaledSize;
    private float deathAngle;

    private float scale;
    private int effectColor;
    private boolean isGlowing;
    private boolean isInvisible;
    private boolean displayFire;

    public LivingAjHolder(LivingEntity parent, AjModel model) {
        this(parent, model, false);
    }

    public LivingAjHolder(LivingEntity parent, AjModel model, boolean updateElementsAsync) {
        super(parent, model, updateElementsAsync);
        this.scaledSize = new Vector2f(this.size);

        this.hitboxInteraction = InteractionElement.redirect(parent);
        this.addElement(this.hitboxInteraction);

        this.collisionElement = CollisionElement.createWithRedirect(parent);
        this.addElement(this.collisionElement);
    }

    @Override
    public void onEntityDataLoaded() {
        this.updateScale(this.parent.getScale());
        super.onEntityDataLoaded();
    }

    @Override
    public void applyPose(AjPose pose, DisplayWrapper<?> display) {
        Quaternionf bodyRotation = Axis.YP.rotationDegrees(-Mth.rotLerp(1.f, this.parent.yBodyRotO, this.parent.yBodyRot));
        if (this.parent.deathTime > 0) {
            bodyRotation.mul(Axis.ZP.rotation(-this.deathAngle * Mth.HALF_PI));
        }

        Vector3f scale = pose.scale();
        Vector3f translation = pose.translation().rotate(bodyRotation);
        if (this.scale != 1.0f) {
            translation.mul(this.scale);
            scale.mul(this.scale);
        }
        translation.add(0, -this.scaledSize.y + 0.01f, 0);

        Quaternionf rightRotation = pose.rotation().mul(Axis.YP.rotationDegrees(180.f)).normalize();
        if (display.isHead()) {
            bodyRotation.mul(Axis.YP.rotation((float) -Math.toRadians(Mth.rotLerp(0.5f, this.parent.yHeadRotO - this.parent.yBodyRotO, this.parent.yHeadRot - this.parent.yBodyRot))));
            bodyRotation.mul(Axis.XP.rotation((float) Math.toRadians(Mth.rotLerp(0.5f, this.parent.getXRot(), this.parent.xRotO))));
        }

        // Update data tracker values
        display.setTranslation(translation);
        display.setRightRotation(rightRotation);
        display.setScale(scale);
        display.setLeftRotation(bodyRotation);

        display.startInterpolation();
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        for (var packet : Utils.updateClientInteraction(this.hitboxInteraction, this.scaledSize)) {
            consumer.accept(packet);
        }

        if (this.parent.canBreatheUnderwater()) {
            consumer.accept(new ClientboundUpdateMobEffectPacket(this.collisionElement.getEntityId(), new MobEffectInstance(MobEffects.WATER_BREATHING, -1, 0, false, false)));
        }

        IntList passengers = new IntArrayList();
        this.addDirectPassengers(passengers);

        consumer.accept(VirtualEntityUtils.createRidePacket(this.parent.getId(), passengers));
        consumer.accept(new ClientboundSetPassengersPacket(this.parent));
    }

    protected void addDirectPassengers(IntList passengers) {
        passengers.add(this.hitboxInteraction.getEntityId());
        passengers.add(this.collisionElement.getEntityId());
    }

    @Override
    public int getDisplayVehicleId() {
        return this.hitboxInteraction.getEntityId();
    }

    @Override
    public int getVehicleId() {
        return this.hitboxInteraction.getEntityId();
    }

    @Override
    public int getLeashedId() {
        return this.collisionElement.getEntityId();
    }

    @Override
    public void updateElements() {
        if (this.parent.deathTime > 0) {
            this.deathAngle = Math.min((float) Math.sqrt((this.parent.deathTime) / 20.0F * 1.6F), 1.f);
        }

        this.updateTrackedData();
        super.updateElements();
    }

    @Override
    protected void updateElement(DisplayWrapper<?> display) {
        AjPose pose = this.animation.firstPose(display);
        if (pose == null) {
            // we always need a valid pose for body rotation & head rotation
            this.applyPose(display.getDefaultPose(), display);
        } else {
            this.applyPose(pose, display);
        }
    }

    private void updateTrackedData() {
        boolean displayFire = !this.parent.fireImmune() && (this.parent.getRemainingFireTicks() > 0 || Utils.hasVisualFire(this.parent));
        if (displayFire != this.displayFire) {
            this.updateFire(displayFire);
        }

        boolean isGlowing = this.parent.isCurrentlyGlowing();
        if (isGlowing != this.isGlowing) {
            this.updateGlow(isGlowing);
        }

        boolean isInvisible = this.parent.isInvisible();
        if (isInvisible != this.isInvisible) {
            this.updateInvisibility(isInvisible);
        }

        int effectColor = this.parent.getEntityData().get(LivingEntityAccessor.getDATA_EFFECT_COLOR_ID());
        if (effectColor != this.effectColor) {
            this.updateEffectColor(effectColor);
        }

        float scale = this.parent.getScale();
        if (scale != this.scale) {
            this.updateScale(scale);
            this.sendScaleUpdate();
        }
    }

    protected void sendScaleUpdate() {
        this.sendPacket(new ClientboundBundlePacket(Utils.updateClientInteraction(this.hitboxInteraction, this.scaledSize)));
    }

    protected void updateFire(boolean displayFire) {
        this.displayFire = displayFire;
        this.hitboxInteraction.setOnFire(displayFire);
    }

    protected void updateGlow(boolean isGlowing) {
        this.isGlowing = isGlowing;
        for (Bone bone : this.bones) {
            bone.element().setGlowing(isGlowing);
        }

        for (LocatorDisplay locator : this.activeLocators) {
            locator.element().setGlowing(isGlowing);
        }
    }

    protected void updateInvisibility(boolean isInvisible) {
        this.isInvisible = isInvisible;
        this.hitboxInteraction.setInvisible(isInvisible);
        for (Bone bone : this.bones) {
            bone.setInvisible(isInvisible);
        }
    }

    protected void updateEffectColor(int effectColor) {
        this.effectColor = effectColor;
        this.collisionElement.getDataTracker().set(LivingEntityAccessor.getDATA_EFFECT_COLOR_ID(), effectColor);
    }

    protected void updateScale(float scale) {
        this.scale = scale;
        this.size.mul(this.scale, this.scaledSize);
        this.collisionElement.setSize(Utils.toSlimeSize(Math.min(this.scaledSize.x, this.scaledSize.y)));
        for (Bone bone : this.bones) {
            bone.element().setDisplaySize(this.scaledSize.x * 2, -this.scaledSize.y - 1);
        }
    }
}
