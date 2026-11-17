package com.eagler.gear;

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

public class GearCommand implements CommandExecutor, TabCompleter {

    private final GearManager gearManager;

    public GearCommand(GearManager gearManager) {
        this.gearManager = gearManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(ChatColor.GOLD + "=== Custom Gear, Weapons, Tools & Health (" + CustomGearType.values().length + ") ===");
            for (CustomGearType t : CustomGearType.values()) {
                sender.sendMessage(t.getDisplayName() + ChatColor.GRAY + " (" + t.getId() + ")");
            }
            return true;
        }

        if (!sender.hasPermission("eaglergear.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to give custom gear.");
            return true;
        }

        if (args[0].equalsIgnoreCase("give") && args.length >= 2) {
            CustomGearType type = CustomGearType.fromId(args[1]);
            if (type == null) {
                sender.sendMessage(ChatColor.RED + "Unknown item ID. Use /cgear list.");
                return true;
            }
            Player target = args.length >= 3 ? Bukkit.getPlayer(args[2]) : (sender instanceof Player p ? p : null);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
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
            Map<Integer, ItemStack> leftovers = target.getInventory().addItem(gearManager.createItem(type, amount));
            leftovers.values().forEach(item -> target.getWorld().dropItemNaturally(target.getLocation(), item));
            sender.sendMessage(ChatColor.GREEN + "Gave " + amount + "x " + type.getDisplayName() + ChatColor.GREEN + " to " + target.getName() + ".");
            if (!leftovers.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "The target's inventory was full; remaining items were dropped nearby.");
            }
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /cgear <give|list> [id] [player] [amount]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("list");
            if (sender.hasPermission("eaglergear.admin")) {
                out.add("give");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("eaglergear.admin")) return out;
            for (CustomGearType t : CustomGearType.values()) out.add(t.getId());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("eaglergear.admin")) return out;
            for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        }
        return out;
    }
}
