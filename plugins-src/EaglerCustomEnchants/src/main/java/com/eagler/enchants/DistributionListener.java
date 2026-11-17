package com.eagler.enchants;

import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DistributionListener implements Listener {

    private final JavaPlugin plugin;
    private final EnchantmentManager manager;

    public DistributionListener(JavaPlugin plugin, EnchantmentManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // 1. Enchanting Table integration
    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        int expCost = event.getExpLevelCost();
        double chance = expCost >= 25 ? 0.55 : (expCost >= 15 ? 0.35 : 0.20);
        if (ThreadLocalRandom.current().nextDouble() > chance) return;

        ItemStack item = event.getItem();
        if (item.getType() == Material.BOOK) {
            EnchantmentType chosen = getRandomEnchantment();
            int level = rollLevel(chosen, expCost);
            // Delay 1 tick so vanilla turns BOOK into ENCHANTED_BOOK first
            plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> {
                    ItemStack slotItem = event.getInventory().getItem(0);
                    if (slotItem != null && slotItem.getType() != Material.AIR) {
                        manager.addEnchantment(slotItem, chosen, level);
                    }
                }, 1L
            );
            return;
        }

        List<EnchantmentType> compatible = getCompatibleEnchantments(item);
        if (compatible.isEmpty()) return;

        EnchantmentType chosen = compatible.get(ThreadLocalRandom.current().nextInt(compatible.size()));
        int level = rollLevel(chosen, expCost);
        manager.addEnchantment(item, chosen, level);
    }

    // 2. Librarian Villager Trades integration
    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.getProfession() != Villager.Profession.LIBRARIAN) return;

        // 35% chance for a new librarian trade to offer a Custom Enchanted Book
        if (ThreadLocalRandom.current().nextDouble() > 0.35) return;

        EnchantmentType chosen = getRandomEnchantment();
        int level = ThreadLocalRandom.current().nextInt(1, chosen.getMaxLevel() + 1);
        ItemStack customBook = manager.createEnchantedBook(chosen, level);

        int emeraldPrice = Math.min(64, 12 + (level * 8) + ThreadLocalRandom.current().nextInt(0, 10));
        MerchantRecipe recipe = new MerchantRecipe(customBook, 0, 12, true, 15, 0.2f);
        recipe.addIngredient(new ItemStack(Material.EMERALD, emeraldPrice));
        recipe.addIngredient(new ItemStack(Material.BOOK, 1));

        event.setRecipe(recipe);
    }

    // 3. Natural Chest Loot integration (Dungeons, Mineshafts, Strongholds, Temples, Bastions, etc.)
    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (!event.getLootTable().getKey().getKey().startsWith("chests/")) return;

        // 40% chance to include 1 custom enchanted book in generated loot containers
        if (ThreadLocalRandom.current().nextDouble() > 0.40) return;

        EnchantmentType chosen = getRandomEnchantment();
        int level = ThreadLocalRandom.current().nextInt(1, chosen.getMaxLevel() + 1);
        event.getLoot().add(manager.createEnchantedBook(chosen, level));
    }

    // 4. Anvil Combining (Book + Gear, Book + Book, Gear + Gear)
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);
        if (first == null || second == null) return;

        Map<EnchantmentType, Integer> firstEnchants = manager.getEnchantments(first);
        Map<EnchantmentType, Integer> secondEnchants = manager.getEnchantments(second);
        if (firstEnchants.isEmpty() && secondEnchants.isEmpty()) return;

        ItemStack vanillaResult = event.getResult();
        if (vanillaResult == null
                && (second.getType() != Material.ENCHANTED_BOOK || secondEnchants.isEmpty())) return;

        ItemStack result = vanillaResult != null ? vanillaResult.clone() : first.clone();
        boolean changed = vanillaResult != null && !firstEnchants.isEmpty();
        int extraCost = 0;

        // Re-apply existing custom enchants from first item
        for (Map.Entry<EnchantmentType, Integer> entry : firstEnchants.entrySet()) {
            manager.addEnchantment(result, entry.getKey(), entry.getValue());
        }

        // Merge custom enchants from second item
        for (Map.Entry<EnchantmentType, Integer> entry : secondEnchants.entrySet()) {
            EnchantmentType type = entry.getKey();
            int addLvl = entry.getValue();

            if (!type.canApplyTo(result) && result.getType() != Material.ENCHANTED_BOOK) continue;
            if (second.getType() != Material.ENCHANTED_BOOK && second.getType() != first.getType()) continue;

            int currentLvl = manager.getLevel(result, type);
            int finalLvl = (currentLvl == addLvl && currentLvl < type.getMaxLevel())
                    ? currentLvl + 1
                    : Math.max(currentLvl, addLvl);

            if (finalLvl > currentLvl) {
                manager.addEnchantment(result, type, finalLvl);
                changed = true;
                extraCost += finalLvl * 3;
            }
        }

        if (changed) {
            event.setResult(result);
        }
        if (extraCost > 0) {
            inv.setRepairCost(Math.max(1, inv.getRepairCost() + extraCost));
        }
    }

    // 5. Grindstone Disenchanting
    @EventHandler
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        ItemStack result = event.getResult();
        if (result == null || !result.hasItemMeta()) return;

        Map<EnchantmentType, Integer> enchants = manager.getEnchantments(result);
        if (enchants.isEmpty()) return;

        var meta = result.getItemMeta();
        for (EnchantmentType type : enchants.keySet()) {
            meta.getPersistentDataContainer().remove(manager.getKey(type));
        }
        meta.setEnchantmentGlintOverride(null);
        result.setItemMeta(meta);
        manager.refreshLoreAndGlint(result);
        event.setResult(result);
    }

    private EnchantmentType getRandomEnchantment() {
        EnchantmentType[] all = EnchantmentType.values();
        return all[ThreadLocalRandom.current().nextInt(all.length)];
    }

    private List<EnchantmentType> getCompatibleEnchantments(ItemStack item) {
        List<EnchantmentType> list = new ArrayList<>();
        for (EnchantmentType type : EnchantmentType.values()) {
            if (type.canApplyTo(item)) {
                list.add(type);
            }
        }
        return list;
    }

    private int rollLevel(EnchantmentType type, int expCost) {
        if (type.getMaxLevel() == 1) return 1;
        if (expCost >= 28) return type.getMaxLevel();
        if (expCost >= 18) return Math.min(type.getMaxLevel(), 2);
        return 1;
    }
}
