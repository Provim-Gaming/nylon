package org.provim.animatedmobs.api.entities.holders;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.AjPose;
import org.provim.animatedmobs.api.model.component.AnimationComponent;
import org.provim.animatedmobs.api.model.component.PoseComponent;
import org.provim.animatedmobs.api.model.component.VariantComponent;

import java.util.UUID;
import java.util.function.Consumer;

public class AjHolder<T extends Entity> extends ElementHolder implements AjHolderInterface {
    protected final T parent;
    protected final Object2ObjectOpenHashMap<UUID, ItemDisplayElement> itemDisplays = new Object2ObjectOpenHashMap<>();

    protected final AnimationComponent animationComponent;
    protected final VariantComponent variantComponent;
    protected final PoseComponent poseComponent;
    protected final Vector2f size;
    private boolean isLoaded;

    public AjHolder(T parent, AjModel model) {
        this.parent = parent;
        this.size = new Vector2f(parent.getType().getWidth(), parent.getType().getHeight());

        this.animationComponent = new AnimationComponent(model);
        this.poseComponent = new PoseComponent(model);
        this.variantComponent = new VariantComponent(model);

        this.setupElements(model);
    }

    protected void setupElements(AjModel model) {
        Item rigItem = model.projectSettings().rigItem();
        model.rig().nodeMap().forEach((key, node) -> {
            ItemDisplayElement element = new ItemDisplayElement();
            element.setDisplaySize(this.size.x * 2, -this.size.y - 1);
            element.setModelTransformation(ItemDisplayContext.FIXED);
            element.setInterpolationDuration(0);
            element.setScale(new Vector3f(0.001f));
            this.addElement(element);

            AjPose pose = model.rig().getDefaultPose(key);
            this.poseComponent.putDefault(element, pose);

            ItemStack itemStack = new ItemStack(rigItem);
            CompoundTag tag = itemStack.getOrCreateTag();
            tag.putInt("CustomModelData", node.customModelData());

            element.setItem(itemStack);
            this.itemDisplays.put(node.uuid(), element);
        });
    }

    public void applyTransformWithCurrentEntityTransformation(AnimationComponent.AnimationTransform transform, ItemDisplayElement element) {
        Vector3f scale = new Vector3f(transform.scale());

        Vector3f translation = transform.pos();
        Quaternionf rightRotation = transform.rot().mul(Axis.YP.rotationDegrees(180.f)).normalize();

        if (!element.getScale().equals(scale) || !element.getRightRotation().equals(rightRotation) || !element.getTranslation().equals(translation)) {
            element.startInterpolation();
            element.setInterpolationDuration(2);

            element.setTranslation(translation);
            element.setRightRotation(rightRotation);
            element.setScale(scale);
        }
    }

    protected void updateElement(UUID uuid, ItemDisplayElement element) {
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

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
    }

    @Override
    public final boolean startWatching(ServerGamePacketListenerImpl player) {
        if (!this.isLoaded) {
            this.isLoaded = true;
            this.onEntityDataLoaded();
        }

        return super.startWatching(player);
    }

    protected void onEntityDataLoaded() {
        for (ItemDisplayElement element : this.itemDisplays.values()) {
            AjPose pose = this.poseComponent.getDefault(element);
            AnimationComponent.AnimationTransform transform = this.animationComponent.getInterpolatedAnimationTransform(pose);
            this.applyTransformWithCurrentEntityTransformation(transform, element);
        }
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        consumer.accept(new ClientboundUpdateMobEffectPacket(this.parent.getId(), new MobEffectInstance(MobEffects.WATER_BREATHING, -1, 0, false, false)));
        consumer.accept(VirtualEntityUtils.createRidePacket(this.getVehicleId(), this.getDisplayIds()));
    }

    @Override
    public void updateElements() {
        this.itemDisplays.forEach(this::updateElement);
        this.animationComponent.decreaseCounter();
    }

    @Override
    public int[] getDisplayIds() {
        int[] displays = new int[this.itemDisplays.size()];

        int index = 0;
        for (ItemDisplayElement element : this.itemDisplays.values()) {
            displays[index++] = element.getEntityId();
        }

        return displays;
    }

    @Override
    public int getVehicleId() {
        return this.parent.getId();
    }

    @Override
    public ItemDisplayElement getItemDisplayElement(UUID elementUUID) {
        return this.itemDisplays.get(elementUUID);
    }

    @Override
    public void setCurrentAnimation(String animation) {
        this.animationComponent.setCurrentAnimation(animation);
    }

    @Override
    public void startExtraAnimation(String animationName) {
        this.animationComponent.startExtraAnimation(animationName);
    }

    @Override
    public boolean extraAnimationRunning() {
        return this.animationComponent.extraAnimationAvailable();
    }

    @Override
    public void setDefaultVariant() {
        this.variantComponent.applyDefaultVariant(this.itemDisplays);
    }

    @Override
    public void setCurrentVariant(String variant) {
        this.variantComponent.applyVariant(variant, this.itemDisplays);
    }
}
