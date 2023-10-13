package org.provim.nylon.holders.base;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
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

public abstract class AbstractAjHolder<T extends Entity> extends AjElementHolder {

    protected final Vector2f size;
    protected final T parent;

    protected final Bone[] bones;
    protected final Map<String, LocatorDisplay> locators;
    protected final ObjectLinkedOpenHashSet<LocatorDisplay> activeLocators;

    protected final AnimationComponent animation;
    protected final VariantComponent variant;

    protected AbstractAjHolder(T parent, AjModel model, boolean updateElementsAsync) {
        super(updateElementsAsync);

        this.size = new Vector2f(parent.getType().getWidth(), parent.getType().getHeight());
        this.parent = parent;

        this.animation = new AnimationComponent(model, parent.getServer(), updateElementsAsync);
        this.variant = new VariantComponent(model, parent.getServer());

        Object2ObjectOpenHashMap<String, LocatorDisplay> locators = new Object2ObjectOpenHashMap<>();
        ObjectArrayList<Bone> bones = new ObjectArrayList<>();
        this.setupElements(model, bones, locators);

        this.locators = Object2ObjectMaps.unmodifiable(locators);
        this.activeLocators = new ObjectLinkedOpenHashSet<>(locators.values());

        this.bones = new Bone[bones.size()];
        for (int i = 0; i < bones.size(); i++) {
            this.bones[i] = bones.get(i);
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
        element.setInterpolationDuration(2);
        element.setInvisible(true);

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
                return locator;
            }
        }

        return null;
    }

    public void activateLocator(LocatorDisplay locator, boolean update) {
        if (this.activeLocators.add(locator)) {
            if (update) {
                this.addElement(locator.element());
                this.sendPacket(new ClientboundSetPassengersPacket(this.parent));
            } else {
                this.addElementWithoutUpdates(locator.element());
            }
        }
    }

    public void deactivateLocator(LocatorDisplay locator, boolean update) {
        if (this.activeLocators.remove(locator)) {
            if (update) {
                this.removeElement(locator.element());
            } else {
                this.removeElementWithoutUpdates(locator.element());
            }
        }
    }

    protected void onEntityDataLoaded() {
        for (Bone bone : this.bones) {
            this.applyPose(bone.getDefaultPose(), bone);
        }

        for (LocatorDisplay locator : this.activeLocators) {
            this.applyPose(locator.getDefaultPose(), locator);
        }
    }

    protected void updateElements() {
        for (Bone bone : this.bones) {
            this.updateElement(bone);
        }

        if (this.activeLocators.size() > 0) {
            for (LocatorDisplay locator : this.activeLocators) {
                this.updateElement(locator);
                locator.updateTransformationConsumer();
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
        Quaternionf rightRotation = pose.rotation().mul(Axis.YP.rotationDegrees(180.f)).normalize();

        // Update data tracker values
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

        for (LocatorDisplay locator : this.activeLocators) {
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
        return this.locators.get(name);
    }

    @Override
    public int[] getDisplayIds() {
        int[] displays = new int[this.bones.length + this.activeLocators.size()];

        int index = 0;
        for (Bone bone : this.bones) {
            displays[index++] = bone.element().getEntityId();
        }

        for (LocatorDisplay locator : this.activeLocators) {
            displays[index++] = locator.element().getEntityId();
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
    public T getParent() {
        return this.parent;
    }

    @Override
    public Animator getAnimator() {
        return this.animation;
    }
}
