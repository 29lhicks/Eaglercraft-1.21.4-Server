package com.eagler.bosses;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public class BossListener implements Listener {

    private final JavaPlugin plugin;
    private final ItemBridge itemBridge;
    private final RecipeBookManager recipeBookManager;
    private final BossManager bossManager;
    private final NamespacedKey starterBookKey;

    public BossListener(JavaPlugin plugin, ItemBridge itemBridge, RecipeBookManager recipeBookManager, BossManager bossManager) {
        this.plugin = plugin;
        this.itemBridge = itemBridge;
        this.recipeBookManager = recipeBookManager;
        this.bossManager = bossManager;
        this.starterBookKey = new NamespacedKey(plugin, "received_starter_tome");
    }

    // Give every player the Artisan's Recipe Tome on their first join + unlock crystal recipes
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.discoverRecipe(new NamespacedKey(plugin, "recipe_artisan_tome"));
        player.discoverRecipe(new NamespacedKey(plugin, "recipe_ignis_crystal"));
        player.discoverRecipe(new NamespacedKey(plugin, "recipe_boreas_crystal"));
        player.discoverRecipe(new NamespacedKey(plugin, "recipe_malakor_crystal"));
        player.discoverRecipe(new NamespacedKey(plugin, "recipe_goliath_crystal"));

        if (!player.getPersistentDataContainer().has(starterBookKey, PersistentDataType.BYTE)) {
            recipeBookManager.giveRecipeBook(player);
            player.getPersistentDataContainer().set(starterBookKey, PersistentDataType.BYTE, (byte) 1);
        }
    }

    // Right-click Recipe Tome (opens /recipes GUI) or Boss Summoning Crystal (spawns Boss)
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) return;

        // 1. Artisan's Recipe Tome -> Opens the complete recipe GUI
        if (recipeBookManager.isRecipeBook(hand)) {
            if (plugin.getServer().getPluginManager().isPluginEnabled("EaglerCustomGear")) {
                event.setCancelled(true);
                player.performCommand("recipes");
            }
            return;
        }

        // 2. Boss Summoning Crystal
        CustomMaterialType matType = itemBridge.identifyMaterial(hand);
        if (matType != null && matType.isCrystal() && matType.getSummonedBoss() != null) {
            event.setCancelled(true);
            Location spawnLoc = event.getClickedBlock() != null
                    ? event.getClickedBlock().getLocation().add(0.5, 1.2, 0.5)
                    : player.getLocation().add(player.getLocation().getDirection().multiply(3));

            hand.setAmount(hand.getAmount() - 1);
            bossManager.spawnBoss(spawnLoc, matType.getSummonedBoss());
        }
    }

    // 10% chance for natural hostile monster spawns to also spawn a nearby Elite Mob
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;
        if (!(event.getEntity() instanceof Monster)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.10) return;

        EliteMobType[] elites = EliteMobType.values();
        EliteMobType chosen = elites[ThreadLocalRandom.current().nextInt(elites.length)];
        bossManager.spawnElite(event.getLocation(), chosen);
    }

    // Custom Loot Drops for Bosses and Elite Mobs
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        // 1. Major Boss Drops
        BossType bossType = bossManager.identifyBoss(dead);
        if (bossType != null) {
            bossManager.removeBossBar(dead.getUniqueId());
            event.getDrops().clear();
            event.setDroppedExp(500);

            // Guaranteed 2-4 Custom Boss Materials
            int matCount = rng.nextInt(2, 5);
            event.getDrops().add(itemBridge.createMaterial(bossType.getGuaranteedMaterial(), matCount));

            // Guaranteed 1 Custom Armor Piece or Weapon from EaglerCustomGear
            String gearId = bossType.getCustomGearRewards().get(rng.nextInt(bossType.getCustomGearRewards().size()));
            event.getDrops().add(itemBridge.createCustomGearById(gearId));

            // Guaranteed 1 Level II Custom Enchanted Book from EaglerCustomEnchants
            String enchKey = bossType.getCustomEnchantRewards().get(rng.nextInt(bossType.getCustomEnchantRewards().size()));
            event.getDrops().add(itemBridge.createCustomEnchantedBook(enchKey, 2));

            // Bonus valuables
            event.getDrops().add(new ItemStack(Material.DIAMOND, rng.nextInt(3, 8)));
            event.getDrops().add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
            return;
        }

        // 2. Elite Mob Drops
        EliteMobType eliteType = bossManager.identifyElite(dead);
        if (eliteType != null) {
            event.setDroppedExp(60);

            // 65% chance to drop 1 Custom Boss Material
            if (rng.nextDouble() < 0.65) {
                event.getDrops().add(itemBridge.createMaterial(eliteType.getDropMaterial(), 1));
            }

            // 20% chance to drop a Custom Enchanted Book
            if (rng.nextDouble() < 0.20) {
                String enchKey = eliteType.getRareEnchantDrops().get(rng.nextInt(eliteType.getRareEnchantDrops().size()));
                event.getDrops().add(itemBridge.createCustomEnchantedBook(enchKey, 1));
            }

            // 15% chance to drop a Custom Gear Piece
            if (rng.nextDouble() < 0.15) {
                String gearId = eliteType.getRareGearDrops().get(rng.nextInt(eliteType.getRareGearDrops().size()));
                event.getDrops().add(itemBridge.createCustomGearById(gearId));
            }
        }
    }
}
