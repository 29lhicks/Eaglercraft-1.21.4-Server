package com.eagler.enchants;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnchantCommand implements CommandExecutor, TabCompleter {

    private final EnchantmentManager manager;

    public EnchantCommand(EnchantmentManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(ChatColor.GOLD + "=== Custom Enchantments (" + EnchantmentType.values().length + ") ===");
            for (EnchantmentType t : EnchantmentType.values()) {
                sender.sendMessage(t.getColor() + t.getKey() + ChatColor.GRAY + " (Max " + t.getMaxLevel() + ") - " + t.getDescription());
            }
            return true;
        }

        if (!sender.hasPermission("eaglerenchants.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to give or directly apply custom enchantments.");
            return true;
        }

        if (args[0].equalsIgnoreCase("give") && args.length >= 3) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            EnchantmentType type = EnchantmentType.fromKey(args[2]);
            if (type == null) {
                sender.sendMessage(ChatColor.RED + "Unknown enchantment. Use /ce list.");
                return true;
            }
            Integer level = parseLevel(args, 3, type, sender);
            if (level == null) return true;
            ItemStack book = manager.createEnchantedBook(type, level);
            for (ItemStack leftover : target.getInventory().addItem(book).values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), leftover);
            }
            sender.sendMessage(ChatColor.GREEN + "Gave " + type.getDisplayName() + " " + level + " book to " + target.getName() + ".");
            return true;
        }

        if (args[0].equalsIgnoreCase("apply") && sender instanceof Player player && args.length >= 2) {
            EnchantmentType type = EnchantmentType.fromKey(args[1]);
            if (type == null) {
                sender.sendMessage(ChatColor.RED + "Unknown enchantment.");
                return true;
            }
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.AIR) {
                sender.sendMessage(ChatColor.RED + "Hold an item in your main hand.");
                return true;
            }
            if (!type.canApplyTo(hand)) {
                sender.sendMessage(ChatColor.RED + type.getDisplayName() + " cannot be applied to that item.");
                return true;
            }
            Integer level = parseLevel(args, 2, type, sender);
            if (level == null) return true;
            manager.addEnchantment(hand, type, level);
            sender.sendMessage(ChatColor.GREEN + "Applied " + type.getDisplayName() + " " + level + " to your item.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /ce <give|apply|list> [player] [enchant] [level]");
        return true;
    }

    private Integer parseLevel(String[] args, int index, EnchantmentType type, CommandSender sender) {
        if (args.length <= index) return 1;
        try {
            int level = Integer.parseInt(args[index]);
            if (level < 1 || level > type.getMaxLevel()) {
                sender.sendMessage(ChatColor.RED + "Level must be between 1 and " + type.getMaxLevel() + ".");
                return null;
            }
            return level;
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Level must be a whole number.");
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("list");
            if (sender.hasPermission("eaglerenchants.admin")) {
                out.addAll(Arrays.asList("give", "apply"));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("eaglerenchants.admin")) return out;
            for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        } else if ((args.length == 2 && args[0].equalsIgnoreCase("apply")) || (args.length == 3 && args[0].equalsIgnoreCase("give"))) {
            if (!sender.hasPermission("eaglerenchants.admin")) return out;
            for (EnchantmentType t : EnchantmentType.values()) out.add(t.getKey());
        }
        return out;
    }
}
