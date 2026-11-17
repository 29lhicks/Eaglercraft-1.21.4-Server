package com.eagler.skills;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SkillListener implements Listener {

    private final JavaPlugin plugin;
    private final SkillManager skillManager;
    private final SkillTreeGui skillGui;
    private final NamespacedKey bossKey;
    private final Map<UUID, Long> phoenixCooldowns = new HashMap<>();
    private final Set<UUID> playersProcessingLevelMilestone = new HashSet<>();

    public SkillListener(JavaPlugin plugin, SkillManager skillManager, SkillTreeGui skillGui) {
        this.plugin = plugin;
        this.skillManager = skillManager;
        this.skillGui = skillGui;
        // Matches EaglerBossesAndBook's exact NamespacedKey for World Bosses
        this.bossKey = new NamespacedKey("eaglerbossesandbook", "custom_boss_id");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        skillManager.applyAttributeBonuses(event.getPlayer());
    }

    // Level 40 milestones grant one point per 40 levels and retain any remainder.
    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (event.getNewLevel() < 40 || !playersProcessingLevelMilestone.add(playerId)) return;

        try {
            int milestones = event.getNewLevel() / 40;
            int remainingLevels = event.getNewLevel() % 40;
            player.setLevel(remainingLevels);
            player.setExp(0f);
            skillManager.addSkillPoints(player, milestones, "Reached Level 40 EXP milestone");
        } finally {
            playersProcessingLevelMilestone.remove(playerId);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        phoenixCooldowns.remove(playerId);
        playersProcessingLevelMilestone.remove(playerId);
    }

    // 2. World Boss Kill -> Grants +1 Skill Point (+ Fortune EXP bonus)
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer == null) return;

        // Check if defeated entity is a World Boss from EaglerBossesAndBook
        String bossId = dead.getPersistentDataContainer().get(bossKey, PersistentDataType.STRING);
        if (bossId != null) {
            skillManager.addSkillPoints(killer, 1, "Defeated World Boss");
        }

        // Pathfinder Tier 2: Treasure Hunter (+25% EXP & Double Drop chance per rank)
        int fortuneRank = skillManager.getRank(killer, SkillNode.AGI_FORTUNE);
        if (fortuneRank > 0) {
            int bonusExp = (int) Math.round(event.getDroppedExp() * (0.25 * fortuneRank));
            event.setDroppedExp(event.getDroppedExp() + bonusExp);

            if (bossId == null && ThreadLocalRandom.current().nextDouble() < (0.10 * fortuneRank)) {
                if (!event.getDrops().isEmpty()) {
                    var extra = event.getDrops().get(0).clone();
                    event.getDrops().add(extra);
                }
            }
        }
    }

    // 3. GUI Click Handling for /skills
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(SkillTreeGui.GUI_TITLE)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();

        // Reset Button (Slot 41)
        if (slot == 41) {
            if (skillManager.getTotalSpentPoints(player) == 0) {
                player.sendMessage(ChatColor.YELLOW + "You don't have any spent skill points to reset.");
                return;
            }
            if (player.getLevel() < 10) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(ChatColor.RED + "You need at least 10 EXP Levels to reset your skill tree!");
                return;
            }
            player.setLevel(player.getLevel() - 10);
            skillManager.resetSkills(player);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
            player.sendMessage(ChatColor.GREEN + "Skill tree reset! All points have been refunded.");
            skillGui.openSkillTree(player);
            return;
        }

        // Check if clicked slot is a Skill Node
        SkillNode clickedNode = SkillNode.fromSlot(slot);
        if (clickedNode == null) return;

        int currentRank = skillManager.getRank(player, clickedNode);
        if (currentRank >= clickedNode.getMaxRank()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(ChatColor.GOLD + "That skill is already at maximum rank!");
            return;
        }

        if (!skillManager.hasUnlockedPrerequisite(player, clickedNode)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(ChatColor.RED + "Unlock the previous tier in this branch first!");
            return;
        }

        int points = skillManager.getSkillPoints(player);
        int cost = clickedNode.getCostPerRank();
        if (points < cost) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(ChatColor.RED + "You need " + cost + " Skill Point(s) to upgrade this node!");
            return;
        }

        skillManager.setSkillPoints(player, points - cost);
        skillManager.setRank(player, clickedNode, currentRank + 1);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.sendMessage(ChatColor.GREEN + "Upgraded " + clickedNode.getDisplayName()
                + ChatColor.GREEN + " to Rank " + (currentRank + 1) + "!");

        skillGui.openSkillTree(player);
    }

    // 4. Combat Skill Triggers (Crits, Lifesteal, Damage Reduction, Evasion)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombatDamage(EntityDamageByEntityEvent event) {
        // Attacker Skills
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof LivingEntity target) {
            // Warlord Tier 2: Executioner's Edge (+8% damage per rank to targets below 60% HP)
            int critRank = skillManager.getRank(attacker, SkillNode.COMBAT_CRIT);
            if (critRank > 0 && target.getAttribute(Attribute.MAX_HEALTH) != null) {
                double maxHp = target.getAttribute(Attribute.MAX_HEALTH).getValue();
                if (target.getHealth() <= maxHp * 0.60) {
                    event.setDamage(event.getDamage() * (1.0 + (0.08 * critRank)));
                }
            }

            // Warlord Tier 3: Sanguine Thirst (+0.5 Hearts / 1.0 HP healed on melee hit per rank)
            int lsRank = skillManager.getRank(attacker, SkillNode.COMBAT_LIFESTEAL);
            if (lsRank > 0 && attacker.getAttribute(Attribute.MAX_HEALTH) != null) {
                double maxHp = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
                attacker.setHealth(Math.min(maxHp, attacker.getHealth() + (1.0 * lsRank)));
            }
        }

        // Defender Skills
        if (event.getEntity() instanceof Player defender) {
            // Pathfinder Tier 3: Phantom Reflexes (+6% Dodge Chance per rank)
            int evasionRank = skillManager.getRank(defender, SkillNode.AGI_EVASION);
            if (evasionRank > 0 && ThreadLocalRandom.current().nextDouble() < (0.06 * evasionRank)) {
                event.setCancelled(true);
                defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.6f);
                defender.spawnParticle(Particle.CLOUD, defender.getLocation().add(0, 1, 0), 8, 0.2, 0.3, 0.2, 0.05);
                return;
            }

            // Guardian Tier 2: Obsidian Skin (+4% Damage Reduction per rank)
            int armorRank = skillManager.getRank(defender, SkillNode.VIT_ARMOR);
            if (armorRank > 0) {
                event.setDamage(event.getDamage() * (1.0 - (0.04 * armorRank)));
            }
        }
    }

    // 5. Guardian Tier 3: Phoenix Rebirth (Fatal Blow Survival)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getHealth() - event.getFinalDamage() > 0.0) return;

        int phoenixRank = skillManager.getRank(player, SkillNode.VIT_SECOND_WIND);
        if (phoenixRank <= 0) return;

        long now = System.currentTimeMillis();
        long readyAt = phoenixCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now < readyAt) return;

        // Trigger Phoenix Rebirth
        event.setCancelled(true);
        phoenixCooldowns.put(player.getUniqueId(), now + 180_000L); // 3 minute cooldown

        double maxHp = player.getAttribute(Attribute.MAX_HEALTH) != null ? player.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0;
        player.setHealth(Math.min(maxHp, 8.0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0));

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.1f);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 0.2);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✦ PHOENIX REBIRTH! "
                + ChatColor.YELLOW + "Saved from fatal damage! (3m Cooldown)");
    }
}
