package org.provim.nylon.holders.base;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.Animator;
import org.provim.nylon.component.AnimationComponent;
import org.provim.nylon.component.VariantComponent;
import org.provim.nylon.holders.wrapper.Bone;
import org.provim.nylon.holders.wrapper.DisplayWrapper;
import org.provim.nylon.holders.wrapper.Locator;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;
import org.provim.nylon.model.AjVariant;
import org.provim.nylon.util.Utils;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractAjHolder<T extends Entity & AjEntity> extends ElementHolder {
    protected static final Quaternionf ROT_180 = Axis.YP.rotationDegrees(180.f);

    protected final T parent;
    private boolean isLoaded;

    protected final Vector2f size;
    protected final Collection<Bone> bones;
    protected final Collection<Locator> locators;

    protected final ObjectSet<DisplayElement> additionalElements;
    protected final AnimationComponent animation;
    protected final VariantComponent variant;

    protected AbstractAjHolder(T parent, AjModel model) {
        this.parent = parent;
        if (this.parent.level().isClientSide) {
            throw new IllegalStateException("You can only create AjElementHolders for serverside entities!");
        }

        this.size = new Vector2f(parent.getType().getWidth(), parent.getType().getHeight());

        this.animation = new AnimationComponent(model);
        this.variant = new VariantComponent(model);

        this.bones = new ObjectArrayList<>();
        this.additionalElements = new ObjectArraySet<>();
        this.locators = new ObjectArrayList<>();
        this.setupElements(model);
    }

    protected void setupElements(AjModel model) {
        Item rigItem = model.projectSettings().rigItem();
        for (AjNode node : model.rig().nodeMap().values()) {
            AjPose defaultPose = model.rig().defaultPose().get(node.uuid());
            boolean alwaysUpdate = node.name().startsWith("head");
            switch (node.type()) {
                case bone -> {
                    ItemDisplayElement bone = this.createBone(node, rigItem);
                    if (bone != null) {
                        this.bones.add(Bone.of(bone, node, defaultPose, alwaysUpdate));
                        this.addElement(bone);
                    }
                }
                case locator -> this.locators.add(Locator.of(node, defaultPose, alwaysUpdate));
                default -> throw new IllegalStateException("Unexpected value: " + node.type());
            }
        }
    }

    @Nullable
    protected ItemDisplayElement createBone(AjNode node, Item rigItem) {
        ItemDisplayElement element = new ItemDisplayElement();
        element.setDisplaySize(this.size.x * 2, -this.size.y - 1);
        element.setModelTransformation(ItemDisplayContext.FIXED);
        element.setInvisible(true);
        element.setInterpolationDuration(2);
        element.getDataTracker().set(DisplayTrackedData.TELEPORTATION_DURATION, this.parent.getTeleportDuration());

        ItemStack itemStack = new ItemStack(rigItem);
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putInt("CustomModelData", node.customModelData());
        element.setItem(itemStack);

        return element;
    }

    protected void onEntityDataLoaded() {
        for (Bone bone : this.bones) {
            this.applyPose(bone.getDefaultPose(), bone);
        }
    }

    @Override
    protected void onTick() {
        for (Bone bone : this.bones) {
            this.updateElement(bone);
        }

        for (Locator locator : this.locators) {
            this.updateLocator(locator);
        }

        this.animation.tickAnimations().run(this, this.parent.getServer());
    }

    protected void updateLocator(Locator locator) {
        AjPose pose = this.animation.findPose(locator);
        if (pose != null) {
            locator.updateListeners(pose);
        }
    }

    protected void updateElement(DisplayWrapper display) {
        AjPose pose = this.animation.findPose(display);
        if (pose != null) {
            this.applyPose(pose, display);
        }
    }

    public void applyPose(AjPose pose, DisplayWrapper display) {
        Vector3f scale = pose.scale();
        Vector3f translation = pose.translation();
        Quaternionf rightRotation = pose.rotation().mul(ROT_180).normalize();

        display.setTranslation(translation);
        display.setRightRotation(rightRotation);
        display.setScale(scale);

        display.startInterpolation();
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key, Object object) {
        if (key.equals(EntityTrackedData.FLAGS)) {
            byte value = (byte) object;
            this.updateOnFire(Utils.getSharedFlag(value, EntityTrackedData.ON_FIRE_FLAG_INDEX));
            this.updateGlowing(Utils.getSharedFlag(value, EntityTrackedData.GLOWING_FLAG_INDEX));
            this.updateInvisibility(Utils.getSharedFlag(value, EntityTrackedData.INVISIBLE_FLAG_INDEX));
        }
    }

    @Override
    public final boolean startWatching(ServerGamePacketListenerImpl player) {
        if (!this.isLoaded) {
            this.isLoaded = true;
            this.onEntityDataLoaded();
        }

        return super.startWatching(player);
    }

    protected void addDirectPassengers(IntList passengers) {
        for (DisplayElement element: this.additionalElements) {
            passengers.add(element.getEntityId());
        }
    }

    public void addAdditionalElement(DisplayElement element) {
        element.setInterpolationDuration(1);

        this.addElement(element);
        this.additionalElements.add(element);
    }

    public void removeAdditionalElement(DisplayElement element) {
        this.additionalElements.remove(element);
        this.removeElement(element);
    }
    public boolean hasAdditionalElement(DisplayElement element) { return this.additionalElements.contains(element); }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        IntList passengers = new IntArrayList();
        this.addDirectPassengers(passengers);

        if (passengers.size() > 0) {
            consumer.accept(VirtualEntityUtils.createRidePacket(this.parent.getId(), passengers));
        }
    }

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
    }

    protected abstract void updateOnFire(boolean displayFire);

    protected void updateInvisibility(boolean isInvisible) {
        for (Bone bone : this.bones) {
            bone.setInvisible(isInvisible);
        }
    }

    protected void updateGlowing(boolean isGlowing) {
        for (Bone bone : this.bones) {
            bone.element().setGlowing(isGlowing);
        }
    }

    public void setDefaultVariant() {
        this.variant.applyDefaultVariant(this.bones);
    }

    public void setVariant(String variant) {
        this.variant.applyVariant(variant, this.bones);
    }

    public void setVariant(UUID variantId) {
        this.variant.applyVariant(variantId, this.bones);
    }

    public AjVariant getCurrentVariant() {
        return this.variant.getCurrentVariant();
    }

    public Collection<Integer> getDisplayIds() {
        IntList displays = new IntArrayList();
        this.bones.forEach(bone -> displays.add(bone.element().getEntityId()));
        return displays;
    }

    public int getDisplayVehicleId() {
        return this.parent.getId();
    }

    public int getVehicleId() {
        return this.parent.getId();
    }

    public int getLeashedId() {
        return this.parent.getId();
    }

    public Animator getAnimator() {
        return this.animation;
    }

    public T getParent() { return this.parent; }

    public Locator getLocator(String name) {
        for (Locator locator: this.locators) {
            if (locator.name().equals(name)) {
                return locator;
            }
        }
        return null;
    }
}
