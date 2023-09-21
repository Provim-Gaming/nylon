package org.provim.nylon.entities.holders;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.Util;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.provim.nylon.entities.holders.elements.Bone;
import org.provim.nylon.entities.holders.elements.DisplayWrapper;
import org.provim.nylon.entities.holders.elements.LocatorDisplay;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;
import org.provim.nylon.model.component.AnimationComponent;
import org.provim.nylon.model.component.VariantComponent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class AbstractAjHolder<T extends Entity> extends ElementHolder implements AjHolderInterface {
    private static final Executor EXECUTOR = Util.backgroundExecutor();

    protected final Vector2f size;
    protected final T parent;

    protected final Bone[] bones;
    private final Map<String, LocatorDisplay> locators;
    private final ObjectLinkedOpenHashSet<LocatorDisplay> activeLocators;

    private final AnimationComponent animation;
    private final VariantComponent variant;

    private final boolean updateElementsAsync;
    private boolean isLoaded;
    private int tickCount;

    protected AbstractAjHolder(T parent, AjModel model, boolean updateElementsAsync) {
        this.size = new Vector2f(parent.getType().getWidth(), parent.getType().getHeight());
        this.updateElementsAsync = updateElementsAsync;
        this.tickCount = parent.tickCount - 1;
        this.parent = parent;

        this.animation = new AnimationComponent(model);
        this.variant = new VariantComponent(model);

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
                    ItemDisplayElement bone = this.createBone(model, node, rigItem);
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
    protected ItemDisplayElement createBone(AjModel model, AjNode node, Item rigItem) {
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
                this.sendPacket(VirtualEntityUtils.createRidePacket(this.getDisplayVehicleId(), this.getDisplayIds()));
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

    @Override
    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        consumer.accept(new ClientboundUpdateMobEffectPacket(this.parent.getId(), new MobEffectInstance(MobEffects.WATER_BREATHING, -1, 0, false, false)));
        consumer.accept(VirtualEntityUtils.createRidePacket(this.getDisplayVehicleId(), this.getDisplayIds()));
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
        for (Bone bone : this.bones) {
            this.applyPose(bone.getDefaultPose(), bone);
        }

        for (LocatorDisplay locator : this.activeLocators) {
            this.applyPose(locator.getDefaultPose(), locator);
        }
    }

    @Override
    public final void tick() {
        if (this.tickCount++ % 2 != 0) {
            return;
        }

        int parentTickCount = this.parent.tickCount;
        if (parentTickCount < this.tickCount) {
            // If the parent entity is behind, they likely haven't been ticked - in which case we don't need to update our elements.
            this.tickCount = parentTickCount;
            return;
        }

        if (this.updateElementsAsync) {
            EXECUTOR.execute(super::tick);
        } else {
            super.tick();
        }
    }

    @Override
    protected final void onTick() {
        this.updateElements();
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

    public List<VirtualElement> getVirtualElements() {
        return getElements();
    }

    public void playAnimation(String name) {
        this.playAnimation(name,null);
    }

    public void playAnimation(String name, Runnable onFinished) {
        this.animation.playAnimation(name, onFinished);
    }

    public void playAnimation(String name, int speed, Runnable onFinished) {
        this.animation.playAnimation(name, speed, onFinished);
    }

    public void pauseAnimation(String name) {
        this.animation.pauseAnimation(name);
    }

    public void stopAnimation(String name) {
        this.animation.stopAnimation(name);
    }
}
