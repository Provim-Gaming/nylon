package org.provim.nylon.holders.living;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.holders.wrapper.Bone;
import org.provim.nylon.holders.elements.CollisionElement;
import org.provim.nylon.holders.wrapper.DisplayWrapper;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjPose;
import org.provim.nylon.util.NylonTrackedData;
import org.provim.nylon.util.Utils;

import java.util.function.Consumer;

public class LivingAjHolder<T extends LivingEntity & AjEntity> extends AbstractAjHolder<T> {
    private final InteractionElement hitboxInteraction;
    private final CollisionElement collisionElement;
    private final Vector2f scaledSize;
    private float deathAngle;
    private float scale;

    public LivingAjHolder(T parent, AjModel model) {
        super(parent, model);
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
    protected void onTick() {
        if (this.parent.deathTime > 0) {
            this.deathAngle = Math.min((float) Math.sqrt((this.parent.deathTime) / 20.0F * 1.6F), 1.f);
        }

        float scale = this.parent.getScale();
        if (scale != this.scale) {
            this.updateScale(scale);
            this.sendScaleUpdate();
        }

        super.onTick();
    }

    @Override
    protected void updateElement(DisplayWrapper display) {
        AjPose pose = this.animation.findPose(display);
        if (pose == null) {
            // we always need a valid pose for body rotation & head rotation
            this.applyPose(display.getDefaultPose(), display);
        } else {
            this.applyPose(pose, display);
        }
    }

    @Override
    public void applyPose(AjPose pose, DisplayWrapper display) {
        Quaternionf rightRotation = pose.rotation().mul(ROT_180).normalize();
        Vector3f translation = pose.translation();
        Vector3f scale = pose.scale();

        boolean isHead = display.requiresUpdateEveryTick();
        boolean isDead = this.parent.deathTime > 0;
        if (isHead || isDead) {
            Quaternionf bodyRotation = Axis.ZP.rotation(-this.deathAngle * Mth.HALF_PI);
            if (isDead) {
                translation.rotate(bodyRotation);
            }

            if (isHead) {
                bodyRotation.mul(Axis.YP.rotation((float) -Math.toRadians(Mth.rotLerp(0.5f, this.parent.yHeadRotO - this.parent.yBodyRotO, this.parent.yHeadRot - this.parent.yBodyRot))));
                bodyRotation.mul(Axis.XP.rotation((float) Math.toRadians(Mth.rotLerp(0.5f, this.parent.getXRot(), this.parent.xRotO))));
            }

            display.setLeftRotation(bodyRotation);
        }

        if (this.scale != 1.0f) {
            scale.mul(this.scale);
            translation.mul(this.scale);
        }
        translation.sub(0, this.scaledSize.y - 0.01f, 0);

        display.setScale(scale);
        display.setTranslation(translation);
        display.setRightRotation(rightRotation);
        display.element().setYaw(this.parent.yBodyRot);

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

        consumer.accept(new ClientboundSetPassengersPacket(this.parent));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key, Object object) {
        super.onSyncedDataUpdated(key, object);
        if (key.equals(NylonTrackedData.EFFECT_COLOR)) {
            this.collisionElement.getDataTracker().set(NylonTrackedData.EFFECT_COLOR, (int) object);
        }
    }

    @Override
    protected void addDirectPassengers(IntList passengers) {
        super.addDirectPassengers(passengers);
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
    protected void updateOnFire(boolean displayFire) {
        this.hitboxInteraction.setOnFire(displayFire);
    }

    @Override
    protected void updateInvisibility(boolean isInvisible) {
        this.hitboxInteraction.setInvisible(isInvisible);
        super.updateInvisibility(isInvisible);
    }

    protected void updateScale(float scale) {
        this.scale = scale;
        this.size.mul(this.scale, this.scaledSize);
        this.collisionElement.setSize(Utils.toSlimeSize(Math.min(this.scaledSize.x, this.scaledSize.y)));
        for (Bone bone : this.bones) {
            bone.element().setDisplaySize(this.scaledSize.x * 2, -this.scaledSize.y - 1);
        }
    }

    protected void sendScaleUpdate() {
        this.sendPacket(new ClientboundBundlePacket(Utils.updateClientInteraction(this.hitboxInteraction, this.scaledSize)));
    }
}
