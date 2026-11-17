package com.eagler.gear;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class GearListener implements Listener {

    private static final String SKYBOUND_ARROW_METADATA = "eaglercustomgear_skybound_bow";
    private static final double BOOMERANG_OUTBOUND_SPEED = 1.15;
    private static final double BOOMERANG_RETURN_SPEED = 1.35;
    private static final double BOOMERANG_MAX_DISTANCE = 16.0;
    private static final int BOOMERANG_MAX_BOUNCES = 4;
    private static final int BOOMERANG_MAX_TICKS = 100;
    private static final Set<EntityType> UNDEAD_TYPES = EnumSet.of(
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED,
            EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.PHANTOM,
            EntityType.WITHER, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN,
            EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE);
    private static final Set<EntityType> ARTHROPOD_TYPES = EnumSet.of(
            EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.BEE,
            EntityType.SILVERFISH, EntityType.ENDERMITE);

    private final JavaPlugin plugin;
    private final GearManager gearManager;
    private final RecipeGuiManager guiManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, BoomerangFlight> activeBoomerangs = new HashMap<>();
    private final Set<UUID> hammerGuard = new HashSet<>();
    private final Set<UUID> grappleFallImmunity = new HashSet<>();

    public GearListener(JavaPlugin plugin, GearManager gearManager, RecipeGuiManager guiManager) {
        this.plugin = plugin;
        this.gearManager = gearManager;
        this.guiManager = guiManager;
    }

    // 1. Interactive 3x3 /recipes GUI Click Handling
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(RecipeGuiManager.GUI_TITLE_PREFIX)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot != 36 && slot != 44) return;

        int pageStart = title.indexOf('(');
        int pageEnd = title.indexOf('/', pageStart);
        if (pageStart < 0 || pageEnd < 0) {
            plugin.getLogger().warning("Recipe GUI has an invalid page title: " + title);
            return;
        }
        try {
            int currentPage = Integer.parseInt(title.substring(pageStart + 1, pageEnd)) - 1;
            guiManager.openRecipeMenu(player, slot == 36 ? currentPage - 1 : currentPage + 1);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        } catch (NumberFormatException exception) {
            plugin.getLogger().warning("Recipe GUI has an invalid page number: " + title);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().startsWith(RecipeGuiManager.GUI_TITLE_PREFIX)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareScytheAnvil(PrepareAnvilEvent event) {
        ItemStack base = event.getInventory().getItem(0);
        ItemStack addition = event.getInventory().getItem(1);
        CustomGearType baseType = gearManager.identify(base);
        if (baseType == CustomGearType.BOOMERANG_DISK) {
            applyVanillaDiskBookEnchantments(event, base, addition);
            return;
        }
        if (baseType != CustomGearType.SOULREAVER_SCYTHE
                || addition == null || !(addition.getItemMeta() instanceof EnchantmentStorageMeta book)) {
            return;
        }

        int bookLevel = book.getStoredEnchantLevel(Enchantment.SHARPNESS);
        if (bookLevel <= 0) {
            return;
        }

        int currentLevel = base.getEnchantmentLevel(Enchantment.SHARPNESS);
        int resultLevel = mergeScytheSharpnessLevel(currentLevel, bookLevel);
        if (resultLevel <= currentLevel) {
            return;
        }

        ItemStack result = event.getResult() == null ? base.clone() : event.getResult().clone();
        result.addUnsafeEnchantment(Enchantment.SHARPNESS, resultLevel);
        event.setResult(result);
        event.getInventory().setRepairCost(Math.max(event.getInventory().getRepairCost(), resultLevel * 2));
    }

    private void applyVanillaDiskBookEnchantments(PrepareAnvilEvent event, ItemStack base, ItemStack addition) {
        if (addition == null || !(addition.getItemMeta() instanceof EnchantmentStorageMeta book)
                || book.getStoredEnchants().isEmpty()) {
            return;
        }

        ItemStack result = event.getResult() == null ? base.clone() : event.getResult().clone();
        int extraCost = 0;
        for (Map.Entry<Enchantment, Integer> entry : book.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (!canApplyVanillaEnchantmentToDisk(enchantment)) continue;
            boolean conflicts = result.getEnchantments().keySet().stream()
                    .anyMatch(existing -> existing != enchantment && existing.conflictsWith(enchantment));
            if (conflicts) continue;

            int currentLevel = result.getEnchantmentLevel(enchantment);
            int bookLevel = entry.getValue();
            int resultLevel = currentLevel == bookLevel && currentLevel < enchantment.getMaxLevel()
                    ? currentLevel + 1
                    : Math.max(currentLevel, bookLevel);
            if (resultLevel > currentLevel) {
                result.addUnsafeEnchantment(enchantment, resultLevel);
                extraCost += resultLevel * 2;
            }
        }

        if (extraCost > 0) {
            event.setResult(result);
            event.getInventory().setRepairCost(Math.max(event.getInventory().getRepairCost(), extraCost));
        }
    }

    static boolean canApplyVanillaEnchantmentToDisk(Enchantment enchantment) {
        return enchantment == Enchantment.SHARPNESS
                || enchantment == Enchantment.SMITE
                || enchantment == Enchantment.BANE_OF_ARTHROPODS
                || enchantment == Enchantment.KNOCKBACK
                || enchantment == Enchantment.FIRE_ASPECT
                || enchantment == Enchantment.LOOTING
                || enchantment == Enchantment.VANISHING_CURSE;
    }

    static int mergeScytheSharpnessLevel(int currentLevel, int bookLevel) {
        if (bookLevel <= 0) {
            return currentLevel;
        }
        return Math.min(5, currentLevel == bookLevel ? currentLevel + 1
                : Math.max(currentLevel, bookLevel));
    }

    // 2. Right-Click Abilities (Weapons & Health Items)
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        CustomGearType type = gearManager.identify(hand);
        if (type == null) return;

        switch (type) {
            case CRIMSON_HEART_FRUIT -> {
                event.setCancelled(true);
                if (gearManager.addPermanentBonusHp(player, 2.0)) {
                    hand.setAmount(hand.getAmount() - 1);
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                    player.sendMessage(ChatColor.RED + "♥ Permanent Max Health increased by +1 Heart!");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "You have reached the maximum +10 Bonus Hearts!");
                }
            }
            case BANDAGE_WRAP -> {
                event.setCancelled(true);
                if (isOnCooldown(player, 3000)) return;
                double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
                player.setHealth(Math.min(maxHp, player.getHealth() + 8.0));
                hand.setAmount(hand.getAmount() - 1);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.4f);
                player.sendMessage(ChatColor.GREEN + "✦ Bandage applied (+4 Hearts).");
            }
            case VALKYRIE_SPEAR -> {
                event.setCancelled(true);
                if (isOnCooldown(player, 4000)) return;
                Vector dash = player.getLocation().getDirection().normalize().multiply(1.6).setY(0.45);
                player.setVelocity(dash);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_JUMP, 1.2f, 1.1f);
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.3, 0.2, 0.3, 0.05);
            }
            case PHANTOM_RAPIER -> {
                event.setCancelled(true);
                if (isOnCooldown(player, 5000)) return;
                Location target = player.getLocation().clone();
                Vector step = player.getLocation().getDirection().normalize();
                for (int i = 1; i <= 6; i++) {
                    Location check = player.getLocation().add(step.clone().multiply(i));
                    if (!check.getBlock().getType().isSolid() && !check.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                        target = check;
                    } else {
                        break;
                    }
                }
                player.teleport(target);
                player.getWorld().playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.3f);
                player.getWorld().spawnParticle(Particle.PORTAL, target, 25, 0.3, 0.8, 0.3, 0.1);
            }
            case BOOMERANG_DISK -> {
                event.setCancelled(true);
                throwBoomerangDisk(player, hand, type);
            }
        }
    }

    private void throwBoomerangDisk(Player player, ItemStack hand, CustomGearType type) {
        UUID ownerId = player.getUniqueId();
        if (activeBoomerangs.containsKey(ownerId)) {
            player.sendMessage(ChatColor.YELLOW + "Your boomerang disk is still returning.");
            return;
        }
        if (isOnCooldown(player, 1000)) return;

        ItemStack disk = hand.clone();
        disk.setAmount(1);
        Snowball projectile = player.launchProjectile(Snowball.class);
        projectile.setItem(disk);
        projectile.setGravity(false);
        projectile.setVelocity(new Vector());
        projectile.setShooter(player);

        boolean returnItem = player.getGameMode() != GameMode.CREATIVE;
        int originalSlot = player.getInventory().getHeldItemSlot();
        if (returnItem) {
            if (hand.getAmount() == 1) {
                player.getInventory().setItemInMainHand(null);
            } else {
                hand.setAmount(hand.getAmount() - 1);
            }
        }

        BoomerangFlight flight = new BoomerangFlight(
                player, projectile, disk, getBoomerangDamage(type), returnItem, originalSlot);
        activeBoomerangs.put(ownerId, flight);
        flight.runTaskTimer(plugin, 1L, 1L);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 0.8f, 0.9f);
    }

    static double getBoomerangDamage(CustomGearType type) {
        return switch (type) {
            case BOOMERANG_DISK -> 8.0;
            default -> 0.0;
        };
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BoomerangFlight flight = activeBoomerangs.get(event.getPlayer().getUniqueId());
        if (flight != null) {
            flight.returnToOwner(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        BoomerangFlight flight = activeBoomerangs.get(event.getPlayer().getUniqueId());
        if (flight != null) {
            flight.returnToOwner(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        BoomerangFlight flight = activeBoomerangs.get(event.getEntity().getUniqueId());
        if (flight != null) {
            flight.addToDeathDrops(event);
        }
    }

    public void dropActiveBoomerangs() {
        for (BoomerangFlight flight : List.copyOf(activeBoomerangs.values())) {
            Player owner = Bukkit.getPlayer(flight.ownerId);
            if (owner != null && owner.isOnline()) {
                flight.returnToOwner(owner);
            } else {
                flight.dropAtLastLocation();
            }
        }
    }

    private final class BoomerangFlight extends BukkitRunnable {
        private final UUID ownerId;
        private final Snowball projectile;
        private final ItemStack disk;
        private final double damage;
        private final boolean returnItem;
        private final int originalSlot;
        private final Map<UUID, Integer> lastHitTicks = new HashMap<>();
        private Vector outboundDirection;
        private Location lastLocation;
        private double outboundDistance;
        private int ticks;
        private int bounces;
        private boolean returning;
        private boolean warnedInventoryFull;

        private BoomerangFlight(
                Player owner, Snowball projectile, ItemStack disk, double damage, boolean returnItem, int originalSlot
        ) {
            this.ownerId = owner.getUniqueId();
            this.projectile = projectile;
            this.disk = disk;
            this.damage = damage;
            this.returnItem = returnItem;
            this.originalSlot = originalSlot;
            this.outboundDirection = owner.getEyeLocation().getDirection().normalize();
            this.lastLocation = projectile.getLocation();
        }

        @Override
        public void run() {
            ticks++;
            Player owner = Bukkit.getPlayer(ownerId);
            if (ticks > BOOMERANG_MAX_TICKS) {
                if (owner != null && owner.isOnline() && owner.getWorld().equals(lastLocation.getWorld())) {
                    returnToOwner(owner);
                } else {
                    dropAtLastLocation();
                }
                return;
            }
            if (owner == null || !owner.isOnline() || owner.isDead()
                    || !owner.getWorld().equals(lastLocation.getWorld())) {
                dropAtLastLocation();
                return;
            }
            if (!projectile.isValid()) {
                returnToOwner(owner);
                return;
            }

            lastLocation = projectile.getLocation();
            if (!returning && outboundDistance >= BOOMERANG_MAX_DISTANCE) {
                returning = true;
            }

            Vector movement;
            if (returning) {
                Vector toOwner = owner.getEyeLocation().toVector().subtract(lastLocation.toVector());
                if (toOwner.lengthSquared() <= 1.5 * 1.5) {
                    returnToOwner(owner);
                    return;
                }
                movement = toOwner.normalize().multiply(BOOMERANG_RETURN_SPEED);
            } else {
                movement = outboundDirection.clone().multiply(BOOMERANG_OUTBOUND_SPEED);
                if (bounceOffCollision(owner, movement)) {
                    return;
                }
                outboundDistance += movement.length();
                if (outboundDistance >= BOOMERANG_MAX_DISTANCE) {
                    returning = true;
                }
            }

            projectile.teleport(lastLocation.clone().add(movement));
        }

        private boolean bounceOffCollision(Player owner, Vector movement) {
            World world = lastLocation.getWorld();
            double distance = movement.length();
            Vector direction = movement.clone().normalize();
            RayTraceResult blockHit = world.rayTraceBlocks(
                    lastLocation, direction, distance, FluidCollisionMode.NEVER, true);
            RayTraceResult entityHit = world.rayTraceEntities(
                    lastLocation, direction, distance, 0.5,
                    entity -> entity instanceof LivingEntity
                            && !entity.getUniqueId().equals(ownerId)
                            && ticks - lastHitTicks.getOrDefault(entity.getUniqueId(), -1000) > 5);

            boolean entityHitFirst = entityHit != null
                    && (blockHit == null || entityHit.getHitPosition().distanceSquared(lastLocation.toVector())
                    < blockHit.getHitPosition().distanceSquared(lastLocation.toVector()));
            if (entityHitFirst && entityHit.getHitEntity() instanceof LivingEntity target) {
                target.damage(getDiskHitDamage(target), projectile);
                applyVanillaDiskHitEffects(owner, target);
                lastHitTicks.put(target.getUniqueId(), ticks);
                Vector normal = entityHit.getHitPosition().clone()
                        .subtract(target.getLocation().toVector().add(new Vector(0.0, target.getHeight() / 2.0, 0.0)));
                if (normal.lengthSquared() == 0.0) {
                    normal = direction.clone().multiply(-1.0);
                } else {
                    normal.normalize();
                }
                bounceAt(entityHit.getHitPosition(), normal, movement, 0.6);
                owner.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.8f, 1.2f);
                return true;
            }
            if (blockHit != null) {
                Vector normal = blockHit.getHitBlockFace() == null
                        ? direction.clone().multiply(-1.0)
                        : blockHit.getHitBlockFace().getDirection();
                bounceAt(blockHit.getHitPosition(), normal, movement, 0.08);
                return true;
            }
            return false;
        }

        private void bounceAt(Vector hitPosition, Vector normal, Vector movement, double offset) {
            outboundDirection = movement.clone()
                    .subtract(normal.clone().multiply(2.0 * movement.dot(normal)))
                    .normalize();
            lastLocation = hitPosition.clone().add(normal.clone().multiply(offset)).toLocation(projectile.getWorld());
            projectile.teleport(lastLocation);
            bounces++;
            if (bounces >= BOOMERANG_MAX_BOUNCES) {
                returning = true;
            }
        }

        private double getDiskHitDamage(LivingEntity target) {
            double bonus = 0.0;
            int sharpness = disk.getEnchantmentLevel(Enchantment.SHARPNESS);
            if (sharpness > 0) {
                bonus += 0.5 * sharpness + 0.5;
            }
            if (UNDEAD_TYPES.contains(target.getType())) {
                bonus += 2.5 * disk.getEnchantmentLevel(Enchantment.SMITE);
            }
            if (ARTHROPOD_TYPES.contains(target.getType())) {
                bonus += 2.5 * disk.getEnchantmentLevel(Enchantment.BANE_OF_ARTHROPODS);
            }
            return damage + bonus;
        }

        private void applyVanillaDiskHitEffects(Player owner, LivingEntity target) {
            int fireAspect = disk.getEnchantmentLevel(Enchantment.FIRE_ASPECT);
            if (fireAspect > 0) {
                target.setFireTicks(Math.max(target.getFireTicks(), fireAspect * 80));
            }

            int knockback = disk.getEnchantmentLevel(Enchantment.KNOCKBACK);
            if (knockback > 0) {
                Vector push = target.getLocation().toVector().subtract(owner.getLocation().toVector());
                if (push.lengthSquared() > 0.0) {
                    target.setVelocity(push.normalize().multiply(0.4 * knockback).setY(0.15));
                }
            }
        }

        private void returnToOwner(Player owner) {
            if (returnItem) {
                ItemStack originalSlotItem = owner.getInventory().getItem(originalSlot);
                if (originalSlotItem == null || originalSlotItem.getType() == Material.AIR) {
                    owner.getInventory().setItem(originalSlot, disk.clone());
                } else {
                    Map<Integer, ItemStack> leftovers = owner.getInventory().addItem(disk.clone());
                    if (!leftovers.isEmpty()) {
                        projectile.teleport(owner.getEyeLocation());
                        lastLocation = projectile.getLocation();
                        returning = true;
                        if (!warnedInventoryFull) {
                            owner.sendMessage(ChatColor.YELLOW + "Your disk is back, but your inventory is full. "
                                    + "Make space to retrieve it.");
                            warnedInventoryFull = true;
                        }
                        return;
                    }
                }
            }
            owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.4f);
            finish();
        }

        private void addToDeathDrops(PlayerDeathEvent event) {
            if (returnItem) {
                event.getDrops().add(disk.clone());
            }
            finish();
        }

        private void dropAtLastLocation() {
            if (returnItem && lastLocation.getWorld() != null) {
                lastLocation.getWorld().dropItemNaturally(lastLocation, disk.clone());
            }
            finish();
        }

        private void finish() {
            activeBoomerangs.remove(ownerId, this);
            if (projectile.isValid()) {
                projectile.remove();
            }
            cancel();
        }
    }

    // 3. Elixir of Immortality Consumption
    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        CustomGearType type = gearManager.identify(event.getItem());
        if (type == CustomGearType.VITALITY_ELIXIR) {
            Player player = event.getPlayer();
            double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            player.setHealth(maxHp);
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 3));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 1));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        }
    }

    // 4. Skyhook Grapple Rod Mechanics
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        CustomGearType main = gearManager.identify(player.getInventory().getItemInMainHand());
        CustomGearType off = gearManager.identify(player.getInventory().getItemInOffHand());
        if (main != CustomGearType.SKYHOOK_ROD && off != CustomGearType.SKYHOOK_ROD) return;

        if (event.getState() == PlayerFishEvent.State.IN_GROUND || event.getState() == PlayerFishEvent.State.REEL_IN) {
            Vector pull = event.getHook().getLocation().toVector().subtract(player.getLocation().toVector());
            if (pull.length() < 1.5) return;
            Vector vel = pull.normalize().multiply(Math.min(2.4, pull.length() * 0.42)).setY(Math.max(0.55, pull.getY() * 0.35 + 0.4));
            player.setVelocity(vel);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.4f);
            grappleFallImmunity.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> grappleFallImmunity.remove(player.getUniqueId()), 100L);
        }
    }

    // 5. Zephyr Double-Jump & Fall Immunity
    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (gearManager.hasZephyrBoots(player)) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            Vector boost = player.getLocation().getDirection().multiply(0.7).setY(0.85);
            player.setVelocity(boost);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 1.2f);
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 12, 0.3, 0.1, 0.3, 0.05);
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        if (grappleFallImmunity.remove(player.getUniqueId()) || "zephyr".equals(gearManager.getEquippedFullSet(player))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)
                || !(arrow.getShooter() instanceof Player shooter)) {
            return;
        }

        CustomGearType mainHand = gearManager.identify(shooter.getInventory().getItemInMainHand());
        CustomGearType offHand = gearManager.identify(shooter.getInventory().getItemInOffHand());
        if (mainHand == CustomGearType.SKYBOUND_BOW || offHand == CustomGearType.SKYBOUND_BOW) {
            arrow.setMetadata(SKYBOUND_ARROW_METADATA, new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)
                || !isSkyboundArrow(arrow)
                || !(event.getHitEntity() instanceof LivingEntity target)) {
            return;
        }

        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 0));
    }

    private boolean isSkyboundArrow(Arrow arrow) {
        for (MetadataValue value : arrow.getMetadata(SKYBOUND_ARROW_METADATA)) {
            if (value.getOwningPlugin() == plugin && value.asBoolean()) return true;
        }
        return false;
    }

    // 6. Combat Weapon & Armor Set Passives
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombatDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof LivingEntity target)) return;

        String activeSet = gearManager.getEquippedFullSet(attacker);
        if ("pyro".equals(activeSet)) {
            target.setFireTicks(80);
        } else if ("shadow".equals(activeSet) && attacker.isSneaking()) {
            event.setDamage(event.getDamage() * 1.5);
            attacker.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.2f, 0.8f);
        }

        CustomGearType weapon = gearManager.identify(attacker.getInventory().getItemInMainHand());
        if (weapon == CustomGearType.EXECUTIONER_GREATAXE) {
            double maxHp = target.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            if (target.getHealth() <= maxHp * 0.5) {
                event.setDamage(event.getDamage() * 1.35);
            }
        } else if (weapon == CustomGearType.SOULREAVER_SCYTHE) {
            double maxHp = attacker.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            attacker.setHealth(Math.min(maxHp, attacker.getHealth() + 3.0));
            for (Entity nearby : target.getNearbyEntities(2.5, 1.5, 2.5)) {
                if (nearby instanceof LivingEntity living && nearby != attacker && nearby != target) {
                    living.damage(4.0, attacker);
                }
            }
        }
    }

    // 7. Earthshaker 3x3 Pickaxe
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.isSneaking()) return;
        if (hammerGuard.contains(player.getUniqueId())) return;

        if (gearManager.identify(player.getInventory().getItemInMainHand()) == CustomGearType.EARTHSHAKER_PICKAXE) {
            hammerGuard.add(player.getUniqueId());
            try {
                Block center = event.getBlock();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            if (!isOnTunnelFace(dx, dy, dz, player.getLocation().getDirection())) continue;
                            Block rel = center.getRelative(dx, dy, dz);
                            if (rel.getType().isSolid() && rel.getType() != Material.BEDROCK) {
                                player.breakBlock(rel);
                            }
                        }
                    }
                }
            } finally {
                hammerGuard.remove(player.getUniqueId());
            }
        }
    }

    private boolean isOnTunnelFace(int dx, int dy, int dz, Vector direction) {
        double absX = Math.abs(direction.getX());
        double absY = Math.abs(direction.getY());
        double absZ = Math.abs(direction.getZ());
        if (absY > absX && absY > absZ) return dy == 0;
        if (absX > absZ) return dx == 0;
        return dz == 0;
    }

    private boolean isOnCooldown(Player player, long millis) {
        long now = System.currentTimeMillis();
        long readyAt = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now < readyAt) {
            player.sendMessage(ChatColor.RED + "Ability recharging (" + String.format("%.1f", (readyAt - now) / 1000.0) + "s)...");
            return true;
        }
        cooldowns.put(player.getUniqueId(), now + millis);
        return false;
    }
}
