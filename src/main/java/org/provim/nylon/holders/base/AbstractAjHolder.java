package org.provim.nylon.holders.base;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.component.AnimationComponent;
import org.provim.nylon.component.VariantComponent;
import org.provim.nylon.holders.wrappers.Bone;
import org.provim.nylon.holders.wrappers.DisplayWrapper;
import org.provim.nylon.holders.wrappers.Locator;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;
import org.provim.nylon.util.Utils;

import java.util.List;
import java.util.Map;

public abstract class AbstractAjHolder<T extends Entity & AjEntity> extends AjElementHolder<T> {
    protected final Bone[] bones;
    protected final Locator[] locators;
    protected final Object2ObjectOpenHashMap<String, Locator> locatorMap;
    protected final ObjectOpenHashSet<DisplayElement> additionalDisplays;

    protected final AnimationComponent animation;
    protected final VariantComponent variant;

    protected EntityDimensions dimensions;

    protected AbstractAjHolder(T parent, AjModel model) {
        super(parent);

        this.animation = new AnimationComponent(model, this);
        this.variant = new VariantComponent(model, this);
        this.dimensions = parent.getType().getDimensions();

        Object2ObjectOpenHashMap<String, Locator> locatorMap = new Object2ObjectOpenHashMap<>();
        ObjectArrayList<Bone> bones = new ObjectArrayList<>();
        this.setupElements(model, bones, locatorMap);

        this.locatorMap = locatorMap;
        this.locators = new Locator[locatorMap.size()];
        this.additionalDisplays = new ObjectOpenHashSet<>();

        int index = 0;
        for (Locator locator : locatorMap.values()) {
            this.locators[index++] = locator;
        }

        this.bones = new Bone[bones.size()];
        for (index = 0; index < bones.size(); index++) {
            this.bones[index] = bones.get(index);
        }
    }

    protected void setupElements(AjModel model, List<Bone> bones, Map<String, Locator> locators) {
        Item rigItem = model.projectSettings().rigItem();
        for (AjNode node : model.rig().nodeMap().values()) {
            AjPose defaultPose = model.rig().defaultPose().get(node.uuid());
            switch (node.type()) {
                case bone -> {
                    ItemDisplayElement bone = this.createBone(node, rigItem);
                    if (bone != null) {
                        bones.add(Bone.of(bone, node, defaultPose));
                        this.addElement(bone);
                    }
                }
                case locator -> {
                    locators.put(node.name(), Locator.of(node, defaultPose));
                }
            }
        }
    }

    @Nullable
    protected ItemDisplayElement createBone(AjNode node, Item rigItem) {
        ItemDisplayElement element = new ItemDisplayElement();
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

    @Override
    public boolean addAdditionalDisplay(DisplayElement element) {
        if (this.additionalDisplays.add(element)) {
            this.addElement(element);
            this.sendPacket(new ClientboundSetPassengersPacket(this.parent));
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAdditionalDisplay(DisplayElement element) {
        if (this.additionalDisplays.remove(element)) {
            this.removeElement(element);
            return true;
        }
        return false;
    }

    @Override
    protected void addDirectPassengers(IntList passengers) {
    }

    protected void onEntityDataLoaded() {
        this.onDimensionsUpdated(this.parent.getDimensions(this.parent.getPose()));

        for (Bone bone : this.bones) {
            this.applyDefaultPose(bone);
        }
    }

    public void applyDefaultPose(DisplayWrapper<?> display) {
        this.applyPose(display.getDefaultPose(), display);
    }

    @Override
    protected void onTick() {
        for (Bone bone : this.bones) {
            this.updateElement(bone);
        }

        for (Locator locator : this.locators) {
            this.updateLocator(locator);
        }

        this.animation.tickAnimations();
    }

    protected void updateLocator(Locator locator) {
        if (locator.requiresUpdate()) {
            AjPose pose = this.animation.findPose(locator);
            if (pose != null) {
                locator.updateListeners(this, pose);
            }
        }
    }

    protected void updateElement(DisplayWrapper<?> display) {
        AjPose pose = this.animation.findPose(display);
        if (pose != null) {
            this.applyPose(pose, display);
        }
    }

    public void applyPose(AjPose pose, DisplayWrapper<?> display) {
        display.setTranslation(pose.readOnlyTranslation());
        display.setRightRotation(pose.readOnlyRightRotation());
        display.setLeftRotation(pose.readOnlyLeftRotation());
        display.setScale(pose.readOnlyScale());

        display.startInterpolation();
    }

    @Override
    public void onDimensionsUpdated(EntityDimensions dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key, Object object) {
        if (key.equals(EntityTrackedData.FLAGS)) {
            byte value = (byte) object;
            this.updateOnFire(Utils.getSharedFlag(value, EntityTrackedData.ON_FIRE_FLAG_INDEX));
            this.updateGlowing(Utils.getSharedFlag(value, EntityTrackedData.GLOWING_FLAG_INDEX));
            this.updateInvisibility(Utils.getSharedFlag(value, EntityTrackedData.INVISIBLE_FLAG_INDEX));
        }
    }

    protected void updateOnFire(boolean displayFire) {
    }

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

    @Override
    public Locator getLocator(String name) {
        return this.locatorMap.get(name);
    }

    @Override
    public int[] getDisplayIds() {
        int[] displays = new int[this.bones.length + this.additionalDisplays.size()];

        int index = 0;
        for (Bone bone : this.bones) {
            displays[index++] = bone.element().getEntityId();
        }

        for (DisplayElement element : this.additionalDisplays) {
            displays[index++] = element.getEntityId();
        }

        return displays;
    }

    @Override
    public int getDisplayVehicleId() {
        return this.parent.getId();
    }

    @Override
    public int getVehicleId() {
        return this.parent.getId();
    }

    @Override
    public int getLeashedId() {
        return this.parent.getId();
    }

    @Override
    public int getEntityEventId() {
        return this.parent.getId();
    }

    @Override
    public int getCritParticleId() {
        return this.parent.getId();
    }

    @Override
    public VariantComponent getVariantController() {
        return this.variant;
    }

    @Override
    public AnimationComponent getAnimator() {
        return this.animation;
    }

    public Bone[] getBones() {
        return this.bones;
    }

    public Locator[] getLocators() {
        return this.locators;
    }
}
