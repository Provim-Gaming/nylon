package org.provim.animatedmobs.api.entities.holders;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.InteractionTrackedData;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.provim.animatedmobs.api.mixins.EntityAccessor;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.component.AnimationComponent;

import java.util.List;
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
        this.scale = this.parent.getScale();
        this.size.mul(this.scale, this.scaledSize);
        super.onEntityDataLoaded();
    }

    @Override
    public void applyTransformWithCurrentEntityTransformation(AnimationComponent.AnimationTransform transform, ItemDisplayElement element) {
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

        for (Packet<ClientGamePacketListener> packet : this.updateClientHitbox(this.scaledSize)) {
            consumer.accept(packet);
        }

        if (this.parent.canBreatheUnderwater()) {
            consumer.accept(new ClientboundUpdateMobEffectPacket(this.parent.getId(), new MobEffectInstance(MobEffects.WATER_BREATHING, -1, 0, false, false)));
        }

        consumer.accept(VirtualEntityUtils.createRidePacket(this.parent.getId(), this.hitboxInteraction.getEntityIds()));
        consumer.accept(VirtualEntityUtils.createRidePacket(this.getVehicleId(), this.getDisplayIds()));
    }

    public List<Packet<ClientGamePacketListener>> updateClientHitbox(Vector2f size) {
        // Updates the dimensions and bounding box of the interaction hitbox on the client. Note that the hitbox's dimensions and the bounding box are two different things.
        // - The bounding box is primarily used for detecting player attacks, interactions and rendering the hitbox.
        // - The dimensions are used for certain other properties, such as the passenger riding height or the fire animation.
        return List.of(
                // We update the POSE in this packet, which makes the client refresh the hitbox's dimensions. This is done to:
                // - Have the fire animation display correctly with the size of the entity when the entity is on fire.
                // - Move the passenger riding height of the interaction up. This is raised to height * 1.325 to match the top of the hitbox.
                new ClientboundSetEntityDataPacket(this.hitboxInteraction.getEntityId(), List.of(
                        SynchedEntityData.DataValue.create(InteractionTrackedData.HEIGHT, size.y * 1.325F),
                        SynchedEntityData.DataValue.create(InteractionTrackedData.WIDTH, size.x),
                        SynchedEntityData.DataValue.create(EntityTrackedData.POSE, Pose.STANDING)
                )),
                // Afterward, we send another packet that only updates the bounding box height back to its original value, without updating its dimensions.
                // This lets us turn the attack hitbox back into the correct size whilst keeping the raised passenger riding height.
                new ClientboundSetEntityDataPacket(this.hitboxInteraction.getEntityId(), List.of(
                        SynchedEntityData.DataValue.create(InteractionTrackedData.HEIGHT, size.y)
                ))
        );
    }

    @Override
    public void updateElements() {
        if (this.parent.deathTime > 0) {
            this.deathAngle = Math.min((float) Math.sqrt((this.parent.deathTime) / 20.0F * 1.6F), 1.f);
        }

        boolean isGlowing = this.parent.isCurrentlyGlowing();
        boolean displayFire = !this.parent.fireImmune() && (this.parent.getRemainingFireTicks() > 0 || this.parent instanceof EntityAccessor entity && entity.am_hasVisualFire());

        this.updateScale();
        this.hitboxInteraction.setOnFire(displayFire);
        this.itemDisplays.forEach((uuid, element) -> {
            element.setGlowing(isGlowing);
            this.updateElement(uuid, element);
        });

        this.animationComponent.decreaseCounter();
    }

    private void updateScale() {
        float scale = this.parent.getScale();
        if (scale != this.scale) {
            this.scale = scale;
            this.size.mul(this.scale, this.scaledSize);
            this.sendPacket(new ClientboundBundlePacket(this.updateClientHitbox(this.scaledSize)));
        }
    }

    @Override
    public int getVehicleId() {
        return this.hitboxInteraction.getEntityId();
    }
}
