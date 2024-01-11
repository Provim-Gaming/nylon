package org.provim.nylon.util;

import net.minecraft.network.syncher.EntityDataAccessor;
import org.provim.nylon.mixins.accessors.LivingEntityAccessor;

public class NylonConstants {
    public static final EntityDataAccessor<Integer> DATA_EFFECT_COLOR = LivingEntityAccessor.getDATA_EFFECT_COLOR_ID();
    public static final int DAMAGE_TINT_COLOR = 0xFF7E7E;
}
