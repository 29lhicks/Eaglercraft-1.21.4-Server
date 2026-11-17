package com.eagler.structures;

import org.bukkit.block.Biome;

import java.util.Set;

public enum StructureType {
    BANDIT_WATCHTOWER("watchtower", "Bandit Watchtower", 0.0045, Set.of(
            Biome.PLAINS, Biome.SUNFLOWER_PLAINS, Biome.MEADOW, Biome.FOREST, Biome.BIRCH_FOREST, Biome.TAIGA, Biome.SAVANNA
    )),
    MOSSY_SHRINE("shrine", "Ancient Mossy Shrine", 0.0040, Set.of(
            Biome.FOREST, Biome.DARK_FOREST, Biome.OLD_GROWTH_PINE_TAIGA, Biome.OLD_GROWTH_SPRUCE_TAIGA, Biome.SWAMP, Biome.MANGROVE_SWAMP, Biome.JUNGLE
    )),
    DESERT_OBELISK("obelisk", "Buried Sun Obelisk", 0.0050, Set.of(
            Biome.DESERT, Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.WOODED_BADLANDS
    )),
    FROST_CAIRN("cairn", "Frozen Explorer Cairn", 0.0045, Set.of(
            Biome.SNOWY_PLAINS, Biome.SNOWY_TAIGA, Biome.ICE_SPIKES, Biome.GROVE, Biome.FROZEN_PEAKS, Biome.JAGGED_PEAKS
    )),
    SUNKEN_RUIN("ruin", "Shoreline Smuggler Ruin", 0.0045, Set.of(
            Biome.BEACH, Biome.SNOWY_BEACH, Biome.STONY_SHORE, Biome.RIVER
    ));

    private final String key;
    private final String displayName;
    private final double spawnChancePerChunk;
    private final Set<Biome> allowedBiomes;

    StructureType(String key, String displayName, double spawnChancePerChunk, Set<Biome> allowedBiomes) {
        this.key = key;
        this.displayName = displayName;
        this.spawnChancePerChunk = spawnChancePerChunk;
        this.allowedBiomes = allowedBiomes;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public double getSpawnChancePerChunk() { return spawnChancePerChunk; }
    public Set<Biome> getAllowedBiomes() { return allowedBiomes; }

    public static StructureType fromKey(String input) {
        for (StructureType type : values()) {
            if (type.key.equalsIgnoreCase(input) || type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        return null;
    }
}
