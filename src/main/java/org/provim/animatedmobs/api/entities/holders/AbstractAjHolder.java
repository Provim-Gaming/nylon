package org.provim.animatedmobs.api.entities.holders;

import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
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
import org.provim.animatedmobs.api.entities.holders.elements.Bone;
import org.provim.animatedmobs.api.entities.holders.elements.Locator;
import org.provim.animatedmobs.api.entities.holders.elements.WrappedDisplay;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.AjNode;
import org.provim.animatedmobs.api.model.AjPose;
import org.provim.animatedmobs.api.model.component.AnimationComponent;
import org.provim.animatedmobs.api.model.component.VariantComponent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class AbstractAjHolder<T extends Entity> extends ElementHolder implements AjHolderInterface {
    private static final Executor EXECUTOR = Util.backgroundExecutor();

    protected final Vector2f size;
    protected final T parent;

    protected final Bone[] bones;
    private final Map<String, Locator> locators;
    private final ObjectLinkedOpenHashSet<Locator> activeLocators;

    private final AnimationComponent animationComponent;
    private final VariantComponent variantComponent;

    private final boolean updateElementsAsync;
    private boolean isLoaded;
    private int tickCount;

    protected AbstractAjHolder(T parent, AjModel model, boolean updateElementsAsync) {
        this.size = new Vector2f(parent.getType().getWidth(), parent.getType().getHeight());
        this.updateElementsAsync = updateElementsAsync;
        this.tickCount = parent.tickCount - 1;
        this.parent = parent;

        this.animationComponent = new AnimationComponent(model);
        this.variantComponent = new VariantComponent(model);

        Object2ObjectOpenHashMap<String, Locator> locators = new Object2ObjectOpenHashMap<>();
        ObjectArrayList<Bone> bones = new ObjectArrayList<>();
        this.setupElements(model, bones, locators);

        this.locators = Object2ObjectMaps.unmodifiable(locators);
        this.activeLocators = new ObjectLinkedOpenHashSet<>(locators.values());

        this.bones = new Bone[bones.size()];
        for (int i = 0; i < bones.size(); i++) {
            this.bones[i] = bones.get(i);
        }
    }

    protected void setupElements(AjModel model, List<Bone> bones, Map<String, Locator> locators) {
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
                    DisplayElement locator = this.createLocator(model, node);
                    if (locator != null) {
                        locators.put(node.name(), Locator.of(locator, node, defaultPose, this));
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

        ItemStack itemStack = new ItemStack(rigItem);
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putInt("CustomModelData", node.customModelData());
        element.setItem(itemStack);

        return element;
    }

    @Nullable
    protected DisplayElement createLocator(AjModel model, AjNode node) {
        if (node.entityType() != null) {
            DisplayElement locator = switch (node.entityType().getPath()) {
                case "item_display" -> new ItemDisplayElement();
                case "block_display" -> new BlockDisplayElement();
                case "text_display" -> {
                    TextDisplayElement element = new TextDisplayElement();
                    element.setInvisible(true);
                    element.setBackground(0);
                    yield element;
                }
                default -> null;
            };

            if (locator != null) {
                locator.setInterpolationDuration(2);
                return locator;
            }
        }

        return null;
    }

    public void activateLocator(Locator locator, boolean update) {
        if (this.activeLocators.add(locator)) {
            if (update) {
                this.addElement(locator.element());
                this.sendPacket(VirtualEntityUtils.createRidePacket(this.getVehicleId(), this.getDisplayIds()));
            } else {
                this.addElementWithoutUpdates(locator.element());
            }
        }
    }

    public void deactivateLocator(Locator locator, boolean update) {
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
        consumer.accept(VirtualEntityUtils.createRidePacket(this.getVehicleId(), this.getDisplayIds()));
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

        for (Locator locator : this.activeLocators) {
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
            for (Locator locator : this.activeLocators) {
                this.updateElement(locator);
            }
        }

        this.animationComponent.decreaseCounter();
    }

    protected void updateElement(WrappedDisplay<?> display) {
        AjNode node = display.node();
        AjPose currentPose;

        if (this.animationComponent.extraAnimationAvailable()) {
            currentPose = this.animationComponent.findExtraAnimationPose(node);
        } else {
            currentPose = this.animationComponent.findCurrentAnimationPose(this.parent.tickCount, node);
            if (currentPose == null) {
                currentPose = display.getDefaultPose();
            }
        }

        if (currentPose == null) {
            return;
        }

        this.applyPose(currentPose, display);
    }

    public void applyPose(AjPose pose, WrappedDisplay<?> display) {
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
        this.variantComponent.applyDefaultVariant(this.bones);
    }

    @Override
    public void setCurrentVariant(String variant) {
        this.variantComponent.applyVariant(variant, this.bones);
    }

    @Override
    @Nullable
    public Locator getLocator(String name) {
        return this.locators.get(name);
    }

    @Override
    public int[] getDisplayIds() {
        int[] displays = new int[this.bones.length + this.activeLocators.size()];

        int index = 0;
        for (Bone bone : this.bones) {
            displays[index++] = bone.element().getEntityId();
        }

        for (Locator locator : this.activeLocators) {
            displays[index++] = locator.element().getEntityId();
        }

        return displays;
    }

    @Override
    public int getVehicleId() {
        return this.parent.getId();
    }
}
