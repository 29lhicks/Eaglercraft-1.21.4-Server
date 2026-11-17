package com.eagler.bosses;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BossCommand implements CommandExecutor, TabCompleter {

    private final ItemBridge itemBridge;
    private final BossManager bossManager;

    public BossCommand(ItemBridge itemBridge, BossManager bossManager) {
        this.itemBridge = itemBridge;
        this.bossManager = bossManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("summonboss")) {
            return summonBoss(sender, args);
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(ChatColor.GOLD + "=== Custom Bosses ===");
            for (BossType b : BossType.values()) {
                sender.sendMessage(b.getDisplayName() + ChatColor.GRAY + " (" + b.getId() + ") - HP: " + (int) b.getMaxHealth());
            }
            sender.sendMessage(ChatColor.GOLD + "=== Elite Mobs ===");
            for (EliteMobType e : EliteMobType.values()) {
                sender.sendMessage(e.getDisplayName() + ChatColor.GRAY + " (" + e.getId() + ") - HP: " + (int) e.getMaxHealth());
            }
            sender.sendMessage(ChatColor.GOLD + "=== Custom Materials & Crystals ===");
            for (CustomMaterialType m : CustomMaterialType.values()) {
                sender.sendMessage(m.getDisplayName() + ChatColor.GRAY + " (" + m.getId() + ")");
            }
            return true;
        }

        if (!sender.hasPermission("eaglerbosses.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use admin boss commands.");
            return true;
        }

        if (args[0].equalsIgnoreCase("spawn") && sender instanceof Player player && args.length >= 2) {
            BossType type = BossType.fromId(args[1]);
            if (type == null) {
                player.sendMessage(ChatColor.RED + "Unknown boss ID. Options: ignis, boreas, malakor, goliath.");
                return true;
            }
            bossManager.spawnBoss(player.getLocation().add(3, 0, 0), type);
            return true;
        }

        if (args[0].equalsIgnoreCase("elite") && sender instanceof Player player && args.length >= 2) {
            EliteMobType type = EliteMobType.fromId(args[1]);
            if (type == null) {
                player.sendMessage(ChatColor.RED + "Unknown elite ID. Options: cinder_knight, gale_stalker, void_acolyte, obsidian_sentinel.");
                return true;
            }
            bossManager.spawnElite(player.getLocation().add(2, 0, 0), type);
            player.sendMessage(ChatColor.GREEN + "Spawned " + type.getDisplayName());
            return true;
        }

        if (args[0].equalsIgnoreCase("give") && args.length >= 2) {
            CustomMaterialType mat = CustomMaterialType.fromId(args[1]);
            if (mat == null) {
                sender.sendMessage(ChatColor.RED + "Unknown material/crystal ID. Use /cboss list.");
                return true;
            }
            Player target = args.length >= 3 ? Bukkit.getPlayer(args[2]) : (sender instanceof Player p ? p : null);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Target player not found.");
                return true;
            }
            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(ChatColor.RED + "Amount must be a positive whole number.");
                    return true;
                }
                if (amount < 1) {
                    sender.sendMessage(ChatColor.RED + "Amount must be a positive whole number.");
                    return true;
                }
            }
            Map<Integer, ItemStack> leftovers = target.getInventory().addItem(itemBridge.createMaterial(mat, amount));
            leftovers.values().forEach(item -> target.getWorld().dropItemNaturally(target.getLocation(), item));
            sender.sendMessage(ChatColor.GREEN + "Gave " + amount + "x " + mat.getDisplayName() + ChatColor.GREEN + " to " + target.getName() + ".");
            if (!leftovers.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "The target's inventory was full; remaining items were dropped nearby.");
            }
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /cboss <spawn|elite|give|list> [id] [player] [amount]");
        return true;
    }

    private boolean summonBoss(CommandSender sender, String[] args) {
        if (!sender.hasPermission("eaglerbosses.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to summon bosses.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Run /summonboss in-game so the boss has a spawn location.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /summonboss <ignis|boreas|malakor|goliath>");
            return true;
        }

        BossType type = BossType.fromId(args[0]);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown boss. Choose ignis, boreas, malakor, or goliath.");
            return true;
        }
        bossManager.spawnBoss(player.getLocation().add(3, 0, 0), type);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("summonboss")) {
            if (args.length == 1 && sender.hasPermission("eaglerbosses.admin")) {
                for (BossType boss : BossType.values()) out.add(boss.getId());
            }
            return out;
        }

        if (args.length == 1) {
            out.add("list");
            if (sender.hasPermission("eaglerbosses.admin")) {
                out.addAll(Arrays.asList("spawn", "elite", "give"));
            }
        } else if (args.length == 2) {
            if (!sender.hasPermission("eaglerbosses.admin")) {
                return out;
            } else if (args[0].equalsIgnoreCase("spawn")) {
                for (BossType b : BossType.values()) out.add(b.getId());
            } else if (args[0].equalsIgnoreCase("elite")) {
                for (EliteMobType e : EliteMobType.values()) out.add(e.getId());
            } else if (args[0].equalsIgnoreCase("give")) {
                for (CustomMaterialType m : CustomMaterialType.values()) out.add(m.getId());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")
                && sender.hasPermission("eaglerbosses.admin")) {
            for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        }
        return out;
    }
}
