package com.customrealms.relics.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeManager {
    private final JavaPlugin plugin;
    private final CustomItemRegistry registry;

    public RecipeManager(JavaPlugin plugin, CustomItemRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public void registerRecipes() {
        addShaped("titan_plate_helmet", new String[]{"NNN", "NEN", "   "},
                'N', Material.NETHERITE_INGOT, 'E', Material.NETHER_STAR);

        addShaped("titan_plate_chestplate", new String[]{"N N", "NEN", "NNN"},
                'N', Material.NETHERITE_INGOT, 'E', Material.NETHER_STAR);

        addShaped("zephyr_hood", new String[]{"FFF", "FDF", "   "},
                'F', Material.FEATHER, 'D', Material.DIAMOND_HELMET);

        addShaped("bloodforged_helm", new String[]{"RBR", "RIR", "   "},
                'R', Material.REDSTONE_BLOCK, 'B', Material.BLAZE_ROD, 'I', Material.IRON_HELMET);

        addShaped("chronos_rapier", new String[]{"  A", " D ", "C  "},
                'A', Material.AMETHYST_SHARD, 'D', Material.DIAMOND_SWORD, 'C', Material.CLOCK);

        addShaped("earthshatter_maul", new String[]{"ONO", "OSO", " S "},
                'O', Material.CRYING_OBSIDIAN, 'N', Material.NETHERITE_INGOT, 'S', Material.BREEZE_ROD);

        addShaped("cinderwake_mace", new String[]{" B ", "MHM", " B "},
                'B', Material.BREEZE_ROD, 'M', Material.MAGMA_CREAM, 'H', Material.HEAVY_CORE);

        addShaped("veinminer_pickaxe", new String[]{"DND", " B ", " B "},
                'D', Material.DIAMOND_BLOCK, 'N', Material.NETHERITE_PICKAXE, 'B', Material.BLAZE_ROD);

        addShaped("treecapitator_axe", new String[]{"EE ", "ES ", " S "},
                'E', Material.EMERALD_BLOCK, 'S', Material.STICK);

        addShaped("harvester_scythe", new String[]{"GG ", " H ", " S "},
                'G', Material.GOLDEN_APPLE, 'H', Material.DIAMOND_HOE, 'S', Material.STICK);

        addShaped("chronos_hourglass", new String[]{"GCG", "EAE", "GCG"},
                'G', Material.GOLD_BLOCK, 'C', Material.CLOCK, 'E', Material.ENDER_PEARL, 'A', Material.AMETHYST_CLUSTER);

        addShaped("aegis_prism", new String[]{"IWI", "NSN", "IWI"},
                'I', Material.IRON_BLOCK, 'W', Material.NETHERITE_SCRAP, 'N', Material.NAUTILUS_SHELL, 'S', Material.SHIELD);

        addShaped("riftwalker_charm", new String[]{"EPE", "PQP", "EPE"},
                'E', Material.ENDER_PEARL, 'P', Material.PHANTOM_MEMBRANE, 'Q', Material.ECHO_SHARD);

        addShaped("soulbloom_pendant", new String[]{"EAE", "AGA", "EAE"},
                'E', Material.ECHO_SHARD, 'A', Material.AMETHYST_SHARD, 'G', Material.GOLDEN_APPLE);

        addShaped(CustomItemRegistry.EVERLASTING_BAKED_POTATO_ID, new String[]{"PPP", "PXP", "PPP"},
                'P', Material.BAKED_POTATO, 'X', Material.POISONOUS_POTATO);
    }

    private void addShaped(String itemId, String[] shape, Object... ingredients) {
        ItemStack result = registry.createItem(itemId);
        if (result == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "recipe_" + itemId);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape[0], shape[1], shape[2]);
        for (int i = 0; i < ingredients.length; i += 2) {
            recipe.setIngredient((Character) ingredients[i], (Material) ingredients[i + 1]);
        }
        Bukkit.addRecipe(recipe);
    }
}
