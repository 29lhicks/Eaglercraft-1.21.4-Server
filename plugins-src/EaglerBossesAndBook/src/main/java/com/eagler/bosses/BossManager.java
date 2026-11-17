package com.eagler.bosses;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class BossManager {

    private final JavaPlugin plugin;
    private final ItemBridge itemBridge;
    private final NamespacedKey bossKey;
    private final NamespacedKey eliteKey;
    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();

    public BossManager(JavaPlugin plugin, ItemBridge itemBridge) {
        this.plugin = plugin;
        this.itemBridge = itemBridge;
        this.bossKey = new NamespacedKey(plugin, "custom_boss_id");
        this.eliteKey = new NamespacedKey(plugin, "custom_elite_id");
    }

    public LivingEntity spawnBoss(Location loc, BossType type) {
        World w = loc.getWorld();
        if (w == null) return null;

        LivingEntity boss = (LivingEntity) w.spawnEntity(loc, type.getEntityType());
        boss.setCustomName(type.getDisplayName());
        boss.setCustomNameVisible(true);
        boss.setRemoveWhenFarAway(false);
        boss.getPersistentDataContainer().set(bossKey, PersistentDataType.STRING, type.getId());

        if (boss.getAttribute(Attribute.MAX_HEALTH) != null) {
            boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(type.getMaxHealth());
            boss.setHealth(type.getMaxHealth());
        }
        if (boss.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            boss.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(type.getAttackDamage());
        }
        if (boss.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            boss.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(type.getMoveSpeed());
        }
        if (boss.getAttribute(Attribute.KNOCKBACK_RESISTANCE) != null) {
            boss.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(0.85);
        }

        // Equip thematic gear visuals
        EntityEquipment eq = boss.getEquipment();
        if (eq != null) {
            switch (type) {
                case IGNIS -> {
                    eq.setHelmet(new ItemStack(Material.GOLDEN_HELMET));
                    eq.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                    eq.setItemInMainHand(new ItemStack(Material.NETHERITE_AXE));
                }
                case BOREAS -> {
                    eq.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                    eq.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                    eq.setItemInMainHand(new ItemStack(Material.TRIDENT));
                }
                case MALAKOR -> {
                    eq.setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
                }
                case GOLIATH -> {
                    boss.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, false, false));
                }
            }
            eq.setHelmetDropChance(0f);
            eq.setChestplateDropChance(0f);
            eq.setItemInMainHandDropChance(0f);
        }

        BossBar bar = Bukkit.createBossBar(type.getDisplayName(), type.getBarColor(), BarStyle.SEGMENTED_10);
        bar.setProgress(1.0);
        activeBossBars.put(boss.getUniqueId(), bar);

        w.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.2f, 0.9f);
        w.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 2);
        Bukkit.broadcastMessage(ChatColor.GOLD + "⚠ " + type.getDisplayName() + ChatColor.YELLOW + " has awakened at "
                + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "!");

        return boss;
    }

    public LivingEntity spawnElite(Location loc, EliteMobType type) {
        World w = loc.getWorld();
        if (w == null) return null;

        LivingEntity elite = (LivingEntity) w.spawnEntity(loc, type.getEntityType());
        elite.setCustomName(type.getDisplayName());
        elite.setCustomNameVisible(true);
        elite.getPersistentDataContainer().set(eliteKey, PersistentDataType.STRING, type.getId());

        if (elite.getAttribute(Attribute.MAX_HEALTH) != null) {
            elite.getAttribute(Attribute.MAX_HEALTH).setBaseValue(type.getMaxHealth());
            elite.setHealth(type.getMaxHealth());
        }
        if (elite.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            elite.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(type.getAttackDamage());
        }

        EntityEquipment eq = elite.getEquipment();
        if (eq != null) {
            eq.setHelmet(new ItemStack(type.getHelmet()));
            eq.setChestplate(new ItemStack(type.getChestplate()));
            eq.setItemInMainHand(new ItemStack(type.getWeapon()));
            eq.setHelmetDropChance(0f);
            eq.setChestplateDropChance(0f);
            eq.setItemInMainHandDropChance(0f);
        }

        return elite;
    }

    public BossType identifyBoss(Entity entity) {
        if (entity == null) return null;
        String id = entity.getPersistentDataContainer().get(bossKey, PersistentDataType.STRING);
        return id != null ? BossType.fromId(id) : null;
    }

    public EliteMobType identifyElite(Entity entity) {
        if (entity == null) return null;
        String id = entity.getPersistentDataContainer().get(eliteKey, PersistentDataType.STRING);
        return id != null ? EliteMobType.fromId(id) : null;
    }

    public void removeBossBar(UUID bossId) {
        BossBar bar = activeBossBars.remove(bossId);
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void cleanupAllBossBars() {
        for (BossBar bar : activeBossBars.values()) {
            bar.removeAll();
        }
        activeBossBars.clear();
    }

    public void startAbilityTask() {
        // Update BossBars every second
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<UUID, BossBar>> it = activeBossBars.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, BossBar> entry = it.next();
                Entity ent = Bukkit.getEntity(entry.getKey());
                BossBar bar = entry.getValue();

                if (!(ent instanceof LivingEntity boss) || boss.isDead()) {
                    bar.removeAll();
                    it.remove();
                    continue;
                }

                double maxHp = boss.getAttribute(Attribute.MAX_HEALTH) != null ? boss.getAttribute(Attribute.MAX_HEALTH).getValue() : 100.0;
                bar.setProgress(Math.max(0.0, Math.min(1.0, boss.getHealth() / maxHp)));

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld().equals(boss.getWorld()) && p.getLocation().distanceSquared(boss.getLocation()) <= 45 * 45) {
                        bar.addPlayer(p);
                    } else {
                        bar.removePlayer(p);
                    }
                }
            }
        }, 20L, 20L);

        // Execute Boss Special Abilities every 5 seconds
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID bossId : new ArrayList<>(activeBossBars.keySet())) {
                Entity ent = Bukkit.getEntity(bossId);
                if (!(ent instanceof LivingEntity boss) || boss.isDead()) continue;

                BossType type = identifyBoss(boss);
                if (type == null) continue;

                Player target = findNearestPlayer(boss, 24.0);
                if (target == null) continue;

                switch (type) {
                    case IGNIS -> {
                        Vector dir = target.getEyeLocation().toVector().subtract(boss.getEyeLocation().toVector()).normalize();
                        SmallFireball fb = boss.launchProjectile(SmallFireball.class, dir.multiply(1.3));
                        fb.setIsIncendiary(false);
                        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.2f, 0.8f);
                    }
                    case BOREAS -> {
                        target.getWorld().strikeLightningEffect(target.getLocation());
                        target.damage(5.0, boss);
                        target.setVelocity(new Vector(0, 0.75, 0));
                    }
                    case MALAKOR -> {
                        Location behind = target.getLocation().subtract(target.getLocation().getDirection().multiply(1.8));
                        behind.setY(target.getLocation().getY());
                        boss.teleport(behind);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 0));
                        boss.getWorld().playSound(behind, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.7f);
                    }
                    case GOLIATH -> {
                        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.6f);
                        boss.getWorld().spawnParticle(Particle.BLOCK, boss.getLocation(), 30, 2.0, 0.3, 2.0, Material.OBSIDIAN.createBlockData());
                        for (Entity nearby : boss.getNearbyEntities(6.0, 4.0, 6.0)) {
                            if (nearby instanceof Player p) {
                                Vector push = p.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize().multiply(1.4).setY(0.6);
                                p.setVelocity(push);
                                p.damage(6.0, boss);
                            }
                        }
                    }
                }
            }
        }, 100L, 100L);
    }

    private Player findNearestPlayer(LivingEntity source, double range) {
        Player closest = null;
        double bestDistSq = range * range;
        for (Player p : source.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
            double dSq = p.getLocation().distanceSquared(source.getLocation());
            if (dSq < bestDistSq) {
                bestDistSq = dSq;
                closest = p;
            }
        }
        return closest;
    }
}
