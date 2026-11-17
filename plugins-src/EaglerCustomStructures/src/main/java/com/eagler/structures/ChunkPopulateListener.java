package com.eagler.structures;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkPopulateListener implements Listener {

    private final JavaPlugin plugin;
    private final StructureBuilder builder;
    private boolean warnedInvalidMultiplier;

    public ChunkPopulateListener(JavaPlugin plugin, StructureBuilder builder) {
        this.plugin = plugin;
        this.builder = builder;
    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) return;

        Chunk chunk = event.getChunk();
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        int localX = 2;
        int localZ = 2;
        int worldX = (chunk.getX() << 4) + localX;
        int worldZ = (chunk.getZ() << 4) + localZ;

        int highestY = world.getHighestBlockYAt(worldX, worldZ);
        if (highestY <= world.getMinHeight() + 5 || highestY >= world.getMaxHeight() - 15) return;

        Block surface = world.getBlockAt(worldX, highestY, worldZ);
        Material mat = surface.getType();
        // Skip water, lava, or tree canopies
        if (!mat.isSolid() || mat.name().endsWith("_LEAVES") || mat.name().endsWith("_LOG")) return;

        Biome biome = world.getBiome(worldX, highestY, worldZ);
        List<StructureType> valid = new ArrayList<>();
        for (StructureType type : StructureType.values()) {
            if (type.getAllowedBiomes().contains(biome)) {
                valid.add(type);
            }
        }
        if (valid.isEmpty()) return;

        StructureType chosen = valid.get(rng.nextInt(valid.size()));
        double chanceMultiplier = plugin.getConfig().getDouble("spawn-chance-multiplier", 4.0);
        if (!Double.isFinite(chanceMultiplier) || chanceMultiplier <= 0.0) {
            if (!warnedInvalidMultiplier) {
                plugin.getLogger().warning("Invalid spawn-chance-multiplier; using 4.0.");
                warnedInvalidMultiplier = true;
            }
            chanceMultiplier = 4.0;
        }
        double spawnChance = Math.min(1.0, chosen.getSpawnChancePerChunk() * chanceMultiplier);
        if (rng.nextDouble() >= spawnChance) return;

        Location origin = new Location(world, worldX, highestY + 1, worldZ);
        builder.buildStructure(origin, chosen);
        plugin.getLogger().info(chosen.getDisplayName() + " generated in " + world.getName()
                + " at " + worldX + ", " + highestY + ", " + worldZ + ".");
    }
}
