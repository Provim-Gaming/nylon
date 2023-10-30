package org.provim.nylontest.registries;

import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class SoundRegistry {
    public static final SoundEvent GOLEM_AMBIENT = register("golem.ambient", SoundEvents.EMPTY);
    public static final SoundEvent GOLEM_HURT = register("golem.hurt", SoundEvents.IRON_GOLEM_HURT);
    public static final SoundEvent GOLEM_DEATH = register("golem.death", SoundEvents.IRON_GOLEM_DEATH);

    private static SoundEvent register(String name, SoundEvent soundEvent) {
        ResourceLocation identifier = new ResourceLocation("animated_java", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, PolymerSoundEvent.of(identifier, soundEvent));
    }

    public static void registerSounds() {
    }
}
