package org.provim.nylontest.registries;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import org.provim.nylontest.entities.RedstoneGolem;

public class MobRegistry {
    public static final EntityType<RedstoneGolem> REDSTONE_GOLEM = register(
            RedstoneGolem.ID,
            FabricEntityTypeBuilder.createMob()
                    .entityFactory(RedstoneGolem::new)
                    .spawnGroup(MobCategory.MONSTER)
                    .dimensions(EntityDimensions.scalable(1.8f, 2.7f))
                    .defaultAttributes(RedstoneGolem::createAttributes)
                    .spawnRestriction(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules)
                    .trackRangeChunks(8)
    );

    private static <T extends Entity> EntityType<T> register(ResourceLocation id, FabricEntityTypeBuilder<T> builder) {
        EntityType<T> type = builder.build();
        PolymerEntityUtils.registerType(type);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type);
    }

    public static void registerMobs() {
    }
}
