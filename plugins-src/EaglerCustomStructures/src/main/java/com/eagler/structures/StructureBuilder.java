package com.eagler.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import java.util.concurrent.ThreadLocalRandom;

public class StructureBuilder {

    private final LootGenerator lootGenerator;

    public StructureBuilder(LootGenerator lootGenerator) {
        this.lootGenerator = lootGenerator;
    }

    public void buildStructure(Location origin, StructureType type) {
        switch (type) {
            case BANDIT_WATCHTOWER -> buildWatchtower(origin, type);
            case MOSSY_SHRINE -> buildMossyShrine(origin, type);
            case DESERT_OBELISK -> buildDesertObelisk(origin, type);
            case FROST_CAIRN -> buildFrostCairn(origin, type);
            case SUNKEN_RUIN -> buildSunkenRuin(origin, type);
        }
    }

    // 1. Bandit Watchtower (5x5 footprint, 8 blocks tall)
    private void buildWatchtower(Location origin, StructureType type) {
        World w = origin.getWorld();
        if (w == null) return;
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();

        // Foundation & Cobblestone base
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                set(w, ox + x, oy - 1, oz + z, Material.COBBLESTONE);
                set(w, ox + x, oy, oz + z, Material.STONE_BRICKS);
            }
        }

        // Corner Oak Log pillars
        for (int y = 1; y <= 6; y++) {
            set(w, ox - 2, oy + y, oz - 2, Material.OAK_LOG);
            set(w, ox + 2, oy + y, oz - 2, Material.OAK_LOG);
            set(w, ox - 2, oy + y, oz + 2, Material.OAK_LOG);
            set(w, ox + 2, oy + y, oz + 2, Material.OAK_LOG);
        }

        // Upper platform & crenellations
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                set(w, ox + x, oy + 5, oz + z, Material.SPRUCE_PLANKS);
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    set(w, ox + x, oy + 6, oz + z, (x + z) % 2 == 0 ? Material.COBBLESTONE_WALL : Material.AIR);
                }
            }
        }

        // Ladder up the side
        for (int y = 1; y <= 5; y++) {
            set(w, ox, oy + y, oz - 1, Material.OAK_FENCE);
        }

        // Lantern & Loot Chest on top deck
        set(w, ox + 1, oy + 6, oz + 1, Material.LANTERN);
        placeLootChest(w, ox, oy + 6, oz, type);
    }

    // 2. Ancient Mossy Shrine (5x5 overgrown stone ruin)
    private void buildMossyShrine(Location origin, StructureType type) {
        World w = origin.getWorld();
        if (w == null) return;
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Material floor = rng.nextBoolean() ? Material.MOSSY_STONE_BRICKS : Material.MOSS_BLOCK;
                set(w, ox + x, oy - 1, oz + z, Material.STONE_BRICKS);
                set(w, ox + x, oy, oz + z, floor);
            }
        }

        // Ruined pillars
        int[] heights = {3, 4, 2, 3};
        int[][] corners = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}};
        for (int i = 0; i < 4; i++) {
            for (int y = 1; y <= heights[i]; y++) {
                set(w, ox + corners[i][0], oy + y, oz + corners[i][1], Material.MOSSY_COBBLESTONE);
            }
        }

        // Central altar with chest and soul lantern
        set(w, ox, oy + 1, oz, Material.CHISELED_STONE_BRICKS);
        placeLootChest(w, ox, oy + 2, oz, type);
        set(w, ox, oy + 3, oz, Material.SOUL_LANTERN);
    }

    // 3. Desert Sun Obelisk (Sandstone spire with hidden sub-floor chest)
    private void buildDesertObelisk(Location origin, StructureType type) {
        World w = origin.getWorld();
        if (w == null) return;
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                set(w, ox + x, oy - 2, oz + z, Material.SMOOTH_SANDSTONE);
                set(w, ox + x, oy - 1, oz + z, Material.CUT_SANDSTONE);
                if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                    set(w, ox + x, oy, oz + z, Material.CHISELED_SANDSTONE);
                }
            }
        }

        // Tall sandstone spire
        for (int y = 1; y <= 6; y++) {
            set(w, ox, oy + y, oz, y == 4 ? Material.GOLD_BLOCK : Material.CUT_SANDSTONE);
        }
        set(w, ox, oy + 7, oz, Material.SANDSTONE_WALL);

        // Exposed offering chest at base
        placeLootChest(w, ox, oy + 1, oz - 1, type);
    }

    // 4. Frozen Explorer Cairn (Packed ice & deepslate monument)
    private void buildFrostCairn(Location origin, StructureType type) {
        World w = origin.getWorld();
        if (w == null) return;
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                set(w, ox + x, oy - 1, oz + z, Material.COBBLED_DEEPSLATE);
                if (Math.abs(x) + Math.abs(z) <= 3) {
                    set(w, ox + x, oy, oz + z, Material.PACKED_ICE);
                }
            }
        }

        set(w, ox - 1, oy + 1, oz, Material.BLUE_ICE);
        set(w, ox + 1, oy + 1, oz, Material.BLUE_ICE);
        set(w, ox, oy + 1, oz + 1, Material.DEEPSLATE_BRICK_WALL);
        set(w, ox, oy + 2, oz + 1, Material.SOUL_LANTERN);
        set(w, ox, oy + 1, oz - 1, Material.CAMPFIRE);

        placeLootChest(w, ox, oy + 1, oz, type);
    }

    // 5. Shoreline Smuggler Ruin (Weathered prismarine & barrel cache)
    private void buildSunkenRuin(Location origin, StructureType type) {
        World w = origin.getWorld();
        if (w == null) return;
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                set(w, ox + x, oy - 1, oz + z, Material.PRISMARINE_BRICKS);
            }
        }

        set(w, ox - 2, oy, oz - 2, Material.DARK_PRISMARINE);
        set(w, ox - 2, oy + 1, oz - 2, Material.DARK_PRISMARINE);
        set(w, ox + 2, oy, oz + 2, Material.DARK_PRISMARINE);
        set(w, ox + 2, oy + 1, oz + 2, Material.SEA_LANTERN);
        set(w, ox - 1, oy, oz + 1, Material.BARREL);

        placeLootChest(w, ox, oy, oz, type);
    }

    private void placeLootChest(World world, int x, int y, int z, StructureType type) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(Material.CHEST, false);
        if (block.getState() instanceof Chest chest) {
            lootGenerator.populateChest(chest.getBlockInventory(), type);
        }
    }

    private void set(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material, false);
    }
}
