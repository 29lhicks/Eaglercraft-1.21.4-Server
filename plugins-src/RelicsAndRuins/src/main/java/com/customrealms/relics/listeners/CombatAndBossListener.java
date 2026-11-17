package com.customrealms.relics.listeners;

import com.customrealms.relics.items.CustomItemDefinition;
import com.customrealms.relics.items.CustomItemRegistry;
import com.customrealms.relics.network.BungeeNotifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CombatAndBossListener implements Listener {
    private final JavaPlugin plugin;
    private final CustomItemRegistry registry;
    private final BungeeNotifier bungee;
    private final Set<UUID> maceAbilityGuard = new HashSet<>();
    private final NamespacedKey customBossKey = new NamespacedKey("eaglerbossesandbook", "custom_boss_id");

    public CombatAndBossListener(JavaPlugin plugin, CustomItemRegistry registry, BungeeNotifier bungee) {
        this.plugin = plugin;
        this.registry = registry;
        this.bungee = bungee;
    }

    @EventHandler
    public void onCombatHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof LivingEntity target) {
            String mainId = registry.getCustomId(attacker.getInventory().getItemInMainHand());
            String offId = registry.getCustomId(attacker.getInventory().getItemInOffHand());

            if ("venomfang_dagger".equals(mainId)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
            }
            if ("void_cleaver".equals(mainId)) {
                double maxHp = target.getAttribute(Attribute.MAX_HEALTH) != null
                        ? target.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0;
                if (target.getHealth() < maxHp * 0.4) {
                    event.setDamage(event.getDamage() * 1.5);
                }
                attacker.damage(1.0);
            }
            if (isMaceSlamFall(attacker.getFallDistance())
                    && isCustomMace(mainId)
                    && maceAbilityGuard.add(attacker.getUniqueId())) {
                try {
                    applyMaceSlamAbility(event, attacker, target, mainId);
                } finally {
                    maceAbilityGuard.remove(attacker.getUniqueId());
                }
            }
            if ("blood_chalice".equals(offId)) {
                double max = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
                attacker.setHealth(Math.min(max, attacker.getHealth() + 1.5));
            }
        }

        if (event.getEntity() instanceof Player defender) {
            String defOffId = registry.getCustomId(defender.getInventory().getItemInOffHand());
            if ("blood_chalice".equals(defOffId)) {
                event.setDamage(event.getDamage() * 1.15);
            }
            if ("ember_totem".equals(defOffId) && event.getDamager() instanceof LivingEntity attacker) {
                attacker.setFireTicks(80);
            }
        }
    }

    static boolean isMaceSlamFall(float fallDistance) {
        return fallDistance > 1.5f;
    }

    private boolean isCustomMace(String itemId) {
        return "earthshatter_maul".equals(itemId)
                || "cinderwake_mace".equals(itemId)
                || "wardenfall_mace".equals(itemId);
    }

    private void applyMaceSlamAbility(
            EntityDamageByEntityEvent event, Player attacker, LivingEntity target, String itemId
    ) {
        if ("earthshatter_maul".equals(itemId)) {
            for (Entity nearby : target.getNearbyEntities(3.0, 2.0, 3.0)) {
                if (nearby instanceof LivingEntity living && nearby != attacker) {
                    living.damage(4.0, attacker);
                    knockbackFrom(living, target, attacker, 0.8, 0.4);
                }
            }
        } else if ("cinderwake_mace".equals(itemId)) {
            target.setFireTicks(Math.max(target.getFireTicks(), 100));
            for (Entity nearby : target.getNearbyEntities(3.0, 2.0, 3.0)) {
                if (nearby instanceof LivingEntity living && nearby != attacker) {
                    living.setFireTicks(Math.max(living.getFireTicks(), 80));
                }
            }
        } else if ("wardenfall_mace".equals(itemId)) {
            event.setDamage(event.getDamage() + 6.0);
            for (Entity nearby : target.getNearbyEntities(4.0, 3.0, 4.0)) {
                if (nearby instanceof LivingEntity living && nearby != attacker) {
                    living.damage(6.0, attacker);
                    knockbackFrom(living, target, attacker, 1.1, 0.6);
                }
            }
            double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
            attacker.setHealth(Math.min(maxHealth, attacker.getHealth() + 2.0));
        }
    }

    private void knockbackFrom(Entity entity, Entity center, Player attacker, double strength, double lift) {
        Vector direction = entity.getLocation().toVector().subtract(center.getLocation().toVector()).setY(0);
        if (direction.lengthSquared() < 0.0001) {
            direction = attacker.getLocation().getDirection().setY(0);
        }
        if (direction.lengthSquared() < 0.0001) {
            direction = new Vector(0, 0, 1);
        }
        entity.setVelocity(direction.normalize().multiply(strength).setY(lift));
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player p && event.getForce() >= 0.95f) {
            String id = registry.getCustomId(event.getBow());
            if ("starfall_longbow".equals(id)) {
                event.getProjectile().setMetadata("starfall_arrow", new FixedMetadataValue(plugin, true));
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().hasMetadata("starfall_arrow")) {
            event.getEntity().getWorld().strikeLightning(event.getEntity().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();

        if (killer != null) {
            String mainId = registry.getCustomId(killer.getInventory().getItemInMainHand());
            if ("soul_reaver_scythe".equals(mainId)) {
                double max = killer.getAttribute(Attribute.MAX_HEALTH).getValue();
                killer.setHealth(Math.min(max, killer.getHealth() + 2.0));
            }
        }

        String customBossId = dead.getPersistentDataContainer().get(customBossKey, PersistentDataType.STRING);
        if (customBossId == null) {
            return;
        }

        if ("goliath".equals(customBossId)) {
            ItemStack wardenfallMace = registry.createItem("wardenfall_mace");
            if (wardenfallMace == null) {
                plugin.getLogger().severe("Could not create the guaranteed Goliath drop: wardenfall_mace");
            } else {
                event.getDrops().add(wardenfallMace);
                if (killer != null) {
                    killer.sendMessage(Component.text("Custom Boss Drop: Wardenfall Mace!", NamedTextColor.DARK_AQUA));
                    bungee.broadcastRareDropAcrossNetwork(killer,
                            "§3[Relics] §e" + killer.getName() + " §7defeated Goliath and obtained the §bWardenfall Mace§7!");
                }
            }
        }

        double dropChance = plugin.getConfig().getDouble("boss-drop-chance", 0.65);
        if (ThreadLocalRandom.current().nextDouble() <= dropChance) {
            List<CustomItemDefinition> bossItems = registry.getBySource(CustomItemDefinition.Source.BOSS_DROP)
                    .stream()
                    .filter(item -> !"wardenfall_mace".equals(item.getId()))
                    .toList();
            if (!bossItems.isEmpty()) {
                CustomItemDefinition chosen = bossItems.get(ThreadLocalRandom.current().nextInt(bossItems.size()));
                ItemStack drop = registry.createItem(chosen.getId());
                if (drop == null) {
                    plugin.getLogger().severe("Could not create custom boss drop: " + chosen.getId());
                    return;
                }
                event.getDrops().add(drop);
                if (killer != null) {
                    killer.sendMessage(Component.text("Custom Boss Relic Dropped: " + chosen.getDisplayName() + "!", NamedTextColor.LIGHT_PURPLE));
                    bungee.broadcastRareDropAcrossNetwork(killer,
                            "§d[Relics] §e" + killer.getName() + " §7obtained boss relic §6" + chosen.getDisplayName() + "§7!");
                }
            } else {
                plugin.getLogger().warning("No custom relics are registered as boss drops.");
            }
        }
    }
}
