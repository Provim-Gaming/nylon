package org.provim.nylon.holders.base;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.api.Animator;
import org.provim.nylon.component.AnimationComponent;
import org.provim.nylon.component.VariantComponent;
import org.provim.nylon.holders.elements.Bone;
import org.provim.nylon.holders.elements.DisplayWrapper;
import org.provim.nylon.holders.elements.LocatorDisplay;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;
import org.provim.nylon.util.Utils;

import java.util.List;
import java.util.Map;

public abstract class AbstractAjHolder<T extends Entity & AjEntity> extends AjElementHolder<T> {
    protected static final Quaternionf ROT_180 = Axis.YP.rotationDegrees(180.f);
    protected final Vector2f size;
    protected final Bone[] bones;
    protected final LocatorDisplay[] locators;
    protected final Map<String, LocatorDisplay> locatorMap;

    protected final AnimationComponent animation;
    protected final VariantComponent variant;
    private int activeLocatorCount;

    protected AbstractAjHolder(T parent, AjModel model, boolean updateElementsAsync) {
        super(parent, updateElementsAsync);
        this.size = new Vector2f(parent.getType().getWidth(), parent.getType().getHeight());

        this.animation = new AnimationComponent(model, this.server, updateElementsAsync);
        this.variant = new VariantComponent(model, this.server);

        Object2ObjectOpenHashMap<String, LocatorDisplay> locators = new Object2ObjectOpenHashMap<>();
        ObjectArrayList<Bone> bones = new ObjectArrayList<>();
        this.setupElements(model, bones, locators);

        this.locatorMap = Object2ObjectMaps.unmodifiable(locators);
        this.locators = new LocatorDisplay[locators.size()];
        this.activeLocatorCount = locators.size();

        int index = 0;
        for (LocatorDisplay locator : locators.values()) {
            this.locators[index++] = locator;
        }

        this.bones = new Bone[bones.size()];
        for (index = 0; index < bones.size(); index++) {
            this.bones[index] = bones.get(index);
        }
    }

    protected void setupElements(AjModel model, List<Bone> bones, Map<String, LocatorDisplay> locators) {
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
                    DisplayElement locator = this.createLocatorDisplay(node);
                    if (locator != null) {
                        locators.put(node.name(), LocatorDisplay.of(locator, node, defaultPose, this));
                        this.addElement(locator);
                    }
                }
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

    @Nullable
    @SuppressWarnings("ConstantConditions")
    protected DisplayElement createLocatorDisplay(AjNode node) {
        if (node.entityType() != null) {
            DisplayElement locator = switch (node.entityType().getPath()) {
                case "item_display" -> new ItemDisplayElement();
                case "block_display" -> new BlockDisplayElement();
                case "text_display" -> {
                    TextDisplayElement element = new TextDisplayElement();
                    element.setBackground(0);
                    yield element;
                }
                default -> null;
            };

            if (locator != null) {
                locator.setInvisible(true);
                locator.setInterpolationDuration(2);
                locator.getDataTracker().set(DisplayTrackedData.TELEPORTATION_DURATION, this.parent.getTeleportDuration());
                return locator;
            }
        }

        return null;
    }

    public void activateLocator(LocatorDisplay locator, boolean isServerOnly) {
        this.activeLocatorCount++;
        if (!isServerOnly) {
            this.addElement(locator.element());
            this.sendPacket(new ClientboundSetPassengersPacket(this.parent));
        }
    }

    public void deactivateLocator(LocatorDisplay locator) {
        this.activeLocatorCount--;
        this.removeElement(locator.element());
    }

    protected void onEntityDataLoaded() {
        for (Bone bone : this.bones) {
            this.applyPose(bone.getDefaultPose(), bone);
        }

        for (LocatorDisplay locator : this.locators) {
            this.applyPose(locator.getDefaultPose(), locator);
        }
    }

    protected void updateElements() {
        for (Bone bone : this.bones) {
            this.updateElement(bone);
        }

        if (this.activeLocatorCount > 0) {
            for (LocatorDisplay locator : this.locators) {
                if (locator.isActive()) {
                    this.updateElement(locator);
                    locator.updateTransformationConsumer();
                }
            }
        }

        this.animation.tickAnimations();
    }

    protected void updateElement(DisplayWrapper<?> display) {
        AjPose pose = this.animation.firstPose(display);
        if (pose != null) {
            this.applyPose(pose, display);
        }
    }

    public void applyPose(AjPose pose, DisplayWrapper<?> display) {
        Vector3f scale = pose.scale();
        Vector3f translation = pose.translation();
        Quaternionf rightRotation = pose.rotation().mul(ROT_180).normalize();

        display.setTranslation(translation);
        display.setRightRotation(rightRotation);
        display.setScale(scale);

        display.startInterpolation();
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

        for (LocatorDisplay locator : this.locators) {
            locator.element().setGlowing(isGlowing);
        }
    }

    @Override
    public void setDefaultVariant() {
        this.variant.applyDefaultVariant(this.bones);
    }

    @Override
    public void setCurrentVariant(String variant) {
        this.variant.applyVariant(variant, this.bones);
    }

    @Override
    @Nullable
    public LocatorDisplay getLocator(String name) {
        return this.locatorMap.get(name);
    }

    @Override
    public int[] getDisplayIds() {
        int[] displays = new int[this.bones.length + this.activeLocatorCount];

        int index = 0;
        for (Bone bone : this.bones) {
            displays[index++] = bone.element().getEntityId();
        }

        if (this.activeLocatorCount > 0) {
            for (LocatorDisplay locator : this.locators) {
                if (locator.isActive()) {
                    displays[index++] = locator.element().getEntityId();
                }
            }
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
    public Animator getAnimator() {
        return this.animation;
    }
}
