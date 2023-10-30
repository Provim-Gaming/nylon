package org.provim.nylontest;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import org.provim.nylontest.registries.MobRegistry;
import org.provim.nylontest.registries.SoundRegistry;

public class NylonTest implements ModInitializer {
    public static final String MODID = "nylon-testmod";

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MODID);
        PolymerResourcePackUtils.markAsRequired();
        MobRegistry.registerMobs();
        SoundRegistry.registerSounds();
    }
}
