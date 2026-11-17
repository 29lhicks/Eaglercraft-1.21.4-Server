package com.customrealms.relics.listeners;

import com.customrealms.relics.items.CustomItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class PassiveBuffNerfTask extends BukkitRunnable implements Listener {
    private final JavaPlugin plugin;
    private final CustomItemRegistry registry;
    private final NamespacedKey wardenSprintPenaltyKey;

    public PassiveBuffNerfTask(JavaPlugin plugin, CustomItemRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
        this.wardenSprintPenaltyKey = new NamespacedKey(plugin, "warden_greaves_sprint_penalty");
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Set<String> equipped = new HashSet<>();
            for (ItemStack armor : p.getInventory().getArmorContents()) {
                String id = registry.getCustomId(armor);
                if (id != null) equipped.add(id);
            }
            String offhandId = registry.getCustomId(p.getInventory().getItemInOffHand());
            if (offhandId != null) equipped.add(offhandId);
            String mainhandId = registry.getCustomId(p.getInventory().getItemInMainHand());
            if (mainhandId != null) equipped.add(mainhandId);

            boolean wearingWardenGreaves = "warden_greaves".equals(
                    registry.getCustomId(p.getInventory().getLeggings()));
            updateWardenSprintPenalty(p, wearingWardenGreaves && p.isSprinting());
            if (equipped.isEmpty()) continue;

            if (equipped.contains("titan_plate_chestplate")) {
                buff(p, PotionEffectType.RESISTANCE, 0);
            }
            if (equipped.contains("zephyr_hood")) {
                buff(p, PotionEffectType.NIGHT_VISION, 0);
            }
            if (equipped.contains("zephyr_boots")) {
                buff(p, PotionEffectType.JUMP_BOOST, 1);
            }
            if (equipped.contains("abyssal_chestplate")) {
                p.removePotionEffect(PotionEffectType.DARKNESS);
                if (p.getWorld().isDayTime() && p.getLocation().getBlock().getLightFromSky() > 12) {
                    buff(p, PotionEffectType.WEAKNESS, 0);
                }
            }
            if (equipped.contains("solar_leggings")) {
                buff(p, PotionEffectType.FIRE_RESISTANCE, 0);
                if (p.isInWater()) {
                    p.damage(1.0);
                }
            }
            if (equipped.contains("bloodforged_helm")) {
                buff(p, PotionEffectType.GLOWING, 0);
            }

            if ("gravity_hammer".equals(mainhandId)) {
                buff(p, PotionEffectType.HASTE, 1);
            }

            if ("heart_of_the_abyss".equals(offhandId)) {
                buff(p, PotionEffectType.REGENERATION, 0);
            }
            if ("chronos_hourglass".equals(offhandId)) {
                buff(p, PotionEffectType.HUNGER, 1);
            }
            if ("aegis_prism".equals(offhandId)) {
                buff(p, PotionEffectType.RESISTANCE, 0);
            }
            if ("ember_totem".equals(offhandId)) {
                buff(p, PotionEffectType.FIRE_RESISTANCE, 0);
                if (p.isInWaterOrRain()) {
                    buff(p, PotionEffectType.SLOWNESS, 0);
                    buff(p, PotionEffectType.WEAKNESS, 0);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSprintToggle(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        boolean wearingGreaves = "warden_greaves".equals(
                registry.getCustomId(player.getInventory().getLeggings()));
        updateWardenSprintPenalty(player, wearingGreaves && event.isSprinting());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        updateWardenSprintPenalty(event.getPlayer(), false);
    }

    public void clearSprintPenalties() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateWardenSprintPenalty(player, false);
        }
    }

    private void updateWardenSprintPenalty(Player player, boolean active) {
        AttributeInstance movementSpeed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        AttributeModifier current = movementSpeed.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(wardenSprintPenaltyKey))
                .findFirst()
                .orElse(null);
        if (active && current == null) {
            movementSpeed.addModifier(new AttributeModifier(
                    wardenSprintPenaltyKey, -0.10, AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                    EquipmentSlotGroup.ANY));
        } else if (!active && current != null) {
            movementSpeed.removeModifier(current);
        }
    }

    private void buff(Player p, PotionEffectType type, int amplifier) {
        p.addPotionEffect(new PotionEffect(type, 45, amplifier, true, false, true));
    }
}
