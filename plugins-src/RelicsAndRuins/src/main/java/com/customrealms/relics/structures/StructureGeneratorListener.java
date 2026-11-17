package com.customrealms.relics.structures;

import com.customrealms.relics.items.CustomItemDefinition;
import com.customrealms.relics.items.CustomItemRegistry;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;

public class StructureGeneratorListener implements Listener {
    private final JavaPlugin plugin;
    private final CustomItemRegistry registry;
    private boolean warnedInvalidRarity;

    public StructureGeneratorListener(JavaPlugin plugin, CustomItemRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {
        if (!plugin.getConfig().getBoolean("enable-custom-structures", true)) return;

        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        World.Environment environment = world.getEnvironment();
        int rarity = switch (environment) {
            case NORMAL -> plugin.getConfig().getInt("structure-chunk-rarity", 95);
            case NETHER -> plugin.getConfig().getInt("nether-structure-chunk-rarity", 95);
            case THE_END -> plugin.getConfig().getInt("end-structure-chunk-rarity", 95);
            default -> -1;
        };
        if (rarity == -1) return;
        if (rarity < 1) {
            if (!warnedInvalidRarity) {
                plugin.getLogger().warning("Invalid structure chunk rarity; using 95.");
                warnedInvalidRarity = true;
            }
            rarity = 95;
        }

        long seed = world.getSeed() ^ ((long) chunk.getX() * 341873128712L + (long) chunk.getZ() * 132897987541L);
        if (environment != World.Environment.NORMAL) {
            seed ^= (long) environment.ordinal() * 0x9E3779B97F4A7C15L;
        }
        Random rand = new Random(seed);
        if (rand.nextInt(rarity) != 0) return;

        switch (environment) {
            case NORMAL -> generateOverworldStructure(chunk, rand);
            case NETHER -> generateNetherStructure(chunk, rand);
            case THE_END -> generateEndStructure(chunk, rand);
            default -> { }
        }
    }

    private void generateOverworldStructure(Chunk chunk, Random rand) {
        World world = chunk.getWorld();
        int x = (chunk.getX() << 4) + 8;
        int z = (chunk.getZ() << 4) + 8;
        int y = world.getHighestBlockYAt(x, z);
        if (y < 60 || y + 7 >= world.getMaxHeight()) return;

        Material ground = world.getBlockAt(x, y, z).getType();
        if (!ground.isSolid() || ground == Material.WATER || ground == Material.LAVA) return;

        Location origin = new Location(world, x, y + 1, z);
        int type = rand.nextInt(4);
        switch (type) {
            case 0 -> {
                buildSunkenObelisk(origin, rand);
                logGenerated("Sunken Obelisk", world, x, y, z);
            }
            case 1 -> {
                buildForgottenForge(origin, rand);
                logGenerated("Forgotten Forge", world, x, y, z);
            }
            case 2 -> {
                buildAstralShrine(origin, rand);
                logGenerated("Astral Shrine", world, x, y, z);
            }
            case 3 -> {
                buildCryptSanctum(origin, rand);
                logGenerated("Crypt Sanctum", world, x, y, z);
            }
        }
    }

    private void generateNetherStructure(Chunk chunk, Random rand) {
        Location origin = findNetherSite(chunk, rand);
        if (origin == null) return;

        String[] names = {
                "Ashen Watchtower", "Crimson Cache", "Basalt Forge",
                "Soulfire Shrine", "Gilded Bastion Vault", "Wither Reliquary"
        };
        int type = rand.nextInt(names.length);
        switch (type) {
            case 0 -> buildAshenWatchtower(origin, rand);
            case 1 -> buildCrimsonCache(origin, rand);
            case 2 -> buildBasaltForge(origin, rand);
            case 3 -> buildSoulfireShrine(origin, rand);
            case 4 -> buildGildedBastionVault(origin, rand);
            case 5 -> buildWitherReliquary(origin, rand);
        }
        logGenerated(names[type], origin.getWorld(), origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
    }

    private void generateEndStructure(Chunk chunk, Random rand) {
        Location origin = findEndSite(chunk, rand);
        if (origin == null) return;

        String[] names = {"Voidspire Observatory", "Chorus Observatory", "Dragon's Cache"};
        int type = rand.nextInt(names.length);
        switch (type) {
            case 0 -> buildVoidspireObservatory(origin, rand);
            case 1 -> buildChorusObservatory(origin, rand);
            case 2 -> buildDragonCache(origin, rand);
        }
        logGenerated(names[type], origin.getWorld(), origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
    }

    private Location findNetherSite(Chunk chunk, Random rand) {
        World world = chunk.getWorld();
        int centerX = (chunk.getX() << 4) + 8;
        int centerZ = (chunk.getZ() << 4) + 8;
        for (int attempt = 0; attempt < 20; attempt++) {
            int x = centerX + rand.nextInt(5) - 2;
            int z = centerZ + rand.nextInt(5) - 2;
            int y = 32 + rand.nextInt(80);
            if (y + 6 >= world.getMaxHeight()) continue;

            boolean suitable = true;
            for (int dx = -2; dx <= 2 && suitable; dx++) {
                for (int dz = -2; dz <= 2 && suitable; dz++) {
                    if (!isNetherGround(world.getBlockAt(x + dx, y - 1, z + dz).getType())) {
                        suitable = false;
                        break;
                    }
                    for (int dy = 0; dy <= 5; dy++) {
                        if (!world.getBlockAt(x + dx, y + dy, z + dz).isEmpty()) {
                            suitable = false;
                            break;
                        }
                    }
                }
            }
            if (suitable) return new Location(world, x, y, z);
        }
        return null;
    }

    private boolean isNetherGround(Material type) {
        return type == Material.NETHERRACK
                || type == Material.BASALT
                || type == Material.BLACKSTONE
                || type == Material.SOUL_SAND
                || type == Material.SOUL_SOIL
                || type == Material.CRIMSON_NYLIUM
                || type == Material.WARPED_NYLIUM
                || type == Material.NETHER_GOLD_ORE
                || type == Material.NETHER_QUARTZ_ORE;
    }

    private Location findEndSite(Chunk chunk, Random rand) {
        World world = chunk.getWorld();
        int centerX = (chunk.getX() << 4) + 8;
        int centerZ = (chunk.getZ() << 4) + 8;
        for (int attempt = 0; attempt < 12; attempt++) {
            int x = centerX + rand.nextInt(5) - 2;
            int z = centerZ + rand.nextInt(5) - 2;
            int y = world.getHighestBlockYAt(x, z);
            if (y < world.getMinHeight() || y + 7 >= world.getMaxHeight()) continue;

            boolean suitable = true;
            int highestGroundY = y;
            for (int dx = -2; dx <= 2 && suitable; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int groundY = world.getHighestBlockYAt(x + dx, z + dz);
                    if (Math.abs(groundY - y) > 2
                            || !world.getBlockAt(x + dx, groundY, z + dz).getType().isSolid()) {
                        suitable = false;
                        break;
                    }
                    highestGroundY = Math.max(highestGroundY, groundY);
                }
            }
            if (suitable && highestGroundY + 7 < world.getMaxHeight()) {
                return new Location(world, x, highestGroundY + 1, z);
            }
        }
        return null;
    }

    private void buildAshenWatchtower(Location origin, Random rand) {
        buildFoundation(origin, Material.NETHER_BRICKS);
        for (int[] corner : new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}}) {
            for (int y = 0; y < 4; y++) {
                origin.getWorld().getBlockAt(origin.clone().add(corner[0], y, corner[1]))
                        .setType(Material.NETHER_BRICKS);
            }
            origin.getWorld().getBlockAt(origin.clone().add(corner[0], 4, corner[1]))
                    .setType(Material.NETHER_BRICK_FENCE);
        }
        placeLootChest(origin, 0, 0, 1, rand, World.Environment.NETHER);
        origin.getWorld().getBlockAt(origin.clone().add(0, 0, -1)).setType(Material.CHAIN);
    }

