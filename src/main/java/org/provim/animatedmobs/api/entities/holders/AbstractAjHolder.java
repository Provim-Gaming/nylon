package org.provim.animatedmobs.api.entities.holders;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.provim.animatedmobs.api.model.AjModel;
import org.provim.animatedmobs.api.model.AjNode;
import org.provim.animatedmobs.api.model.component.AnimationComponent;
import org.provim.animatedmobs.api.model.component.PoseComponent;
import org.provim.animatedmobs.api.model.component.VariantComponent;

import java.util.UUID;
import java.util.concurrent.Executor;

public abstract class AbstractAjHolder<T extends Entity> extends ElementHolder implements AjHolderInterface {
    private static final Executor EXECUTOR = Util.backgroundExecutor();
    protected final T parent;
    protected final Vector2f size;
    protected final Object2ObjectOpenHashMap<UUID, ItemDisplayElement> itemDisplays = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectOpenHashMap<AjNode, DisplayElement> additionalDisplays = new Object2ObjectOpenHashMap<>();
    protected final AnimationComponent animationComponent;
    protected final PoseComponent poseComponent;
    private final VariantComponent variantComponent;
    private final boolean updateElementsAsync;

    private boolean isLoaded;
    private int tickCount;

    public AbstractAjHolder(T parent, AjModel model) {
        this(parent, model, true); // Experimenting with async element updates and networking.
    }

    public AbstractAjHolder(T parent, AjModel model, boolean updateElementsAsync) {
        this.updateElementsAsync = updateElementsAsync;
        this.parent = parent;
        this.tickCount = parent.tickCount;
        this.size = new Vector2f(parent.getType().getWidth(), parent.getType().getHeight());
        this.animationComponent = new AnimationComponent(model);
        this.variantComponent = new VariantComponent(model);
        this.poseComponent = new PoseComponent(model);
    }

    @Override
    public ItemDisplayElement getItemDisplayElement(UUID elementUuid) {
        return this.itemDisplays.get(elementUuid);
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

    @Override
    @Nullable
    public DisplayElement getAdditionalDisplayNamed(String name) {
        for (AjNode node : this.additionalDisplays.keySet()) {
            if (node.name().equals(name)) {
                return this.additionalDisplays.get(node);
            }
        }
        return null;
    }

    @Override
    public int[] getDisplayIds() {
        int[] displays = new int[this.itemDisplays.size() + this.additionalDisplays.size()];

        int index = 0;
        for (ItemDisplayElement element : this.itemDisplays.values()) {
            displays[index++] = element.getEntityId();
        }

        for (DisplayElement element : this.additionalDisplays.values()) {
            displays[index++] = element.getEntityId();
        }

        return displays;
    }

    @Override
    public void tick() {
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

    protected abstract void onEntityDataLoaded();
}
