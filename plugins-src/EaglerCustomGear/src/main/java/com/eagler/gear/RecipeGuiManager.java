package com.eagler.gear;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipeGuiManager {

    public record RecipeDisplay(ItemStack result, ItemStack[] grid3x3) {}

    private final JavaPlugin plugin;
    private final GearManager gearManager;
    private final List<RecipeDisplay> recipes = new ArrayList<>();
    private final Set<NamespacedKey> registeredExternalRecipes = new HashSet<>();

    public static final String GUI_TITLE_PREFIX = ChatColor.DARK_PURPLE + "Artisan 3x3 Recipes ";

    public RecipeGuiManager(JavaPlugin plugin, GearManager gearManager) {
        this.plugin = plugin;
        this.gearManager = gearManager;
        buildDisplayList();
    }

    private void buildDisplayList() {
        // Health Items
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.BANDAGE_WRAP, 3), new ItemStack[]{
                null, item(Material.PAPER), null,
                item(Material.PAPER), item(Material.GLISTERING_MELON_SLICE), item(Material.PAPER),
                null, item(Material.PAPER), null
        }));
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.VITALITY_ELIXIR), new ItemStack[]{
                null, item(Material.GOLDEN_APPLE), null,
                item(Material.GOLDEN_APPLE), item(Material.HONEY_BOTTLE), item(Material.GOLDEN_APPLE),
                null, item(Material.BLAZE_POWDER), null
        }));
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.CRIMSON_HEART_FRUIT), new ItemStack[]{
                item(Material.REDSTONE_BLOCK), item(Material.GOLDEN_APPLE), item(Material.REDSTONE_BLOCK),
                item(Material.GOLDEN_APPLE), item(Material.EMERALD_BLOCK), item(Material.GOLDEN_APPLE),
                item(Material.REDSTONE_BLOCK), item(Material.GOLDEN_APPLE), item(Material.REDSTONE_BLOCK)
        }));

        // Early-game weapons
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.COPPER_SHORTSWORD), new ItemStack[]{
                null, item(Material.COPPER_INGOT), null,
                null, item(Material.COPPER_INGOT), null,
                null, item(Material.STICK), null
        }));
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.IRONBOUND_HATCHET), new ItemStack[]{
                item(Material.IRON_INGOT), item(Material.IRON_INGOT), null,
                item(Material.IRON_INGOT), item(Material.STICK), item(Material.COPPER_INGOT),
                null, item(Material.STICK), null
        }));
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.TRAILBLAZER_BOW), new ItemStack[]{
                item(Material.STICK), item(Material.COPPER_INGOT), null,
                item(Material.STICK), null, item(Material.STRING),
                item(Material.STICK), item(Material.COPPER_INGOT), null
        }));
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.BOOMERANG_DISK), new ItemStack[]{
                null, item(Material.IRON_INGOT), null,
                item(Material.IRON_INGOT), item(Material.DIAMOND), item(Material.IRON_INGOT),
                null, item(Material.IRON_INGOT), null
        }));
        addArmorDisplays(item(Material.COPPER_INGOT), item(Material.LEATHER),
                CustomGearType.TRAILBLAZER_HELMET, CustomGearType.TRAILBLAZER_CHESTPLATE,
                CustomGearType.TRAILBLAZER_LEGGINGS, CustomGearType.TRAILBLAZER_BOOTS);
        addArmorDisplays(item(Material.COPPER_INGOT), item(Material.IRON_INGOT),
                CustomGearType.IRONBOUND_HELMET, CustomGearType.IRONBOUND_CHESTPLATE,
                CustomGearType.IRONBOUND_LEGGINGS, CustomGearType.IRONBOUND_BOOTS);

        // These display items carry the same metadata as boss drops for ExactChoice recipes.
        ItemStack infernal = gearManager.getBossMaterialIngredient("infernal_core", Material.MAGMA_CREAM);
        ItemStack tempest = gearManager.getBossMaterialIngredient("tempest_essence", Material.WIND_CHARGE);
        ItemStack soul = gearManager.getBossMaterialIngredient("abyssal_soul_shard", Material.ECHO_SHARD);
        ItemStack titan = gearManager.getBossMaterialIngredient("titan_heartstone", Material.HEAVY_CORE);

        // Weapons & Tools
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.EXECUTIONER_GREATAXE), new ItemStack[]{
                infernal, infernal, null,
                infernal, item(Material.BLAZE_ROD), null,
                null, item(Material.BLAZE_ROD), null
        }));
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.VALKYRIE_SPEAR), new ItemStack[]{
                null, tempest, tempest,
                null, item(Material.BREEZE_ROD), tempest,
                item(Material.BREEZE_ROD), null, null
        }));
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.SKYBOUND_BOW), new ItemStack[]{
                item(Material.PHANTOM_MEMBRANE), tempest, item(Material.PHANTOM_MEMBRANE),
                item(Material.STRING), item(Material.BREEZE_ROD), item(Material.STRING),
                null, item(Material.STRING), null
        }));
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.SKYHOOK_ROD), new ItemStack[]{
                null, null, item(Material.BREEZE_ROD),
                null, item(Material.BREEZE_ROD), tempest,
                item(Material.STRING), null, tempest
        }));
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.SOULREAVER_SCYTHE), new ItemStack[]{
                soul, soul, null,
                null, item(Material.NETHERITE_INGOT), null,
                null, item(Material.NETHERITE_INGOT), null
        }));
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.PHANTOM_RAPIER), new ItemStack[]{
                null, null, soul,
                null, soul, null,
                item(Material.AMETHYST_SHARD), null, null
        }));
        recipes.add(new RecipeDisplay(gearManager.createItem(CustomGearType.EARTHSHAKER_PICKAXE), new ItemStack[]{
                titan, titan, titan,
                null, item(Material.OBSIDIAN), null,
                null, item(Material.OBSIDIAN), null
        }));

        // Add Armor Set Recipes
        addArmorDisplays(titan, item(Material.NETHERITE_INGOT), CustomGearType.JUGGERNAUT_HELMET, CustomGearType.JUGGERNAUT_CHESTPLATE, CustomGearType.JUGGERNAUT_LEGGINGS, CustomGearType.JUGGERNAUT_BOOTS);
        addArmorDisplays(tempest, item(Material.PHANTOM_MEMBRANE), CustomGearType.ZEPHYR_HOOD, CustomGearType.ZEPHYR_TUNIC, CustomGearType.ZEPHYR_LEGGINGS, CustomGearType.ZEPHYR_BOOTS);
        addArmorDisplays(infernal, item(Material.GOLD_INGOT), CustomGearType.PYRO_CROWN, CustomGearType.PYRO_CHESTPLATE, CustomGearType.PYRO_LEGGINGS, CustomGearType.PYRO_BOOTS);
        addArmorDisplays(soul, item(Material.ECHO_SHARD), CustomGearType.SHADOW_MASK, CustomGearType.SHADOW_CLOAK, CustomGearType.SHADOW_LEGGINGS, CustomGearType.SHADOW_BOOTS);
    }

    private void addArmorDisplays(ItemStack core, ItemStack sec, CustomGearType h, CustomGearType c, CustomGearType l, CustomGearType b) {
        recipes.add(new RecipeDisplay(gearManager.createItem(h), new ItemStack[]{core, sec, core, sec, null, sec, null, null, null}));
        recipes.add(new RecipeDisplay(gearManager.createItem(c), new ItemStack[]{sec, null, sec, core, sec, core, sec, sec, sec}));
        recipes.add(new RecipeDisplay(gearManager.createItem(l), new ItemStack[]{core, sec, core, sec, null, sec, sec, null, sec}));
        recipes.add(new RecipeDisplay(gearManager.createItem(b), new ItemStack[]{null, null, null, core, null, core, sec, null, sec}));
    }

    public void openRecipeMenu(Player player, int page) {
        addExternalRecipes();
        int safePage = Math.floorMod(page, recipes.size());
        RecipeDisplay current = recipes.get(safePage);

        Inventory gui = Bukkit.createInventory(null, 45, GUI_TITLE_PREFIX + "(" + (safePage + 1) + "/" + recipes.size() + ")");
        ItemStack filler = named(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) gui.setItem(i, filler);

        // 3x3 Grid slots (Rows 2-4, Columns 2-4 -> indices 10,11,12 / 19,20,21 / 28,29,30)
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int i = 0; i < 9; i++) {
            gui.setItem(gridSlots[i], current.grid3x3()[i]);
        }

        gui.setItem(23, named(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Craft in any 3x3 Crafting Table"));
        gui.setItem(25, current.result());

        gui.setItem(36, named(Material.ARROW, ChatColor.AQUA + "← Previous Recipe"));
        gui.setItem(44, named(Material.ARROW, ChatColor.AQUA + "Next Recipe →"));

        player.openInventory(gui);
    }

    private void addExternalRecipes() {
        var iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (!(recipe instanceof ShapedRecipe shaped)) continue;

            NamespacedKey key = shaped.getKey();
            if (!key.getNamespace().equals("relicsandruins")
                    && !key.getNamespace().equals("eaglerbossesandbook")) {
                continue;
            }
            if (!registeredExternalRecipes.add(key)) {
                continue;
            }

            ItemStack[] grid = new ItemStack[9];
            String[] shape = shaped.getShape();
            Map<Character, ItemStack> ingredients = shaped.getIngredientMap();
            for (int row = 0; row < shape.length; row++) {
                for (int column = 0; column < shape[row].length(); column++) {
                    ItemStack ingredient = ingredients.get(shape[row].charAt(column));
                    if (ingredient != null) {
                        grid[row * 3 + column] = ingredient.clone();
                    }
                }
            }
            recipes.add(new RecipeDisplay(shaped.getResult().clone(), grid));
        }
    }

    private ItemStack item(Material mat) {
        return new ItemStack(mat);
    }

    private ItemStack named(Material mat, String name) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