    private void buildCrimsonCache(Location origin, Random rand) {
        buildFoundation(origin, Material.CRIMSON_NYLIUM);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) == 1 || Math.abs(dz) == 1) {
                    origin.getWorld().getBlockAt(origin.clone().add(dx, 0, dz)).setType(Material.CRIMSON_PLANKS);
                }
            }
        }
        origin.getWorld().getBlockAt(origin.clone().add(-2, 0, -2)).setType(Material.CRIMSON_FENCE);
        origin.getWorld().getBlockAt(origin.clone().add(2, 0, 2)).setType(Material.CRIMSON_FENCE);
        placeLootChest(origin, 0, 0, 0, rand, World.Environment.NETHER);
    }

    private void buildBasaltForge(Location origin, Random rand) {
        buildFoundation(origin, Material.POLISHED_BASALT);
        origin.getWorld().getBlockAt(origin.clone().add(0, 0, 0)).setType(Material.MAGMA_BLOCK);
        origin.getWorld().getBlockAt(origin.clone().add(-1, 0, 0)).setType(Material.ANVIL);
        origin.getWorld().getBlockAt(origin.clone().add(1, 0, 0)).setType(Material.CHAIN);
        placeLootChest(origin, 0, 0, 2, rand, World.Environment.NETHER);
    }

    private void buildSoulfireShrine(Location origin, Random rand) {
        buildFoundation(origin, Material.SOUL_SOIL);
        for (int[] corner : new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}}) {
            origin.getWorld().getBlockAt(origin.clone().add(corner[0], 0, corner[1])).setType(Material.SOUL_SAND);
            origin.getWorld().getBlockAt(origin.clone().add(corner[0], 1, corner[1])).setType(Material.SOUL_LANTERN);
        }
        placeLootChest(origin, 0, 0, 0, rand, World.Environment.NETHER);
    }

    private void buildGildedBastionVault(Location origin, Random rand) {
        buildFoundation(origin, Material.POLISHED_BLACKSTONE_BRICKS);
        for (int[] corner : new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}}) {
            origin.getWorld().getBlockAt(origin.clone().add(corner[0], 0, corner[1])).setType(Material.GILDED_BLACKSTONE);
            origin.getWorld().getBlockAt(origin.clone().add(corner[0], 1, corner[1])).setType(Material.GOLD_BLOCK);
        }
        placeLootChest(origin, 0, 0, 0, rand, World.Environment.NETHER);
    }

    private void buildWitherReliquary(Location origin, Random rand) {
        buildFoundation(origin, Material.OBSIDIAN);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    origin.getWorld().getBlockAt(origin.clone().add(dx, 0, dz)).setType(Material.NETHER_BRICKS);
                }
            }
        }
        origin.getWorld().getBlockAt(origin.clone().add(-2, 0, -2)).setType(Material.CRYING_OBSIDIAN);
        origin.getWorld().getBlockAt(origin.clone().add(2, 0, 2)).setType(Material.CRYING_OBSIDIAN);
        placeLootChest(origin, 0, 0, 0, rand, World.Environment.NETHER);
    }

    private void buildVoidspireObservatory(Location origin, Random rand) {
        buildFoundation(origin, Material.END_STONE_BRICKS);
        for (int[] corner : new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}}) {
            for (int y = 0; y < 4; y++) {
                origin.getWorld().getBlockAt(origin.clone().add(corner[0], y, corner[1]))
                        .setType(Material.PURPUR_PILLAR);
            }
            origin.getWorld().getBlockAt(origin.clone().add(corner[0], 4, corner[1]))
                    .setType(Material.END_ROD);
        }
        placeLootChest(origin, 0, 0, 0, rand, World.Environment.THE_END);
    }

    private void buildChorusObservatory(Location origin, Random rand) {
        buildFoundation(origin, Material.PURPUR_BLOCK);
        for (int x = -2; x <= 2; x += 4) {
            for (int z = -2; z <= 2; z += 4) {
                origin.getWorld().getBlockAt(origin.clone().add(x, 0, z)).setType(Material.END_STONE);
                origin.getWorld().getBlockAt(origin.clone().add(x, 1, z)).setType(Material.END_ROD);
            }
        }
        origin.getWorld().getBlockAt(origin.clone().add(0, 0, 0)).setType(Material.PURPUR_PILLAR);
        placeLootChest(origin, 1, 0, 0, rand, World.Environment.THE_END);
    }

    private void buildDragonCache(Location origin, Random rand) {
        buildFoundation(origin, Material.END_STONE);
        for (int[] corner : new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}}) {
            origin.getWorld().getBlockAt(origin.clone().add(corner[0], 0, corner[1])).setType(Material.OBSIDIAN);
            origin.getWorld().getBlockAt(origin.clone().add(corner[0], 1, corner[1])).setType(Material.PURPUR_BLOCK);
        }
        origin.getWorld().getBlockAt(origin.clone().add(0, 0, 0)).setType(Material.PURPUR_PILLAR);
        placeLootChest(origin, 0, 0, 1, rand, World.Environment.THE_END);
    }

    private void buildFoundation(Location origin, Material material) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                origin.getWorld().getBlockAt(origin.clone().add(dx, -1, dz)).setType(material);
            }
        }
    }

    private void placeLootChest(
            Location origin, int dx, int dy, int dz, Random rand, World.Environment environment
    ) {
        Block chest = origin.getWorld().getBlockAt(origin.clone().add(dx, dy, dz));
        chest.setType(Material.CHEST);
        fillDimensionLoot(chest, rand, environment);
    }

    private void fillDimensionLoot(Block containerBlock, Random rand, World.Environment environment) {
        Inventory inventory = getContainerInventory(containerBlock);
        if (inventory == null) return;

        List<CustomItemDefinition> structureItems = registry.getBySource(CustomItemDefinition.Source.STRUCTURE_LOOT);
        if (!structureItems.isEmpty()) {
            CustomItemDefinition chosen = structureItems.get(rand.nextInt(structureItems.size()));
            ItemStack customLoot = registry.createItem(chosen.getId());
            if (customLoot != null) inventory.setItem(12, customLoot);
        }

        ItemStack vanillaGear = environment == World.Environment.NETHER
                ? new ItemStack(Material.NETHERITE_PICKAXE)
                : new ItemStack(Material.ELYTRA);
        vanillaGear.addUnsafeEnchantment(Enchantment.UNBREAKING, 4);
        vanillaGear.addUnsafeEnchantment(Enchantment.MENDING, 2);
        if (environment == World.Environment.NETHER) {
            vanillaGear.addUnsafeEnchantment(Enchantment.EFFICIENCY, 5);
            vanillaGear.addUnsafeEnchantment(Enchantment.FORTUNE, 3);
        }
        inventory.setItem(14, vanillaGear);
        inventory.setItem(10, createOverpoweredBook());
        inventory.setItem(16, new ItemStack(
                environment == World.Environment.NETHER ? Material.NETHERITE_SCRAP : Material.SHULKER_SHELL,
                1 + rand.nextInt(2)));
    }

    private ItemStack createOverpoweredBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        if (book.getItemMeta() instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta meta) {
            meta.addStoredEnchant(Enchantment.UNBREAKING, 4, true);
            meta.addStoredEnchant(Enchantment.MENDING, 2, true);
            book.setItemMeta(meta);
        }
        return book;
    }

    private Inventory getContainerInventory(Block block) {
        if (block.getState() instanceof Chest chest) {
            return chest.getBlockInventory();
        }
        if (block.getState() instanceof Barrel barrel) {
            return barrel.getInventory();
        }
        return null;
    }

    private void logGenerated(String name, World world, int x, int y, int z) {
        plugin.getLogger().info(name + " generated in " + world.getName() + " at " + x + ", " + y + ", " + z + ".");
    }

    public void buildSunkenObelisk(Location origin, Random rand) {
        World w = origin.getWorld();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                w.getBlockAt(origin.clone().add(dx, -1, dz)).setType(Material.CHISELED_STONE_BRICKS);
            }
        }
        for (int dy = 0; dy <= 6; dy++) {
            w.getBlockAt(origin.clone().add(0, dy, 0)).setType(dy == 6 ? Material.SEA_LANTERN : Material.OBSIDIAN);
        }
        Block chestBlock = w.getBlockAt(origin.clone().add(1, 0, 0));
        chestBlock.setType(Material.CHEST);
        fillRelicLoot(chestBlock, rand);
        w.spawnEntity(origin.clone().add(-1, 0, 1), EntityType.BOGGED);
    }

    public void buildForgottenForge(Location origin, Random rand) {
        World w = origin.getWorld();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                w.getBlockAt(origin.clone().add(dx, -1, dz)).setType(Material.DEEPSLATE_TILES);
                if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                    for (int dy = 0; dy <= 2; dy++) {
                        w.getBlockAt(origin.clone().add(dx, dy, dz)).setType(dy == 2 ? Material.IRON_BARS : Material.DEEPSLATE_BRICKS);
                    }
                }
            }
        }
        w.getBlockAt(origin.clone().add(0, 0, 0)).setType(Material.MAGMA_BLOCK);
        w.getBlockAt(origin.clone().add(1, 0, 0)).setType(Material.ANVIL);
        Block barrel = w.getBlockAt(origin.clone().add(-1, 0, 0));
        barrel.setType(Material.BARREL);
        fillRelicLoot(barrel, rand);
    }

    public void buildAstralShrine(Location origin, Random rand) {
        World w = origin.getWorld();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                w.getBlockAt(origin.clone().add(dx, -1, dz)).setType(Material.AMETHYST_BLOCK);
            }
        }
        int[][] pillars = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}};
        for (int[] p : pillars) {
            for (int dy = 0; dy <= 3; dy++) {
                w.getBlockAt(origin.clone().add(p[0], dy, p[1])).setType(dy == 3 ? Material.SHROOMLIGHT : Material.PURPUR_PILLAR);
            }
        }
        Block chest = w.getBlockAt(origin.clone().add(0, 0, 0));
        chest.setType(Material.CHEST);
        fillRelicLoot(chest, rand);
    }

    public void buildCryptSanctum(Location origin, Random rand) {
        World w = origin.getWorld();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                w.getBlockAt(origin.clone().add(dx, -1, dz)).setType(Material.MOSSY_STONE_BRICKS);
            }
        }
        w.getBlockAt(origin.clone().add(0, 0, 1)).setType(Material.SPAWNER);
        Block chest = w.getBlockAt(origin.clone().add(0, 0, -1));
        chest.setType(Material.CHEST);
        fillRelicLoot(chest, rand);
    }

    private void fillRelicLoot(Block containerBlock, Random rand) {
        Inventory inv = null;
        if (containerBlock.getState() instanceof Chest chest) {
            inv = chest.getBlockInventory();
        } else if (containerBlock.getState() instanceof Barrel barrel) {
            inv = barrel.getInventory();
        }
        if (inv == null) return;

        List<CustomItemDefinition> structItems = registry.getBySource(CustomItemDefinition.Source.STRUCTURE_LOOT);
        if (!structItems.isEmpty()) {
            CustomItemDefinition chosen = structItems.get(rand.nextInt(structItems.size()));
            ItemStack relic = registry.createItem(chosen.getId());
            if (relic != null) inv.setItem(13, relic);
        }
        inv.setItem(11, new ItemStack(Material.DIAMOND, 1 + rand.nextInt(3)));
        inv.setItem(15, new ItemStack(Material.EXPERIENCE_BOTTLE, 4 + rand.nextInt(8)));
    }
}
