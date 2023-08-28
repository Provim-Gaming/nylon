package org.provim.animatedmobs.api.entities.holders;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.provim.animatedmobs.api.entities.holders.elements.FastItemDisplayElement;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.AjNode;
import org.provim.animatedmobs.api.model.AjPose;
import org.provim.animatedmobs.api.model.component.AnimationComponent;
import org.provim.animatedmobs.api.util.Util;

import java.util.UUID;
import java.util.function.Consumer;

public class AjHolder<T extends Entity> extends AbstractAjHolder<T> {
    public AjHolder(T parent, AjModel model) {
        super(parent, model);

        this.setupBoneElements(model);
        this.setupAdditionalElements(model);
    }

    protected void setupBoneElements(AjModel model) {
        Item rigItem = model.projectSettings().rigItem();

        model.rig().nodeMap().forEach((key, node) -> {
            if (node.type() == AjNode.NodeType.bone) {
                ItemDisplayElement element = new FastItemDisplayElement();
                element.setDisplaySize(this.size.x * 2, -this.size.y - 1);
                element.setModelTransformation(ItemDisplayContext.FIXED);
                element.setInterpolationDuration(2);
                element.setScale(new Vector3f(0.001f));
                this.addElement(element);

                AjPose pose = model.rig().getDefaultPose(key);
                this.poseComponent.putDefault(element, pose);

                ItemStack itemStack = new ItemStack(rigItem);
                CompoundTag tag = itemStack.getOrCreateTag();
                tag.putInt("CustomModelData", node.customModelData());

                element.setItem(itemStack);
                this.itemDisplays.put(node.uuid(), element);
            }
        });
    }

    protected void setupAdditionalElements(AjModel model) {
        model.rig().nodeMap().forEach((key, node) -> {
            if (node.type() == AjNode.NodeType.locator) {
                DisplayElement displayElement = Util.toDisplayElement(model, node);
                if (displayElement != null) {
                    displayElement.setInterpolationDuration(2);
                    this.addElement(displayElement);
                    this.additionalDisplays.put(node, displayElement);
                }
            }
        });
    }

    @Override
    protected void onEntityDataLoaded() {
        for (ItemDisplayElement element : this.itemDisplays.values()) {
            AjPose pose = this.poseComponent.getDefault(element);
            AnimationComponent.AnimationTransform transform = this.animationComponent.getInterpolatedAnimationTransform(pose);
            this.applyTransformWithCurrentEntityTransformation(transform, element);
        }
    }

    @Override
    protected void onTick() {
        this.updateElements();
    }

    protected void updateElements() {
        this.itemDisplays.forEach(this::updateElement);
        this.additionalDisplays.forEach((node, element) -> this.updateElement(node.uuid(), element));
        this.animationComponent.decreaseCounter();
    }

    protected void updateElement(UUID uuid, DisplayElement element) {
        AjPose currentPose;

        if (this.animationComponent.extraAnimationAvailable()) {
            currentPose = this.animationComponent.findExtraAnimationPose(uuid);
        } else {
            currentPose = this.animationComponent.findCurrentAnimationPose(this.parent.tickCount, uuid);
            if (currentPose == null) {
                currentPose = this.poseComponent.getDefault(element);
            }
        }

        if (currentPose == null) {
            return;
        }

        AnimationComponent.AnimationTransform transform = this.animationComponent.getInterpolatedAnimationTransform(currentPose);
        this.applyTransformWithCurrentEntityTransformation(transform, element);
    }

    public void applyTransformWithCurrentEntityTransformation(AnimationComponent.AnimationTransform transform, DisplayElement element) {
        Vector3f scale = transform.scale();
        Vector3f translation = transform.translation();
        Quaternionf rightRotation = transform.rot().mul(Axis.YP.rotationDegrees(180.f)).normalize();

        // Update data tracker values
        element.setTranslation(translation);
        element.setRightRotation(rightRotation);
        element.setScale(scale);

        element.startInterpolation();
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        consumer.accept(new ClientboundUpdateMobEffectPacket(this.parent.getId(), new MobEffectInstance(MobEffects.WATER_BREATHING, -1, 0, false, false)));
        consumer.accept(VirtualEntityUtils.createRidePacket(this.getVehicleId(), this.getDisplayIds()));
    }

    @Override
    public int getVehicleId() {
        return this.parent.getId();
    }
}
