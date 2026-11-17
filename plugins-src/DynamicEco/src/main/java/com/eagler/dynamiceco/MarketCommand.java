package com.eagler.dynamiceco;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

final class MarketCommand implements CommandExecutor, TabCompleter {
    private static final int MAX_AMOUNT = 2304;
    private final EconomyManager economy;

    MarketCommand(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return switch (command.getName().toLowerCase(Locale.ROOT)) {
            case "bal" -> handleBalance(sender, args);
            case "pay" -> handlePay(sender, args);
            case "market" -> handleMarket(sender, args);
            default -> false;
        };
    }

    private boolean handleBalance(CommandSender sender, String[] args) {
        OfflinePlayer target;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Usage: /bal <online player>");
                return true;
            }
            target = player;
        } else if (args.length == 1) {
            if (!sender.hasPermission("dynamiceco.balance.others")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to view other balances.");
                return true;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "That player is not online.");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /bal [player]");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + target.getName() + "'s balance: $"
                + format(economy.getBalance(target.getUniqueId())));
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use /pay.");
            return true;
        }
        if (args.length != 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /pay <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || target.equals(player)) {
            player.sendMessage(ChatColor.RED + "Choose another online player.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Enter a valid amount.");
            return true;
        }
        if (!Double.isFinite(amount) || amount <= 0.0) {
            player.sendMessage(ChatColor.RED + "The amount must be greater than zero.");
            return true;
        }

        amount = economy.round(amount);
        if (amount <= 0.0 || !economy.withdraw(player.getUniqueId(), amount)) {
            player.sendMessage(ChatColor.RED + "You do not have enough money.");
            return true;
        }
        if (!economy.deposit(target.getUniqueId(), amount)) {
            economy.deposit(player.getUniqueId(), amount);
            player.sendMessage(ChatColor.RED + "That payment could not be completed.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Sent $" + format(amount) + " to " + target.getName() + ".");
        target.sendMessage(ChatColor.GREEN + "Received $" + format(amount) + " from " + player.getName() + ".");
        return true;
    }

    private boolean handleMarket(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /market <price|buy|sell> <item|hand> [amount]");
            return true;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        if (args[1].equalsIgnoreCase("hand")) {
            if (!action.equals("price") && !action.equals("sell")) {
                sender.sendMessage(ChatColor.YELLOW + "Use /market price hand or /market sell hand [amount].");
                return true;
            }
            if (action.equals("price") && args.length == 3) {
                sender.sendMessage(ChatColor.YELLOW + "Usage: /market price hand");
                return true;
            }
            int amount = args.length == 3 ? parseAmount(sender, args[2]) : 1;
            if (amount < 1) {
                return true;
            }
            return action.equals("price") ? showCustomPrice(sender) : sellCustom(sender, amount);
        }

        Material material = Material.matchMaterial(args[1]);
        if (material == null || !economy.isMarketItem(material)) {
            sender.sendMessage(ChatColor.RED + "That item is not traded on the market.");
            return true;
        }

        if (action.equals("price") && args.length == 3) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /market price <item>");
            return true;
        }

        int amount = args.length == 3 ? parseAmount(sender, args[2]) : 1;
        if (amount < 1) {
            return true;
        }

        return switch (action) {
            case "price" -> showPrice(sender, material);
            case "buy" -> buy(sender, material, amount);
            case "sell" -> sell(sender, material, amount);
            default -> {
                sender.sendMessage(ChatColor.YELLOW + "Usage: /market <price|buy|sell> <item|hand> [amount]");
                yield true;
            }
        };
    }

    private int parseAmount(CommandSender sender, String input) {
        try {
            int amount = Integer.parseInt(input);
            if (amount >= 1 && amount <= MAX_AMOUNT) {
                return amount;
            }
        } catch (NumberFormatException ignored) {
        }
        sender.sendMessage(ChatColor.RED + "Amount must be between 1 and " + MAX_AMOUNT + ".");
        return -1;
    }

    private boolean showCustomPrice(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can check held items.");
            return true;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        String key = economy.getCustomMarketKey(held);
        if (!economy.isCustomMarketItem(key)) {
            player.sendMessage(ChatColor.RED + "Hold a configured custom gear item or boss material.");
            return true;
        }
        player.sendMessage(ChatColor.GOLD + "Custom item sell price: $"
                + format(economy.getCustomSellPrice(key)) + " each.");
        return true;
    }

    private boolean sellCustom(CommandSender sender, int amount) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can sell custom items.");
            return true;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        String key = economy.getCustomMarketKey(held);
        if (!economy.isCustomMarketItem(key)) {
            player.sendMessage(ChatColor.RED + "Hold a configured custom gear item or boss material.");
            return true;
        }
        if (amount > held.getAmount()) {
            player.sendMessage(ChatColor.RED + "You only have " + held.getAmount() + " in your hand.");
            return true;
        }

        double payout = economy.round(economy.getCustomSellPrice(key) * amount);
        if (!Double.isFinite(payout) || payout <= 0.0) {
            player.sendMessage(ChatColor.RED + "This custom item has no valid sell price.");
            return true;
        }

        ItemStack original = held.clone();
        if (amount == held.getAmount()) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            held.setAmount(held.getAmount() - amount);
        }
        if (!economy.deposit(player.getUniqueId(), payout)) {
            player.getInventory().setItemInMainHand(original);
            player.sendMessage(ChatColor.RED + "The sale could not be completed; your items were returned.");
            return true;
        }

        economy.recordCustomSale(key, amount);
        String itemName = original.hasItemMeta() && original.getItemMeta().hasDisplayName()
                ? ChatColor.stripColor(original.getItemMeta().getDisplayName()) : original.getType().name();
        player.sendMessage(ChatColor.GREEN + "Sold " + amount + "x " + itemName
                + " for $" + format(payout) + ".");
        return true;
    }

    private boolean showPrice(CommandSender sender, Material material) {
        double base = economy.getBasePrice(material);
        double buy = economy.getBuyPrice(material);
        double sell = economy.getSellPrice(material);
        double difference = (buy - base) / base * 100.0;
        ChatColor trendColor = difference > 1.0 ? ChatColor.RED
                : difference < -1.0 ? ChatColor.AQUA : ChatColor.GRAY;
        String trend = difference > 1.0 ? "Rising (+" + format(difference) + "%)"
                : difference < -1.0 ? "Falling (" + format(difference) + "%)" : "Stable";

        sender.sendMessage(ChatColor.GOLD + "=== " + material.name() + " Market ===");
        sender.sendMessage(ChatColor.WHITE + "Buy: $" + format(buy) + " | Sell: $" + format(sell));
        sender.sendMessage(trendColor + "Price trend: " + trend);
        return true;
    }

    private boolean buy(CommandSender sender, Material material, int amount) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can buy market items.");
            return true;
        }

        double total = economy.round(economy.getBuyPrice(material) * amount);
        if (!Double.isFinite(total) || total <= 0.0
                || !economy.withdraw(player.getUniqueId(), total)) {
            player.sendMessage(ChatColor.RED + "You do not have enough money.");
            return true;
        }

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(new ItemStack(material, amount));
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
        economy.recordPurchase(material, amount);
        player.sendMessage(ChatColor.GREEN + "Bought " + amount + "x " + material.name()
                + " for $" + format(total) + ".");
        player.sendMessage(ChatColor.GRAY + "New unit buy price: $" + format(economy.getBuyPrice(material)));
        return true;
    }

    private boolean sell(CommandSender sender, Material material, int amount) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can sell market items.");
            return true;
        }

        ItemStack required = new ItemStack(material, amount);
        Map<Integer, ItemStack> leftovers = player.getInventory().removeItem(required);
        int notRemoved = leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
        int sold = amount - notRemoved;
        if (sold == 0) {
            player.sendMessage(ChatColor.RED + "You do not have " + amount + "x " + material.name() + ".");
            return true;
        }

        double payout = economy.round(economy.getSellPrice(material) * sold);
        if (!economy.deposit(player.getUniqueId(), payout)) {
            player.getInventory().addItem(new ItemStack(material, sold));
            player.sendMessage(ChatColor.RED + "The sale could not be completed; your items were returned.");
            return true;
        }

        economy.recordSale(material, sold);
        player.sendMessage(ChatColor.GREEN + "Sold " + sold + "x " + material.name()
                + " for $" + format(payout) + ".");
        player.sendMessage(ChatColor.GRAY + "New unit sell price: $" + format(economy.getSellPrice(material)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("market")) {
            if (args.length == 1) {
                return partialMatches(List.of("price", "buy", "sell"), args[0]);
            }
            if (args.length == 2) {
                List<String> suggestions = new java.util.ArrayList<>(economy.getMarketItems().stream()
                        .map(Material::name).toList());
                if (args[0].equalsIgnoreCase("price") || args[0].equalsIgnoreCase("sell")) {
                    suggestions.add("hand");
                }
                return partialMatches(suggestions.stream().sorted().toList(), args[1]);
            }
            if (args.length == 3 && !args[0].equalsIgnoreCase("price")) {
                return partialMatches(List.of("1", "16", "32", "64"), args[2]);
            }
        } else if (args.length == 1 && (command.getName().equalsIgnoreCase("pay")
                || (command.getName().equalsIgnoreCase("bal")
                && sender.hasPermission("dynamiceco.balance.others")))) {
            return partialMatches(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).sorted().toList(), args[0]);
        }
        return List.of();
    }

    private List<String> partialMatches(List<String> values, String input) {
        String prefix = input.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix))
                .collect(Collectors.toList());
    }

    private String format(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }
}
