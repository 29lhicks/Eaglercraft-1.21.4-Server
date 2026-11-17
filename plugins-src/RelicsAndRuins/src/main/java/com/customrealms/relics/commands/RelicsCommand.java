package com.customrealms.relics.commands;

import com.customrealms.relics.items.CustomItemDefinition;
import com.customrealms.relics.items.CustomItemRegistry;
import com.customrealms.relics.structures.StructureGeneratorListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RelicsCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final CustomItemRegistry registry;

    public RelicsCommand(JavaPlugin plugin, CustomItemRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only command.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /relics <give|structure|list>", NamedTextColor.YELLOW));
            return true;
        }
        String subcommand = args[0].toLowerCase();
        if (subcommand.equals("list")) {
            player.sendMessage(Component.text("=== Registered Custom Relics (" + registry.getAllItems().size() + ") ===", NamedTextColor.GOLD));
            for (CustomItemDefinition def : registry.getAllItems()) {
                player.sendMessage(Component.text("- " + def.getId() + " (" + def.getCategory() + " | " + def.getSource() + ")", NamedTextColor.GRAY));
            }
            return true;
        }
        if (!subcommand.equals("give") && !subcommand.equals("structure")) {
            player.sendMessage(Component.text("Usage: /relics <give|structure|list>", NamedTextColor.YELLOW));
            return true;
        }
        if (!player.hasPermission("relics.admin")) {
            player.sendMessage(Component.text("You do not have permission to give relics or place structures.", NamedTextColor.RED));
            return true;
        }

        switch (subcommand) {
            case "give" -> {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Specify an item ID.", NamedTextColor.RED));
                    return true;
                }
                ItemStack item = registry.createItem(args[1]);
                if (item == null) {
                    player.sendMessage(Component.text("Unknown relic ID: " + args[1], NamedTextColor.RED));
                    return true;
                }
                player.getInventory().addItem(item);
                player.sendMessage(Component.text("Given " + args[1], NamedTextColor.GREEN));
            }
            case "structure" -> {
                StructureGeneratorListener gen = new StructureGeneratorListener(plugin, registry);
                String type = args.length > 1 ? args[1].toLowerCase() : "obelisk";
                switch (type) {
                    case "forge" -> gen.buildForgottenForge(player.getLocation(), new Random());
                    case "shrine" -> gen.buildAstralShrine(player.getLocation(), new Random());
                    case "crypt" -> gen.buildCryptSanctum(player.getLocation(), new Random());
                    default -> gen.buildSunkenObelisk(player.getLocation(), new Random());
                }
                player.sendMessage(Component.text("Generated structure (" + type + ") at your location!", NamedTextColor.AQUA));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.addAll(List.of("give", "structure", "list"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            registry.getAllItems().forEach(i -> out.add(i.getId()));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("structure")) {
            out.addAll(List.of("obelisk", "forge", "shrine", "crypt"));
        }
        return out;
    }
}
