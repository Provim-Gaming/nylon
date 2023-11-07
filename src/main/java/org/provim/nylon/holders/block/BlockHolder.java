package org.provim.nylon.holders.block;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.provim.nylon.holders.base.AbstractAjHolder;
import org.provim.nylon.model.AjModel;

public class BlockHolder extends AbstractAjHolder {
    protected final Vec3 pos;

    public BlockHolder(ServerLevel level, Vec3 pos, AjModel model) {
        super(model, level);
        this.pos = pos;
    }

    @Override
    public CommandSourceStack createCommandSourceStack() {
        return new CommandSourceStack(
                this.getServer(),
                this.pos,
                Vec2.ZERO,
                this.level,
                0,
                "BlockHolder",
                Component.literal("BlockHolder"),
                this.getServer(),
                null
        );
    }
}
