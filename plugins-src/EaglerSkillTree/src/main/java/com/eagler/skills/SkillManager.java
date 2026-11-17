package com.eagler.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class SkillManager {

    private final JavaPlugin plugin;
    private final NamespacedKey pointsKey;
    private final NamespacedKey modHealthKey;
    private final NamespacedKey modDamageKey;
    private final NamespacedKey modSpeedKey;

    public SkillManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pointsKey = new NamespacedKey(plugin, "skill_points");
        this.modHealthKey = new NamespacedKey(plugin, "skill_bonus_health");
        this.modDamageKey = new NamespacedKey(plugin, "skill_bonus_damage");
        this.modSpeedKey = new NamespacedKey(plugin, "skill_bonus_speed");
    }

    public int getSkillPoints(Player player) {
        return player.getPersistentDataContainer().getOrDefault(pointsKey, PersistentDataType.INTEGER, 0);
    }

    public void setSkillPoints(Player player, int points) {
        player.getPersistentDataContainer().set(pointsKey, PersistentDataType.INTEGER, Math.max(0, points));
    }

    public void addSkillPoints(Player player, int amount, String reason) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Skill point grants must be positive.");
        }
        int updated = getSkillPoints(player) + amount;
        setSkillPoints(player, updated);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "★ SKILL POINT EARNED! "
                + ChatColor.YELLOW + "+" + amount + " Skill Point(s) (" + reason + "). "
                + ChatColor.AQUA + "Type /skills to upgrade!");
    }

    public int getRank(Player player, SkillNode node) {
        NamespacedKey key = new NamespacedKey(plugin, "node_" + node.getId());
        return player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public void setRank(Player player, SkillNode node, int rank) {
        NamespacedKey key = new NamespacedKey(plugin, "node_" + node.getId());
        int clamped = Math.max(0, Math.min(node.getMaxRank(), rank));
        player.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, clamped);
        applyAttributeBonuses(player);
    }

    public boolean hasUnlockedPrerequisite(Player player, SkillNode node) {
        if (node.getPrerequisiteId() == null) return true;
        SkillNode prereq = SkillNode.fromId(node.getPrerequisiteId());
        return prereq == null || getRank(player, prereq) >= 1;
    }

    public int getTotalSpentPoints(Player player) {
        int spent = 0;
        for (SkillNode node : SkillNode.values()) {
            spent += getRank(player, node) * node.getCostPerRank();
        }
        return spent;
    }

    public void resetSkills(Player player) {
        int refunded = getTotalSpentPoints(player);
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        for (SkillNode node : SkillNode.values()) {
            pdc.remove(new NamespacedKey(plugin, "node_" + node.getId()));
        }
        setSkillPoints(player, getSkillPoints(player) + refunded);
        applyAttributeBonuses(player);
    }

    /**
     * Uses 1.21.4 NamespacedKey AttributeModifiers so skill bonuses stack safely
     * with EaglerCustomGear's baseValue updates without ever overwriting armor set bonuses!
     */
    public void applyAttributeBonuses(Player player) {
        // 1. Vitality Health (+2 HP per rank)
        int vitRank = getRank(player, SkillNode.VIT_HEALTH);
        updateModifier(player, Attribute.MAX_HEALTH, modHealthKey, vitRank * 2.0, AttributeModifier.Operation.ADD_NUMBER);

        // 2. Combat Might (+1.0 Attack Damage per rank)
        int mightRank = getRank(player, SkillNode.COMBAT_MIGHT);
        updateModifier(player, Attribute.ATTACK_DAMAGE, modDamageKey, mightRank * 1.0, AttributeModifier.Operation.ADD_NUMBER);

        // 3. Agility Speed (+6% Scalar Speed per rank)
        int speedRank = getRank(player, SkillNode.AGI_SPEED);
        updateModifier(player, Attribute.MOVEMENT_SPEED, modSpeedKey, speedRank * 0.06, AttributeModifier.Operation.ADD_SCALAR);
    }

    private void updateModifier(Player player, Attribute attribute, NamespacedKey key, double value, AttributeModifier.Operation op) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;

        AttributeModifier current = inst.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(key))
                .findFirst()
                .orElse(null);

        if (value <= 0.0) {
            if (current != null) {
                inst.removeModifier(current);
            }
            return;
        }

        if (current != null
                && Math.abs(current.getAmount() - value) < 0.0001
                && current.getOperation() == op) {
            return;
        }
        if (current != null) {
            inst.removeModifier(current);
        }
        inst.addModifier(new AttributeModifier(key, value, op, EquipmentSlotGroup.ANY));
    }

    public void startAttributeSyncTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                applyAttributeBonuses(player);
            }
        }, 40L, 40L);
    }
}
