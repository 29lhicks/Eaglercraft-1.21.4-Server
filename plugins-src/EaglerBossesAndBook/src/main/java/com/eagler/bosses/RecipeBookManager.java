package com.eagler.bosses;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class RecipeBookManager {

    private final JavaPlugin plugin;
    private final ItemBridge itemBridge;
    private final NamespacedKey recipeBookKey;

    public RecipeBookManager(JavaPlugin plugin, ItemBridge itemBridge) {
        this.plugin = plugin;
        this.itemBridge = itemBridge;
        this.recipeBookKey = new NamespacedKey(plugin, "artisan_recipe_tome");
    }

    public ItemStack createRecipeBook() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return book;

        meta.setDisplayName(ChatColor.GOLD + "Artisan's Recipe Tome");
        meta.setLore(List.of(ChatColor.GRAY + "Right-click to browse every crafting recipe."));
        meta.getPersistentDataContainer().set(recipeBookKey, PersistentDataType.BYTE, (byte) 1);
        book.setItemMeta(meta);
        return book;
    }

    public boolean isRecipeBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(recipeBookKey, PersistentDataType.BYTE);
    }

    public void giveRecipeBook(Player player) {
        for (ItemStack leftover : player.getInventory().addItem(createRecipeBook()).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.1f);
        player.sendMessage(ChatColor.GOLD + "✦ You received the " + ChatColor.YELLOW
                + "Artisan's Recipe Tome" + ChatColor.GOLD + "! Right-click it to open the crafting recipe GUI.");
    }

    public void registerRecipes() {
        // Recipe Tome crafting recipe (Book + 4 Amethyst Shards)
        ShapedRecipe tomeRecipe = new ShapedRecipe(new NamespacedKey(plugin, "recipe_artisan_tome"), createRecipeBook());
        tomeRecipe.shape(" A ", "ABA", " A ");
        tomeRecipe.setIngredient('A', Material.AMETHYST_SHARD);
        tomeRecipe.setIngredient('B', Material.BOOK);
        Bukkit.addRecipe(tomeRecipe);

        // Ignis Summoning Crystal
        ShapedRecipe ignis = new ShapedRecipe(new NamespacedKey(plugin, "recipe_ignis_crystal"), itemBridge.createMaterial(CustomMaterialType.IGNIS_CRYSTAL, 1));
        ignis.shape("BMB", "MEM", "BMB");
        ignis.setIngredient('B', Material.BLAZE_POWDER);
        ignis.setIngredient('M', Material.MAGMA_CREAM);
        ignis.setIngredient('E', Material.ENDER_EYE);
        Bukkit.addRecipe(ignis);

        // Boreas Summoning Crystal
        ShapedRecipe boreas = new ShapedRecipe(new NamespacedKey(plugin, "recipe_boreas_crystal"), itemBridge.createMaterial(CustomMaterialType.BOREAS_CRYSTAL, 1));
        boreas.shape("BPB", "PEP", "BPB");
        boreas.setIngredient('B', Material.BREEZE_ROD);
        boreas.setIngredient('P', Material.PHANTOM_MEMBRANE);
        boreas.setIngredient('E', Material.ENDER_EYE);
        Bukkit.addRecipe(boreas);

        // Malakor Summoning Crystal
        ShapedRecipe malakor = new ShapedRecipe(new NamespacedKey(plugin, "recipe_malakor_crystal"), itemBridge.createMaterial(CustomMaterialType.MALAKOR_CRYSTAL, 1));
        malakor.shape("PAP", "AEA", "PAP");
        malakor.setIngredient('P', Material.ENDER_PEARL);
        malakor.setIngredient('A', Material.AMETHYST_SHARD);
        malakor.setIngredient('E', Material.ENDER_EYE);
        Bukkit.addRecipe(malakor);

        // Goliath Summoning Crystal
        ShapedRecipe goliath = new ShapedRecipe(new NamespacedKey(plugin, "recipe_goliath_crystal"), itemBridge.createMaterial(CustomMaterialType.GOLIATH_CRYSTAL, 1));
        goliath.shape("OIO", "IEI", "OIO");
        goliath.setIngredient('O', Material.OBSIDIAN);
        goliath.setIngredient('I', Material.IRON_BLOCK);
        goliath.setIngredient('E', Material.ENDER_EYE);
        Bukkit.addRecipe(goliath);
    }
}
