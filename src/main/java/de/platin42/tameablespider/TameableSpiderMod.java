package de.platin42.tameablespider;

import de.platin42.tameablespider.entity.ModEntityTypes;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.resources.Identifier;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TameableSpiderMod implements ModInitializer {
	public static final String MOD_ID = "tameablespider";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static Identifier ResourceLocation;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

        LOGGER.info("Initializing Tameable Spider Mod...");

        //register spider and attributes (why? - tempting; goals and targets are then overridden internally, but tempting is a "new" thing to a spider.)
        ModEntityTypes.registerModEntityTypes();
        ModEntityTypes.registerAttributes();

        //add spawning
        SpawnPlacements.register(
                ModEntityTypes.TAMEABLE_SPIDER,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules // Reuses standard hostile monster spawn checks (darkness required)
        );

        //Remove vanilla spiders from all overworld biomes
        BiomeModifications.create(Identifier.fromNamespaceAndPath(MOD_ID, "remove_vanilla_spiders"))
            .add(ModificationPhase.REMOVALS, BiomeSelectors.foundInOverworld(), context -> {
                context.getMobSpawnSettings().removeSpawnsOfEntityType(EntityTypes.SPIDER);
        });

        //Add "new" ones
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(), // Spawns everywhere in the overworld biomes
                MobCategory.MONSTER,             // Categorized as a monster
                ModEntityTypes.TAMEABLE_SPIDER,     // Your EntityType
                100,                             // Weight (standard spider weight is usually 100)
                4,                               // Minimum group size
                4                                // Maximum group size
        );
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
