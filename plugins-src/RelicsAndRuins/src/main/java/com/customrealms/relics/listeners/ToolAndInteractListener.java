package com.customrealms.relics.listeners;

import com.customrealms.relics.items.CustomItemRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ToolAndInteractListener implements Listener {
    private final JavaPlugin plugin;
    private final CustomItemRegistry registry;
    private final Set<Block> processing = new HashSet<>();
    private final Map<UUID, Map<String, Long>> artifactCooldowns = new HashMap<>();

    public ToolAndInteractListener(JavaPlugin plugin, CustomItemRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @EventHandler
    public void onArtifactUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND
                || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Player player = event.getPlayer();
        String artifactId = registry.getCustomId(player.getInventory().getItemInOffHand());
        long cooldownSeconds = switch (artifactId == null ? "" : artifactId) {
            case "riftwalker_charm" -> 15;
            case "stormcaller_totem" -> 25;
            case "frostbound_idol" -> 20;
            case "soulbloom_pendant" -> 30;
            case "bastion_heart" -> 40;
            default -> 0;
        };
        if (cooldownSeconds == 0) return;

        event.setCancelled(true);
        if (!startArtifactCooldown(player, artifactId, cooldownSeconds)) return;

        switch (artifactId) {
            case "riftwalker_charm" -> useRiftwalkerCharm(player);
            case "stormcaller_totem" -> useStormcallerTotem(player);
            case "frostbound_idol" -> useFrostboundIdol(player);
            case "soulbloom_pendant" -> useSoulbloomPendant(player);
            case "bastion_heart" -> useBastionHeart(player);
            default -> throw new IllegalStateException("Unhandled off-hand artifact: " + artifactId);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        artifactCooldowns.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if ("treecapitator_axe".equals(registry.getCustomId(player.getInventory().getItemInMainHand()))
                && !Tag.LOGS.isTagged(event.getBlock().getType())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 0, true, false, true));
        }
    }

    private boolean startArtifactCooldown(Player player, String artifactId, long cooldownSeconds) {
        long now = System.nanoTime();
        Map<String, Long> playerCooldowns = artifactCooldowns.computeIfAbsent(
                player.getUniqueId(), ignored -> new HashMap<>());
        long readyAt = playerCooldowns.getOrDefault(artifactId, 0L);
        if (readyAt > now) {
            long secondsLeft = (readyAt - now + 999_999_999L) / 1_000_000_000L;
            player.sendMessage(Component.text("Artifact recharging: " + secondsLeft + "s", NamedTextColor.GRAY));
            return false;
        }
        playerCooldowns.put(artifactId, now + cooldownSeconds * 1_000_000_000L);
        return true;
    }

    private void useRiftwalkerCharm(Player player) {
        Vector dash = player.getLocation().getDirection().normalize().multiply(1.35).setY(0.35);
        player.setVelocity(dash);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 24, 0.3, 0.5, 0.3, 0.2);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.4f);
        player.sendMessage(Component.text("Rift Dash!", NamedTextColor.LIGHT_PURPLE));
    }

    private void useStormcallerTotem(Player player) {
        int strikes = 0;
        for (Entity nearby : player.getNearbyEntities(8.0, 5.0, 8.0)) {
            if (nearby instanceof LivingEntity target
                    && (target instanceof Monster || target instanceof Player)
                    && target != player
                    && strikes < 4) {
                target.damage(6.0, player);
                target.getWorld().strikeLightningEffect(target.getLocation());
                strikes++;
            }
        }
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 0.8f, 1.0f);
        player.sendMessage(Component.text(
                strikes == 0 ? "Stormcaller finds no enemies nearby." : "Stormcall struck " + strikes + " enemies!",
                NamedTextColor.BLUE));
    }

    private void useFrostboundIdol(Player player) {
        int frozen = 0;
        for (Entity nearby : player.getNearbyEntities(6.0, 4.0, 6.0)) {
            if (nearby instanceof LivingEntity target
                    && (target instanceof Monster || target instanceof Player)
                    && target != player) {
                target.setFreezeTicks(Math.max(target.getFreezeTicks(), 120));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 3, true, true, true));
                frozen++;
            }
        }
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 40, 3, 1, 3, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8f, 0.7f);
        player.sendMessage(Component.text("Frostbound freeze affected " + frozen + " enemies!", NamedTextColor.AQUA));
    }

    private void useSoulbloomPendant(Player player) {
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + 6.0));
        for (PotionEffectType effect : new PotionEffectType[]{
                PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.WEAKNESS,
                PotionEffectType.SLOWNESS, PotionEffectType.BLINDNESS
        }) {
            player.removePotionEffect(effect);
        }
        player.setFireTicks(0);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 18, 0.5, 0.7, 0.5);
        player.playSound(player.getLocation(), Sound.ITEM_HONEY_BOTTLE_DRINK, 0.8f, 1.2f);
        player.sendMessage(Component.text("Soulbloom restored 6 health and cleansed ailments.", NamedTextColor.GREEN));
    }

    private void useBastionHeart(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0, true, true, true));
        player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation().add(0, 1, 0), 32,
                0.6, 0.8, 0.6, Material.AMETHYST_BLOCK.createBlockData());
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.7f, 0.8f);
        player.sendMessage(Component.text("Bastion barrier active for 5 seconds!", NamedTextColor.GOLD));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (processing.contains(block)) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        String id = registry.getCustomId(tool);
        if (id == null) return;

        if ("veinminer_pickaxe".equals(id) && Tag.MINEABLE_PICKAXE.isTagged(block.getType())) {
            var direction = player.getLocation().getDirection();
            double absX = Math.abs(direction.getX());
            double absY = Math.abs(direction.getY());
            double absZ = Math.abs(direction.getZ());
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        if (!isOnTunnelFace(dx, dy, dz, absX, absY, absZ)) continue;
                        Block rel = block.getRelative(dx, dy, dz);
                        if (Tag.MINEABLE_PICKAXE.isTagged(rel.getType()) && rel.getType() != Material.BEDROCK) {
                            processing.add(rel);
                            try {
                                rel.breakNaturally(tool);
                            } finally {
                                processing.remove(rel);
                            }
                        }
                    }
                }
            }
            player.setFoodLevel(Math.max(0, player.getFoodLevel() - 2));
        }

        if ("treecapitator_axe".equals(id) && Tag.LOGS.isTagged(block.getType())) {
            Block current = block.getRelative(BlockFace.UP);
            int count = 0;
            while (Tag.LOGS.isTagged(current.getType()) && count < 32) {
                processing.add(current);
                current.breakNaturally(tool);
                processing.remove(current);
                current = current.getRelative(BlockFace.UP);
                count++;
            }
        }

        if ("molten_shovel".equals(id)) {
            if (block.getType() == Material.SAND || block.getType() == Material.RED_SAND) {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GLASS));
            } else if (block.getType() == Material.CLAY) {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.TERRACOTTA));
            }
        }

        if ("harvester_scythe".equals(id)
                && block.getBlockData() instanceof Ageable ageable
                && ageable.getAge() == ageable.getMaximumAge()) {
            event.setCancelled(true);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Block crop = block.getRelative(dx, 0, dz);
                    if (crop.getBlockData() instanceof Ageable cropAge
                            && cropAge.getAge() == cropAge.getMaximumAge()) {
                        for (ItemStack drop : crop.getDrops(tool)) {
                            crop.getWorld().dropItemNaturally(crop.getLocation(), drop.clone());
                            crop.getWorld().dropItemNaturally(crop.getLocation(), drop.clone());
                        }
                        cropAge.setAge(0);
                        crop.setBlockData(cropAge, true);
                    }
                }
            }
            player.damageItemStack(EquipmentSlot.HAND, 1);
        }
    }

    private boolean isOnTunnelFace(int dx, int dy, int dz, double absX, double absY, double absZ) {
        if (absY > absX && absY > absZ) return dy == 0;
        if (absX > absZ) return dx == 0;
        return dz == 0;
    }
}
