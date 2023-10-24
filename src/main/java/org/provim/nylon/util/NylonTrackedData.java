package org.provim.nylon.util;	

import net.minecraft.network.syncher.EntityDataAccessor;	
import org.provim.nylon.mixins.accessors.LivingEntityAccessor;	

public class NylonTrackedData {	
    public static final EntityDataAccessor<Integer> EFFECT_COLOR = LivingEntityAccessor.getDATA_EFFECT_COLOR_ID();	
}