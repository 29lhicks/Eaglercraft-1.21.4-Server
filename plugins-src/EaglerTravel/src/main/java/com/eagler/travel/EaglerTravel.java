package com.eagler.travel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Tag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public final class EaglerTravel extends JavaPlugin implements CommandExecutor, TabCompleter {
    private static final String PREFIX = ChatColor.AQUA + "[Travel] " + ChatColor.RESET;
    private static final String DEFAULT_HOME = "home";
    private static final long DEFAULT_TPA_EXPIRY_SECONDS = 60;
    private static final int RTP_ATTEMPTS = 40;
    private final Map<UUID, TeleportRequest> pendingRequests = new HashMap<>();
    private final Map<UUID, Long> rtpCooldowns = new HashMap<>();
    private YamlConfiguration homes;
    private File homesFile;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        homesFile = new File(getDataFolder(), "homes.yml");
        homes = YamlConfiguration.loadConfiguration(homesFile);
        for (String name : List.of("rtp", "sethome", "home", "delhome", "homes", "tpa", "tpaccept", "tpdeny")) {
            PluginCommand command = getCommand(name);
            if (command == null) {
                throw new IllegalStateException("Missing command registration in plugin.yml: " + name);
            }
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
        getLogger().info("EaglerTravel enabled.");
    }

    @Override
    public void onDisable() {
        pendingRequests.clear();
        rtpCooldowns.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        if (name.equals("tpaccept") || name.equals("tpdeny")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Only players can use this command.");
                return true;
            }
            if (args.length != 0) return false;
            return name.equals("tpaccept") ? acceptRequest(player) : denyRequest(player);
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Only players can use this command.");
            return true;
        }
        return switch (name) {
            case "rtp" -> args.length == 0 ? randomTeleport(player) : false;
            case "sethome" -> args.length <= 1 ? setHome(player, args.length == 0 ? DEFAULT_HOME : args[0]) : false;
            case "home" -> args.length <= 1 ? goHome(player, args.length == 0 ? DEFAULT_HOME : args[0]) : false;
            case "delhome" -> args.length == 1 ? deleteHome(player, args[0]) : false;
            case "homes" -> args.length == 0 ? listHomes(player) : false;
            case "tpa" -> args.length == 1 ? requestTeleport(player, args[0]) : false;
            default -> false;
        };
    }

    private boolean setHome(Player player, String rawName) {
        String homeName = normalizeHomeName(rawName);
        if (homeName == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "Home names must be 1-16 letters, numbers, underscores, or hyphens.");
            return true;
        }
        String playerPath = "homes." + player.getUniqueId();
        ConfigurationSection section = homes.getConfigurationSection(playerPath);
        boolean alreadyExists = section != null && section.contains(homeName);
        int maxHomes = Math.max(1, getConfig().getInt("max-homes", 4));
        if (!alreadyExists && section != null && section.getKeys(false).size() >= maxHomes) {
            player.sendMessage(PREFIX + ChatColor.RED + "You have reached the limit of " + maxHomes + " homes.");
            return true;
        }

        Location location = player.getLocation();
        String path = playerPath + "." + homeName;
        homes.set(path + ".world", location.getWorld().getName());
        homes.set(path + ".world-uuid", location.getWorld().getUID().toString());
        homes.set(path + ".x", location.getX());
        homes.set(path + ".y", location.getY());
        homes.set(path + ".z", location.getZ());
        homes.set(path + ".yaw", location.getYaw());
        homes.set(path + ".pitch", location.getPitch());
        if (!saveHomes(player)) return true;
        player.sendMessage(PREFIX + ChatColor.GREEN + "Home '" + homeName + "' saved.");
        return true;
    }

    private boolean goHome(Player player, String rawName) {
        String homeName = normalizeHomeName(rawName);
        if (homeName == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "Invalid home name.");
            return true;
        }
        String path = "homes." + player.getUniqueId() + "." + homeName;
        if (!homes.contains(path)) {
            player.sendMessage(PREFIX + ChatColor.RED + "You don't have a home named '" + homeName + "'.");
            return true;
        }

        World world = getHomeWorld(path);
        if (world == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "That home's world is unavailable.");
            return true;
        }
        Location destination = new Location(world,
                homes.getDouble(path + ".x"),
                homes.getDouble(path + ".y"),
                homes.getDouble(path + ".z"),
                (float) homes.getDouble(path + ".yaw"),
                (float) homes.getDouble(path + ".pitch"));
        player.teleportAsync(destination).thenAccept(success -> Bukkit.getScheduler().runTask(this, () -> {
            if (player.isOnline()) {
                player.sendMessage(PREFIX + (success
                        ? ChatColor.GREEN + "Teleported to '" + homeName + "'."
                        : ChatColor.RED + "Teleport failed."));
            }
        }));
        return true;
    }

    private boolean deleteHome(Player player, String rawName) {
        String homeName = normalizeHomeName(rawName);
        if (homeName == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "Invalid home name.");
            return true;
        }
        String path = "homes." + player.getUniqueId() + "." + homeName;
        if (!homes.contains(path)) {
            player.sendMessage(PREFIX + ChatColor.RED + "You don't have a home named '" + homeName + "'.");
            return true;
        }
        homes.set(path, null);
        if (homes.getConfigurationSection("homes." + player.getUniqueId()) != null
                && homes.getConfigurationSection("homes." + player.getUniqueId()).getKeys(false).isEmpty()) {
            homes.set("homes." + player.getUniqueId(), null);
        }
        if (!saveHomes(player)) return true;
        player.sendMessage(PREFIX + ChatColor.GREEN + "Home '" + homeName + "' deleted.");
        return true;
    }

    private boolean listHomes(Player player) {
        ConfigurationSection section = homes.getConfigurationSection("homes." + player.getUniqueId());
        if (section == null || section.getKeys(false).isEmpty()) {
            player.sendMessage(PREFIX + ChatColor.YELLOW + "You have no saved homes. Use /sethome [name].");
            return true;
        }
        List<String> names = new ArrayList<>(section.getKeys(false));
        names.sort(String.CASE_INSENSITIVE_ORDER);
        player.sendMessage(PREFIX + ChatColor.GREEN + "Homes (" + names.size() + "): "
                + ChatColor.WHITE + String.join(", ", names));
        return true;
    }

    private boolean requestTeleport(Player requester, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            requester.sendMessage(PREFIX + ChatColor.RED + "That player is not online.");
            return true;
        }
        if (target.equals(requester)) {
            requester.sendMessage(PREFIX + ChatColor.RED + "You can't send a teleport request to yourself.");
            return true;
        }
        TeleportRequest existing = pendingRequests.get(target.getUniqueId());
        if (existing != null && existing.expiresAtNanos() > System.nanoTime()) {
            requester.sendMessage(PREFIX + ChatColor.YELLOW + target.getName() + " already has a pending request.");
            return true;
        }

        long expirySeconds = Math.max(10, getConfig().getLong("tpa.expiry-seconds", DEFAULT_TPA_EXPIRY_SECONDS));
        TeleportRequest request = new TeleportRequest(requester.getUniqueId(), target.getUniqueId(),
                System.nanoTime() + expirySeconds * 1_000_000_000L);
        pendingRequests.put(target.getUniqueId(), request);
        requester.sendMessage(PREFIX + ChatColor.GREEN + "Teleport request sent to " + target.getName() + ".");
        target.sendMessage(PREFIX + ChatColor.YELLOW + requester.getName()
                + " wants to teleport to you. Use /tpaccept or /tpdeny (" + expirySeconds + "s).");
        BukkitTask expiryTask = Bukkit.getScheduler().runTaskLater(this, () -> {
            if (pendingRequests.remove(target.getUniqueId(), request)) {
                Player onlineRequester = Bukkit.getPlayer(requester.getUniqueId());
                if (onlineRequester != null) {
                    onlineRequester.sendMessage(PREFIX + ChatColor.RED + "Your teleport request to "
                            + target.getName() + " expired.");
                }
                if (target.isOnline()) {
                    target.sendMessage(PREFIX + ChatColor.GRAY + "The teleport request expired.");
                }
            }
        }, expirySeconds * 20L);
        return expiryTask != null;
    }

    private boolean acceptRequest(Player target) {
        TeleportRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null || request.expiresAtNanos() <= System.nanoTime()) {
            target.sendMessage(PREFIX + ChatColor.RED + "You have no pending teleport request.");
            return true;
        }
        Player requester = Bukkit.getPlayer(request.requesterId());
        if (requester == null) {
            target.sendMessage(PREFIX + ChatColor.RED + "That player is no longer online.");
            return true;
        }
        requester.teleportAsync(target.getLocation()).thenAccept(success ->
                Bukkit.getScheduler().runTask(this, () -> {
                    if (!requester.isOnline()) return;
                    requester.sendMessage(PREFIX + (success
                            ? ChatColor.GREEN + "Teleporting to " + target.getName() + "."
                            : ChatColor.RED + "Teleport failed."));
                    if (target.isOnline()) {
                        target.sendMessage(PREFIX + (success
                                ? ChatColor.GREEN + requester.getName() + " teleported to you."
                                : ChatColor.RED + "Teleport failed."));
                    }
                }));
        return true;
    }

    private boolean denyRequest(Player target) {
        TeleportRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) {
            target.sendMessage(PREFIX + ChatColor.RED + "You have no pending teleport request.");
            return true;
        }
        target.sendMessage(PREFIX + ChatColor.GRAY + "Teleport request denied.");
        Player requester = Bukkit.getPlayer(request.requesterId());
        if (requester != null) {
            requester.sendMessage(PREFIX + ChatColor.RED + target.getName() + " denied your teleport request.");
        }
        return true;
    }

    private boolean randomTeleport(Player player) {
        World world = Bukkit.getWorlds().stream()
                .filter(candidate -> candidate.getEnvironment() == World.Environment.NORMAL)
                .findFirst().orElse(null);
        if (world == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "No Overworld is available for random teleport.");
            return true;
        }
        long now = System.nanoTime();
        long readyAt = rtpCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (readyAt > now) {
            long secondsLeft = (readyAt - now + 999_999_999L) / 1_000_000_000L;
            player.sendMessage(PREFIX + ChatColor.YELLOW + "Random teleport recharging: " + secondsLeft + "s.");
            return true;
        }

        int radius = Math.min(500, Math.max(1, getConfig().getInt("rtp.max-radius", 500)));
        WorldBorder border = world.getWorldBorder();
        Location center = world.getSpawnLocation();
        double halfBorder = border.getSize() / 2.0 - 16.0;
        double safeRadius = Math.min(radius, halfBorder);
        if (safeRadius < 1.0) {
            player.sendMessage(PREFIX + ChatColor.RED + "The Overworld border is too small for random teleport.");
            return true;
        }

        for (int attempt = 0; attempt < RTP_ATTEMPTS; attempt++) {
            double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
            double distance = ThreadLocalRandom.current().nextDouble(0, Math.max(0.001, safeRadius));
            int x = (int) Math.round(center.getX() + Math.cos(angle) * distance);
            int z = (int) Math.round(center.getZ() + Math.sin(angle) * distance);
            Location destination = findSafeSurface(world, x, z);
            if (destination == null || !border.isInside(destination)) continue;

            rtpCooldowns.put(player.getUniqueId(), now
                    + Math.max(0, getConfig().getLong("rtp.cooldown-seconds", 600)) * 1_000_000_000L);
            player.sendMessage(PREFIX + ChatColor.YELLOW + "Finding a safe random location...");
            player.teleportAsync(destination).thenAccept(success ->
                    Bukkit.getScheduler().runTask(this, () -> {
                        if (player.isOnline()) {
                            player.sendMessage(PREFIX + (success
                                    ? ChatColor.GREEN + "Random teleport complete."
                                    : ChatColor.RED + "Teleport failed."));
                        }
                    }));
            return true;
        }
        player.sendMessage(PREFIX + ChatColor.RED + "Couldn't find a safe spot. Please try /rtp again.");
        return true;
    }

    private Location findSafeSurface(World world, int x, int z) {
        Block floor = world.getHighestBlockAt(x, z);
        Block feet = floor.getRelative(0, 1, 0);
        if (feet.getY() < world.getMinHeight() || feet.getY() + 1 >= world.getMaxHeight()) return null;
        if (!floor.getType().isSolid() || isUnsafe(floor) || floor.isLiquid()) return null;
        if (!feet.isPassable() || !feet.getRelative(0, 1, 0).isPassable()) return null;
        return new Location(world, x + 0.5, feet.getY(), z + 0.5);
    }

    private boolean isUnsafe(Block block) {
        Material type = block.getType();
        return Tag.LEAVES.isTagged(type) || type == Material.CACTUS || type == Material.MAGMA_BLOCK
                || type == Material.CAMPFIRE || type == Material.SOUL_CAMPFIRE || type == Material.FIRE
                || type == Material.SOUL_FIRE || type == Material.POWDER_SNOW
                || type == Material.SWEET_BERRY_BUSH || type == Material.WITHER_ROSE;
    }

    private World getHomeWorld(String path) {
        String worldId = homes.getString(path + ".world-uuid");
        if (worldId != null) {
            try {
                World world = Bukkit.getWorld(UUID.fromString(worldId));
                if (world != null) return world;
            } catch (IllegalArgumentException exception) {
                getLogger().warning("Invalid saved world UUID for " + path + ": " + worldId);
            }
        }
        String worldName = homes.getString(path + ".world");
        return worldName == null ? null : Bukkit.getWorld(worldName);
    }

    private boolean saveHomes(Player player) {
        try {
            homes.save(homesFile);
            return true;
        } catch (IOException exception) {
            getLogger().log(Level.SEVERE, "Could not save homes.yml", exception);
            player.sendMessage(PREFIX + ChatColor.RED + "Couldn't save your home. Check the server log.");
            return false;
        }
    }

    private String normalizeHomeName(String name) {
        if (name == null || !name.matches("[A-Za-z0-9_-]{1,16}")) return null;
        return name.toLowerCase(Locale.ROOT);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        if (sender instanceof Player player && args.length == 1
                && (name.equals("home") || name.equals("delhome"))) {
            ConfigurationSection section = homes.getConfigurationSection("homes." + player.getUniqueId());
            if (section == null) return List.of();
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return section.getKeys(false).stream()
                    .filter(home -> home.startsWith(prefix))
                    .sorted(Comparator.naturalOrder())
                    .toList();
        }
        if (name.equals("tpa") && args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .filter(player -> !(sender instanceof Player source) || !player.equals(source))
                    .map(Player::getName)
                    .filter(playerName -> playerName.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }
        return List.of();
    }

    private record TeleportRequest(UUID requesterId, UUID targetId, long expiresAtNanos) {}
}
