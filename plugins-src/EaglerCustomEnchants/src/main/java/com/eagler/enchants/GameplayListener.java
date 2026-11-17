package com.eagler.enchants;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.NamespacedKey;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class GameplayListener implements Listener {

    private static final NamespacedKey CUSTOM_GEAR_ID_KEY =
            new NamespacedKey("eaglercustomgear", "custom_gear_id");

    private final JavaPlugin plugin;
    private final EnchantmentManager manager;
    private final Set<UUID> grappleFallImmune = new HashSet<>();
    private final Set<UUID> miningGuard = new HashSet<>();
    private final Map<Block, BlockData> temporaryLavaChanges = new HashMap<>();
    private final NamespacedKey soulboundReturnKey;
    private final NamespacedKey secondWindCooldownKey;

    private static final Set<Material> ORES = EnumSet.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
            Material.ANCIENT_DEBRIS
    );

    private static final Set<Material> LOGS = EnumSet.of(
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG, Material.CHERRY_LOG, Material.PALE_OAK_LOG,
            Material.CRIMSON_STEM, Material.WARPED_STEM
    );

    private static final Set<Material> SOFT_BLOCKS = EnumSet.of(
            Material.DIRT, Material.GRASS_BLOCK, Material.SAND, Material.RED_SAND,
            Material.GRAVEL, Material.CLAY, Material.SOUL_SAND, Material.SOUL_SOIL,
            Material.MYCELIUM, Material.PODZOL, Material.SNOW_BLOCK, Material.MUD
    );

    public GameplayListener(JavaPlugin plugin, EnchantmentManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.soulboundReturnKey = new NamespacedKey(plugin, "soulbound_return");
        this.secondWindCooldownKey = new NamespacedKey(plugin, "second_wind_ready_at");
    }

    // 1. GRAPPLE (Fishing Rod)
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack rod = event.getHand() == EquipmentSlot.OFF_HAND
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            int angler = manager.getLevel(rod, EnchantmentType.ANGLER);
            if (angler > 0 && event.getCaught() instanceof Item caught
                    && isFish(caught.getItemStack().getType())
                    && ThreadLocalRandom.current().nextDouble() < 0.05 * angler) {
                ItemStack extraFish = caught.getItemStack().clone();
                extraFish.setAmount(1);
                giveOrDrop(player, extraFish);
            }

            int mariner = manager.getLevel(rod, EnchantmentType.MARINER);
            if (mariner > 0) {
                event.setExpToDrop(event.getExpToDrop() + mariner);
            }
        }

        int level = manager.getLevel(rod, EnchantmentType.GRAPPLE);
        if (level <= 0) {
            rod = player.getInventory().getItemInOffHand();
            level = manager.getLevel(rod, EnchantmentType.GRAPPLE);
        }
        if (level <= 0) return;

        PlayerFishEvent.State state = event.getState();
        if (state == PlayerFishEvent.State.IN_GROUND || state == PlayerFishEvent.State.REEL_IN || state == PlayerFishEvent.State.CAUGHT_ENTITY) {
            Location hookLoc = event.getHook().getLocation();
            Location playerLoc = player.getLocation();

            Vector direction = hookLoc.toVector().subtract(playerLoc.toVector());
            double distance = direction.length();
            if (distance < 1.5) return;

            double multiplier = 0.28 + (level * 0.12);
            Vector velocity = direction.normalize().multiply(Math.min(2.6, distance * multiplier));
            velocity.setY(Math.max(0.45, velocity.getY() + 0.35));

            player.setVelocity(velocity);
            player.getWorld().playSound(playerLoc, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.3f);

            grappleFallImmune.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> grappleFallImmune.remove(player.getUniqueId()), 100L);
        }
    }

    private boolean isFish(Material material) {
        return material == Material.COD || material == Material.SALMON
                || material == Material.TROPICAL_FISH || material == Material.PUFFERFISH;
    }

    private void giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (grappleFallImmune.remove(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory()) return;

        Player player = event.getEntity();
        Iterator<ItemStack> drops = event.getDrops().iterator();
        while (drops.hasNext()) {
            ItemStack item = drops.next();
            if (manager.getLevel(item, EnchantmentType.SOULBOUND) <= 0
                    || item.containsEnchantment(Enchantment.VANISHING_CURSE)) {
                continue;
            }

            YamlConfiguration serialized = new YamlConfiguration();
            serialized.set("item", item.clone());
            player.getPersistentDataContainer().set(
                    soulboundReturnKey, PersistentDataType.STRING, serialized.saveToString());
            drops.remove();
            break;
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String serialized = player.getPersistentDataContainer().get(
                soulboundReturnKey, PersistentDataType.STRING);
        if (serialized == null) return;

        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.loadFromString(serialized);
        } catch (InvalidConfigurationException exception) {
            plugin.getLogger().log(Level.SEVERE,
                    "Could not restore a Soulbound item for " + player.getName()
                            + "; its saved data was retained for another attempt.",
                    exception);
            return;
        }

        ItemStack item = configuration.getItemStack("item");
        if (item == null || item.getType() == Material.AIR) {
            plugin.getLogger().severe("Could not restore a Soulbound item for " + player.getName()
                    + ": saved data contained no item.");
            return;
        }

        player.getPersistentDataContainer().remove(soulboundReturnKey);
        Bukkit.getScheduler().runTask(plugin, () -> giveOrDrop(player, item));
    }

    // 2. BLOCK BREAKING: Veinminer, Timber, Excavate, Replenish, Experience
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (miningGuard.contains(player.getUniqueId())) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        Block block = event.getBlock();
        Material type = block.getType();

        // Experience
        int expLvl = manager.getLevel(tool, EnchantmentType.EXPERIENCE);
        if (expLvl > 0 && event.getExpToDrop() > 0) {
            event.setExpToDrop(event.getExpToDrop() * (1 + expLvl));
        }

        // Replenish (Hoe)
        if (manager.getLevel(tool, EnchantmentType.REPLENISH) > 0 && block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                Material cropType = type;
                Material seedType = getSeedForCrop(cropType);
                if (seedType != null && player.getInventory().contains(seedType)) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (block.getType() == Material.AIR && player.getInventory().contains(seedType)) {
                            player.getInventory().removeItem(new ItemStack(seedType, 1));
                            block.setType(cropType);
                        }
                    }, 2L);
                }
            }
        }

        // Veinminer (Pickaxe)
        int veinLvl = manager.getLevel(tool, EnchantmentType.VEINMINER);
        if (veinLvl > 0 && ORES.contains(type) && !player.isSneaking()) {
            int maxBlocks = 8 * veinLvl;
            breakConnected(player, block, type, maxBlocks, true);
            return;
        }

        // Timber (Axe)
        int timberLvl = manager.getLevel(tool, EnchantmentType.TIMBER);
        if (timberLvl > 0 && LOGS.contains(type) && !player.isSneaking()) {
            int maxLogs = 24 * timberLvl;
            breakConnected(player, block, type, maxLogs, false);
            return;
        }

        // Excavate (Shovel 3x3)
        int excLvl = manager.getLevel(tool, EnchantmentType.EXCAVATE);
        if (excLvl > 0 && SOFT_BLOCKS.contains(type) && !player.isSneaking()) {
            miningGuard.add(player.getUniqueId());
            try {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        Block rel = block.getRelative(dx, 0, dz);
                        if (SOFT_BLOCKS.contains(rel.getType())) {
                            player.breakBlock(rel);
                        }
                    }
                }
            } finally {
                miningGuard.remove(player.getUniqueId());
            }
        }
    }

    // 3. BLOCK DROPS: Auto-Smelt & Telekinesis
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        boolean autoSmelt = manager.getLevel(tool, EnchantmentType.AUTO_SMELT) > 0;
        boolean telekinesis = manager.getLevel(tool, EnchantmentType.TELEKINESIS) > 0;
        ItemStack bonusDrop = getBonusBlockDrop(event, tool);

        if (!autoSmelt && !telekinesis && bonusDrop == null) return;

        Iterator<Item> it = event.getItems().iterator();
        while (it.hasNext()) {
            Item dropEntity = it.next();
            ItemStack stack = dropEntity.getItemStack();

            if (autoSmelt) {
                Material smelted = getSmeltedMaterial(stack.getType());
                if (smelted != null) {
                    stack.setType(smelted);
                    dropEntity.setItemStack(stack);
                    player.getWorld().spawnParticle(Particle.FLAME, dropEntity.getLocation(), 4, 0.1, 0.1, 0.1, 0.01);
                }
            }

            if (telekinesis) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
                if (leftover.isEmpty()) {
                    it.remove();
                } else {
                    dropEntity.setItemStack(leftover.values().iterator().next());
                }
            }
        }

        if (bonusDrop != null) {
            if (autoSmelt) {
                Material smelted = getSmeltedMaterial(bonusDrop.getType());
                if (smelted != null) bonusDrop.setType(smelted);
            }

            if (telekinesis) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(bonusDrop);
                if (leftover.isEmpty()) return;
                bonusDrop = leftover.values().iterator().next();
            }

            Item extra = event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation().add(0.5, 0.5, 0.5), bonusDrop);
            event.getItems().add(extra);
        }
    }

    private ItemStack getBonusBlockDrop(BlockDropItemEvent event, ItemStack tool) {
        Material blockType = event.getBlockState().getType();
        int prospector = manager.getLevel(tool, EnchantmentType.PROSPECTOR);
        if (prospector > 0 && ORES.contains(blockType)
                && ThreadLocalRandom.current().nextDouble() < 0.05 * prospector) {
            return firstDrop(event);
        }

        int archaeologist = manager.getLevel(tool, EnchantmentType.ARCHAEOLOGIST);
        if (archaeologist > 0
                && (blockType == Material.SAND || blockType == Material.RED_SAND
                || blockType == Material.GRAVEL || blockType == Material.CLAY)
                && ThreadLocalRandom.current().nextDouble() < 0.05 * archaeologist) {
            return firstDrop(event);
        }

        int horticulturist = manager.getLevel(tool, EnchantmentType.HORTICULTURIST);
        if (horticulturist > 0 && event.getBlockState().getBlockData() instanceof Ageable crop
                && crop.getAge() == crop.getMaximumAge()
                && ThreadLocalRandom.current().nextDouble() < 0.05 * horticulturist) {
            Material produce = getProduce(blockType);
            return produce == null ? null : new ItemStack(produce);
        }

        return null;
    }

    private ItemStack firstDrop(BlockDropItemEvent event) {
        for (Item drop : event.getItems()) {
            ItemStack item = drop.getItemStack();
            if (item != null && item.getType() != Material.AIR) {
                ItemStack extra = item.clone();
                extra.setAmount(1);
                return extra;
            }
        }
        return null;
    }

    private Material getProduce(Material crop) {
        return switch (crop) {
            case WHEAT -> Material.WHEAT;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT;
            case NETHER_WART -> Material.NETHER_WART;
            case SWEET_BERRY_BUSH -> Material.SWEET_BERRIES;
            default -> null;
        };
    }

    // 4. COMBAT: Lifesteal, Thunderstrike, Frostbite, Poison Aspect, Molten
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        CombatWeapon combatWeapon = getCombatWeapon(event.getDamager());
        if (combatWeapon != null) {
            Player attacker = combatWeapon.attacker();
            ItemStack weapon = combatWeapon.item();

            int hunter = manager.getLevel(weapon, EnchantmentType.HUNTER);
            if (hunter > 0 && target instanceof Monster) {
                event.setDamage(event.getDamage() * (1.0 + 0.03 * hunter));
            }

            int duelist = manager.getLevel(weapon, EnchantmentType.DUELIST);
            if (duelist > 0 && target instanceof Player) {
                event.setDamage(event.getDamage() * (1.0 + 0.02 * duelist));
            }

            int finisher = manager.getLevel(weapon, EnchantmentType.FINISHER);
            if (finisher > 0 && target.getHealth() <= target.getMaxHealth() * 0.25) {
                event.setDamage(event.getDamage() + 0.5 * finisher);
            }

            int backstab = manager.getLevel(weapon, EnchantmentType.BACKSTAB);
            if (backstab > 0 && isBehind(attacker, target)) {
                event.setDamage(event.getDamage() * (1.0 + 0.05 * backstab));
            }

            int tidecaller = manager.getLevel(weapon, EnchantmentType.TIDECALLER);
            if (tidecaller > 0 && target instanceof WaterMob) {
                event.setDamage(event.getDamage() * (1.0 + 0.05 * tidecaller));
            }

            int crippling = manager.getLevel(weapon, EnchantmentType.CRIPPLING);
            if (crippling > 0 && ThreadLocalRandom.current().nextDouble() < 0.05 * crippling) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 0));
            }

            int stagger = manager.getLevel(weapon, EnchantmentType.STAGGER);
            if (stagger > 0 && ThreadLocalRandom.current().nextDouble() < 0.05 * stagger) {
                Vector push = target.getLocation().toVector().subtract(attacker.getLocation().toVector());
                if (push.lengthSquared() > 0.0) {
                    target.setVelocity(push.normalize().multiply(0.35).setY(0.12));
                }
            }

            // Lifesteal
            int lifesteal = manager.getLevel(weapon, EnchantmentType.LIFESTEAL);
            if (lifesteal > 0) {
                double heal = event.getFinalDamage() * (0.08 * lifesteal);
                double maxHp = attacker.getMaxHealth();
                attacker.setHealth(Math.min(maxHp, attacker.getHealth() + heal));
            }

            // Thunderstrike
            int thunder = manager.getLevel(weapon, EnchantmentType.THUNDERSTRIKE);
            if (thunder > 0 && ThreadLocalRandom.current().nextDouble() < (0.10 * thunder)) {
                target.getWorld().strikeLightningEffect(target.getLocation());
                target.damage(2.0 * thunder);
            }

            // Frostbite
            int frost = manager.getLevel(weapon, EnchantmentType.FROSTBITE);
            if (frost > 0) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60 * frost, frost));
                target.setFreezeTicks(100 * frost);
            }

            // Poison Aspect
            int poison = manager.getLevel(weapon, EnchantmentType.POISON_ASPECT);
            if (poison > 0) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60 * poison, poison - 1));
            }
        }

        if (event.getDamager() instanceof AbstractArrow arrow
                && arrow.getShooter() instanceof Player rangedAttacker) {
            applyRangedDamageEnchantments(event, rangedAttacker, target,
                    projectileLevel(arrow, "ce_longshot"),
                    projectileLevel(arrow, "ce_pinning"),
                    arrow instanceof Trident ? projectileLevel(arrow, "ce_tidecaller") : 0);
        } else if (event.getDamager() instanceof Snowball snowball
                && snowball.getShooter() instanceof Player rangedAttacker
                && isBoomerangDisk(snowball.getItem())) {
            ItemStack disk = snowball.getItem();
            applyRangedDamageEnchantments(event, rangedAttacker, target,
                    manager.getLevel(disk, EnchantmentType.LONGSHOT),
                    manager.getLevel(disk, EnchantmentType.PINNING),
                    manager.getLevel(disk, EnchantmentType.TIDECALLER));
        }
    }

    private void applyRangedDamageEnchantments(
            EntityDamageByEntityEvent event, Player attacker, LivingEntity target,
            int longshot, int pinning, int tidecaller
    ) {
        if (longshot > 0) {
            double distance = attacker.getLocation().distance(target.getLocation());
            double bonus = Math.min(0.05 * longshot, Math.max(0.0, distance - 20.0) * 0.0025 * longshot);
            event.setDamage(event.getDamage() * (1.0 + bonus));
        }

        if (pinning > 0 && ThreadLocalRandom.current().nextDouble() < 0.05 * pinning) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 0));
        }

        if (tidecaller > 0 && target instanceof WaterMob) {
            event.setDamage(event.getDamage() * (1.0 + 0.05 * tidecaller));
        }

        // Defensive Chestplate: Molten
        if (event.getEntity() instanceof Player defender
                && event.getDamager() instanceof LivingEntity meleeAttacker) {
            int molten = manager.getLevel(defender.getInventory().getChestplate(), EnchantmentType.MOLTEN);
            if (molten > 0 && ThreadLocalRandom.current().nextDouble() < (0.20 * molten)) {
                meleeAttacker.setFireTicks(60 * molten);
            }
        }
    }

    private boolean isBehind(Player attacker, LivingEntity target) {
        Vector facing = target.getLocation().getDirection().setY(0);
        Vector toAttacker = attacker.getLocation().toVector().subtract(target.getLocation().toVector()).setY(0);
        if (facing.lengthSquared() == 0.0 || toAttacker.lengthSquared() == 0.0) return false;
        return facing.normalize().dot(toAttacker.normalize()) < -0.5;
    }

    private int projectileLevel(AbstractArrow arrow, String key) {
        for (org.bukkit.metadata.MetadataValue value : arrow.getMetadata(key)) {
            if (value.getOwningPlugin() == plugin) return value.asInt();
        }
        return 0;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIncomingDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double reduction = 0.02 * manager.getLevel(
                player.getInventory().getChestplate(), EnchantmentType.BULWARK);
        EntityDamageEvent.DamageCause cause = event.getCause();
        switch (cause) {
            case FIRE, FIRE_TICK, LAVA, HOT_FLOOR ->
                    reduction += 0.08 * strongestArmorLevel(player, EnchantmentType.FIREGUARD);
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION ->
                    reduction += 0.08 * strongestArmorLevel(player, EnchantmentType.BLASTGUARD);
            case PROJECTILE ->
                    reduction += 0.08 * strongestArmorLevel(player, EnchantmentType.PROJECTILE_WARD);
            case FREEZE ->
                    reduction += 0.10 * strongestArmorLevel(player, EnchantmentType.FROSTGUARD);
            case FALL ->
                    reduction += 0.08 * manager.getLevel(
                            player.getInventory().getBoots(), EnchantmentType.FEATHERFALL);
            case VOID ->
                    reduction += 0.10 * strongestArmorLevel(player, EnchantmentType.VOIDPROOF);
            default -> {
            }
        }

        if (player.isBlocking()) {
            int aegis = Math.max(
                    manager.getLevel(player.getInventory().getItemInMainHand(), EnchantmentType.AEGIS),
                    manager.getLevel(player.getInventory().getItemInOffHand(), EnchantmentType.AEGIS));
            reduction += 0.08 * aegis;
        }

        if (reduction > 0.0) {
            event.setDamage(event.getDamage() * (1.0 - Math.min(0.35, reduction)));
        }

        int secondWind = manager.getLevel(
                player.getInventory().getChestplate(), EnchantmentType.SECOND_WIND);
        if (secondWind <= 0
                || event.getFinalDamage() < player.getHealth() + player.getAbsorptionAmount()) {
            return;
        }

        long now = System.currentTimeMillis();
        long readyAt = player.getPersistentDataContainer().getOrDefault(
                secondWindCooldownKey, PersistentDataType.LONG, 0L);
        if (now < readyAt) return;

        player.getPersistentDataContainer().set(
                secondWindCooldownKey, PersistentDataType.LONG, now + 120_000L);
        event.setCancelled(true);
        player.setHealth(1.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0));
    }

    private int strongestArmorLevel(Player player, EnchantmentType type) {
        int level = 0;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            level = Math.max(level, manager.getLevel(armor, type));
        }
        return level;
    }

    // 5. MOB DEATH: Telekinesis & Experience
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        CombatWeapon combatWeapon = getDeathCombatWeapon(event.getEntity());
        if (combatWeapon == null) return;
        Player killer = combatWeapon.attacker();
        ItemStack weapon = combatWeapon.item();

        int expLvl = manager.getLevel(weapon, EnchantmentType.EXPERIENCE);
        if (expLvl > 0) {
            event.setDroppedExp(event.getDroppedExp() * (1 + expLvl));
        }

        int scavenger = manager.getLevel(weapon, EnchantmentType.SCAVENGER);
        if (scavenger > 0 && event.getEntity() instanceof Monster
                && event.getEntity().getCustomName() == null
                && !event.getDrops().isEmpty()
                && ThreadLocalRandom.current().nextDouble() < 0.05 * scavenger) {
            ItemStack extra = event.getDrops().get(
                    ThreadLocalRandom.current().nextInt(event.getDrops().size())).clone();
            extra.setAmount(1);
            event.getDrops().add(extra);
        }

        if (manager.getLevel(weapon, EnchantmentType.TELEKINESIS) > 0) {
            ListIterator<ItemStack> it = event.getDrops().listIterator();
            while (it.hasNext()) {
                ItemStack drop = it.next();
                Map<Integer, ItemStack> leftover = killer.getInventory().addItem(drop.clone());
                if (leftover.isEmpty()) {
                    it.remove();
                } else {
                    it.set(leftover.values().iterator().next());
                }
            }
        }

        if (isBoomerangDisk(weapon)) {
            int looting = weapon.getEnchantmentLevel(Enchantment.LOOTING);
            int extraDrops = ThreadLocalRandom.current().nextInt(looting + 1);
            for (int i = 0; i < extraDrops && !event.getDrops().isEmpty(); i++) {
                ItemStack extra = event.getDrops().get(
                        ThreadLocalRandom.current().nextInt(event.getDrops().size())).clone();
                extra.setAmount(1);
                event.getDrops().add(extra);
            }
        }
    }

    private CombatWeapon getDeathCombatWeapon(LivingEntity victim) {
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent byEntity) {
            CombatWeapon sourceWeapon = getCombatWeapon(byEntity.getDamager());
            if (sourceWeapon != null && isBoomerangDisk(sourceWeapon.item())) {
                return sourceWeapon;
            }
        }

        Player killer = victim.getKiller();
        return killer == null ? null : new CombatWeapon(killer, killer.getInventory().getItemInMainHand());
    }

    private CombatWeapon getCombatWeapon(Entity damager) {
        if (damager instanceof Player player) {
            return new CombatWeapon(player, player.getInventory().getItemInMainHand());
        }
        if (damager instanceof Snowball snowball
                && snowball.getShooter() instanceof Player player
                && isBoomerangDisk(snowball.getItem())) {
            return new CombatWeapon(player, snowball.getItem());
        }
        return null;
    }

    private boolean isBoomerangDisk(ItemStack item) {
        return item != null && item.hasItemMeta()
                && "boomerang_disk".equals(item.getItemMeta().getPersistentDataContainer()
                .get(CUSTOM_GEAR_ID_KEY, PersistentDataType.STRING));
    }

    private record CombatWeapon(Player attacker, ItemStack item) {}

    // 6. BOW / CROSSBOW: Explosive Arrows
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;

        ItemStack bow = player.getInventory().getItemInMainHand();
        int lvl = manager.getLevel(bow, EnchantmentType.EXPLOSIVE_ARROWS);
        if (lvl > 0) {
            arrow.setMetadata("ce_explosive", new FixedMetadataValue(plugin, lvl));
        }

        int longshot = manager.getLevel(bow, EnchantmentType.LONGSHOT);
        if (longshot > 0) {
            arrow.setMetadata("ce_longshot", new FixedMetadataValue(plugin, longshot));
        }

        int pinning = manager.getLevel(bow, EnchantmentType.PINNING);
        if (pinning > 0) {
            arrow.setMetadata("ce_pinning", new FixedMetadataValue(plugin, pinning));
        }

        int retrieval = manager.getLevel(bow, EnchantmentType.RETRIEVAL);
        if (retrieval > 0 && !bow.containsEnchantment(Enchantment.INFINITY)) {
            arrow.setMetadata("ce_retrieval", new FixedMetadataValue(plugin, retrieval));
        }

        int tidecaller = manager.getLevel(bow, EnchantmentType.TIDECALLER);
        if (tidecaller > 0 && arrow instanceof Trident) {
            arrow.setMetadata("ce_tidecaller", new FixedMetadataValue(plugin, tidecaller));
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof AbstractArrow arrow && arrow.hasMetadata("ce_explosive")) {
            int lvl = projectileLevel(arrow, "ce_explosive");
            Location loc = arrow.getLocation();
            loc.getWorld().createExplosion(loc, 1.5f + (lvl * 0.8f), false, false);
            arrow.remove();
            return;
        }

        if (event.getEntity() instanceof AbstractArrow arrow
                && projectileLevel(arrow, "ce_retrieval") > 0
                && arrow.getPickupStatus() == AbstractArrow.PickupStatus.ALLOWED
                && arrow.getShooter() instanceof Player player
                && ThreadLocalRandom.current().nextDouble()
                < 0.10 * projectileLevel(arrow, "ce_retrieval")) {
            ItemStack recovered = arrow.getItem();
            if (recovered != null && recovered.getType() != Material.AIR) {
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                ItemStack copy = recovered.clone();
                copy.setAmount(1);
                giveOrDrop(player, copy);
            }
        }
    }

    // 7. CHESTPLATE: Hardened (Durability Save)
    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        int hardened = manager.getLevel(event.getItem(), EnchantmentType.HARDENED);
        int recycler = manager.getLevel(event.getItem(), EnchantmentType.RECYCLER);
        double saveChance = Math.min(0.5, 0.15 * hardened + 0.05 * recycler);
        if (saveChance > 0 && ThreadLocalRandom.current().nextDouble() < saveChance) {
            event.setCancelled(true);
        }
    }

    // 8. BOOTS: Lava Walker
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        int lavaWalker = manager.getLevel(player.getInventory().getBoots(), EnchantmentType.LAVA_WALKER);
        if (lavaWalker <= 0) return;

        int radius = 1 + lavaWalker;
        Block center = event.getTo().getBlock().getRelative(BlockFace.DOWN);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block target = center.getRelative(x, 0, z);
                if (target.getType() == Material.LAVA && !temporaryLavaChanges.containsKey(target)) {
                    BlockData originalData = target.getBlockData().clone();
                    temporaryLavaChanges.put(target, originalData);
                    target.setType(Material.MAGMA_BLOCK);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        BlockData previousData = temporaryLavaChanges.remove(target);
                        if (previousData != null && target.getType() == Material.MAGMA_BLOCK) {
                            target.setBlockData(previousData);
                        }
                    }, 80L);
                }
            }
        }
    }

    public void applyAdditionalPassiveEffects(Player player) {
        ItemStack boots = player.getInventory().getBoots();
        Material eyeBlock = player.getEyeLocation().getBlock().getType();
        boolean submerged = eyeBlock == Material.WATER || eyeBlock == Material.BUBBLE_COLUMN;

        if (manager.getLevel(boots, EnchantmentType.SWIFT_SWIM) > 0
                && (submerged || player.isSwimming())) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.DOLPHINS_GRACE, 60, 0, true, false, false));
        }

        if (submerged && manager.getLevel(
                player.getInventory().getHelmet(), EnchantmentType.GILLS) > 0) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.WATER_BREATHING, 60, 0, true, false, false));
        }

        int deepMiner = manager.getLevel(
                player.getInventory().getItemInMainHand(), EnchantmentType.DEEP_MINER);
        if (deepMiner > 0 && player.getWorld().getEnvironment() == World.Environment.NORMAL
                && player.getLocation().getBlockY() <= 48
                && player.getWorld().getHighestBlockAt(player.getLocation()).getY()
                > player.getLocation().getBlockY() + 1) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.HASTE, 60, deepMiner - 1, true, false, false));
        }
    }

    public void restoreTemporaryLava() {
        for (Map.Entry<Block, BlockData> entry : temporaryLavaChanges.entrySet()) {
            if (entry.getKey().getType() == Material.MAGMA_BLOCK) {
                entry.getKey().setBlockData(entry.getValue());
            }
        }
        temporaryLavaChanges.clear();
    }

    private void breakConnected(Player player, Block origin, Material targetMat, int limit, boolean includeDiagonals) {
        miningGuard.add(player.getUniqueId());
        try {
            Queue<Block> queue = new ArrayDeque<>();
            Set<Block> visited = new HashSet<>();
            queue.add(origin);
            visited.add(origin);

            int broken = 0;
            while (!queue.isEmpty() && broken < limit) {
                Block current = queue.poll();
                if (!current.equals(origin)) {
                    player.breakBlock(current);
                    broken++;
                }

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (!includeDiagonals && (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1)) continue;
                            Block neighbor = current.getRelative(dx, dy, dz);
                            if (!visited.contains(neighbor) && neighbor.getType() == targetMat) {
                                visited.add(neighbor);
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        } finally {
            miningGuard.remove(player.getUniqueId());
        }
    }

    private Material getSmeltedMaterial(Material raw) {
        return switch (raw) {
            case RAW_IRON, IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case RAW_GOLD, GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.GOLD_INGOT;
            case RAW_COPPER, COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            case SAND, RED_SAND -> Material.GLASS;
            case COBBLESTONE -> Material.STONE;
            case CLAY_BALL -> Material.BRICK;
            default -> null;
        };
    }

    private Material getSeedForCrop(Material crop) {
        return switch (crop) {
            case WHEAT -> Material.WHEAT_SEEDS;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT_SEEDS;
            case NETHER_WART -> Material.NETHER_WART;
            default -> null;
        };
    }
}
