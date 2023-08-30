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
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.AjNode;
import org.provim.animatedmobs.api.model.AjPose;
import org.provim.animatedmobs.api.model.component.AnimationComponent;
import org.provim.animatedmobs.api.model.component.VariantComponent;
import org.provim.animatedmobs.api.util.WrappedDisplay;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class AbstractAjHolder<T extends Entity> extends ElementHolder implements AjHolderInterface {
    private static final Executor EXECUTOR = Util.backgroundExecutor();

    protected final WrappedDisplay<ItemDisplayElement>[] bones;
    protected final Map<String, WrappedDisplay<DisplayElement>> locators;
    protected final WrappedDisplay<? extends DisplayElement>[] elements;

    protected final AnimationComponent animationComponent;
    protected final VariantComponent variantComponent;

    protected final Vector2f size;
    protected final T parent;

    private final boolean updateElementsAsync;
    private boolean isLoaded;
    private int tickCount;

    @SuppressWarnings("unchecked")
    protected AbstractAjHolder(T parent, AjModel model, boolean updateElementsAsync) {
        this.size = new Vector2f(parent.getType().getWidth(), parent.getType().getHeight());
        this.updateElementsAsync = updateElementsAsync;
        this.tickCount = parent.tickCount - 1;
        this.parent = parent;

        this.animationComponent = new AnimationComponent(model);
        this.variantComponent = new VariantComponent(model);

        Object2ObjectOpenHashMap<String, WrappedDisplay<DisplayElement>> locators = new Object2ObjectOpenHashMap<>();
        ObjectArrayList<WrappedDisplay<ItemDisplayElement>> bones = new ObjectArrayList<>();
        this.setupElements(model, bones, locators);

        this.locators = Object2ObjectMaps.unmodifiable(locators);
        this.bones = new WrappedDisplay[bones.size()];
        for (int i = 0; i < bones.size(); i++) {
            this.bones[i] = bones.get(i);
        }

        // All display elements that need updates should be added at this point. We store all of them in a single array for faster iteration.
        this.elements = new WrappedDisplay[this.bones.length + locators.size()];

        int index = 0;
        for (WrappedDisplay<ItemDisplayElement> bone : this.bones) {
            this.elements[index++] = bone;
        }

        for (WrappedDisplay<DisplayElement> locator : locators.values()) {
            this.elements[index++] = locator;
        }
    }

    protected void setupElements(AjModel model, List<WrappedDisplay<ItemDisplayElement>> bones, Map<String, WrappedDisplay<DisplayElement>> locators) {
        Item rigItem = model.projectSettings().rigItem();
        for (AjNode node : model.rig().nodeMap().values()) {
            AjPose defaultPose = model.rig().defaultPose().get(node.uuid());
            switch (node.type()) {
                case bone -> {
                    ItemDisplayElement bone = this.createBone(model, node, defaultPose, rigItem);
                    if (bone != null) {
                        bones.add(WrappedDisplay.of(bone, node, defaultPose));
                        this.addElement(bone);
                    }
                }
                case locator -> {
                    DisplayElement locator = this.createLocator(model, node, defaultPose);
                    if (locator != null) {
                        locators.put(node.name(), WrappedDisplay.of(locator, node, defaultPose, false));
                        this.addElement(locator);
                    }
                }
            }
        }
    }

    @Nullable
    protected ItemDisplayElement createBone(AjModel model, AjNode node, AjPose defaultPose, Item rigItem) {
        ItemDisplayElement element = new ItemDisplayElement();
        element.setDisplaySize(this.size.x * 2, -this.size.y - 1);
        element.setModelTransformation(ItemDisplayContext.FIXED);
        // element.setTransformation(defaultPose.matrix());
        element.setInterpolationDuration(2);

        ItemStack itemStack = new ItemStack(rigItem);
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putInt("CustomModelData", node.customModelData());
        element.setItem(itemStack);

        return element;
    }

    @Nullable
    protected DisplayElement createLocator(AjModel model, AjNode node, AjPose defaultPose) {
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
                // locator.setTransformation(defaultPose.matrix());
                locator.setInterpolationDuration(2);
                return locator;
            }
        }

        return null;
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
        for (WrappedDisplay<ItemDisplayElement> wrapped : this.bones) {
            AnimationComponent.AnimationTransform transform = this.animationComponent.getInterpolatedAnimationTransform(wrapped.getDefaultPose());
            this.applyTransformWithCurrentEntityTransformation(transform, wrapped);
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
        for (WrappedDisplay<? extends DisplayElement> wrapped : this.elements) {
            this.updateElement(wrapped);
        }

        this.animationComponent.decreaseCounter();
    }

    protected void updateElement(WrappedDisplay<?> wrapped) {
        UUID uuid = wrapped.node().uuid();
        AjPose currentPose;

        if (this.animationComponent.extraAnimationAvailable()) {
            currentPose = this.animationComponent.findExtraAnimationPose(uuid);
        } else {
            currentPose = this.animationComponent.findCurrentAnimationPose(this.parent.tickCount, uuid);
            if (currentPose == null) {
                currentPose = wrapped.getDefaultPose();
            }
        }

        if (currentPose == null) {
            return;
        }

        AnimationComponent.AnimationTransform transform = this.animationComponent.getInterpolatedAnimationTransform(currentPose);
        this.applyTransformWithCurrentEntityTransformation(transform, wrapped);
    }

    public void applyTransformWithCurrentEntityTransformation(AnimationComponent.AnimationTransform transform, WrappedDisplay<?> wrapped) {
        Vector3f scale = transform.scale();
        Vector3f translation = transform.translation();
        Quaternionf rightRotation = transform.rot().mul(Axis.YP.rotationDegrees(180.f)).normalize();

        // Update data tracker values
        wrapped.setTranslation(translation);
        wrapped.setRightRotation(rightRotation);
        wrapped.setScale(scale);

        wrapped.startInterpolation();
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
    public DisplayElement getLocator(String name) {
        WrappedDisplay<DisplayElement> locator = this.locators.get(name);
        return locator == null ? null : locator.element();
    }

    @Override
    public int[] getDisplayIds() {
        int[] displays = new int[this.elements.length];

        int index = 0;
        for (WrappedDisplay<? extends DisplayElement> wrapped : this.elements) {
            displays[index++] = wrapped.element().getEntityId();
        }

        return displays;
    }

    @Override
    public int getVehicleId() {
        return this.parent.getId();
    }
}
