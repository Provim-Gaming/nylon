package org.provim.nylon.component;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.provim.nylon.holders.base.AbstractAjHolder;

import java.util.UUID;

public class AnimationTickResult {
    public UUID wantedVariant;
    public ResourceLocation wantedSound;
    public String wantedCommand;

    private AnimationTickResult(UUID wantedVariant, ResourceLocation wantedSound, String wantedCommand) {
        this.wantedVariant = wantedVariant;
        this.wantedSound = wantedSound;
        this.wantedCommand = wantedCommand;
    }

    static AnimationTickResult empty() {
        return new AnimationTickResult(null, null, null);
    }

    public void clear() {
        this.wantedVariant = null;
        this.wantedSound = null;
        this.wantedCommand = null;
    }

    public void run(AbstractAjHolder holder, MinecraftServer server) {
        if (this.wantedVariant != null) {
            holder.setVariant(this.wantedVariant);
        }
        if (this.wantedSound != null) {
            holder.getParent().playSound(BuiltInRegistries.SOUND_EVENT.get(this.wantedSound));
        }
        // TODO: configurable list
        if (this.wantedCommand != null && (this.wantedCommand.startsWith("say") || this.wantedCommand.startsWith("particle") || this.wantedCommand.startsWith("summon"))) {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withEntity(holder.getParent()), this.wantedCommand);
        }

        this.clear();
    }
}