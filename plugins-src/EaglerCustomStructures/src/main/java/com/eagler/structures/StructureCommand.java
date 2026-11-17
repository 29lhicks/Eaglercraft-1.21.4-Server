package com.eagler.structures;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class StructureCommand implements CommandExecutor, TabCompleter {

    private final StructureBuilder builder;

    public StructureCommand(StructureBuilder builder) {
        this.builder = builder;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(ChatColor.GOLD + "=== Custom Structures (" + StructureType.values().length + ") ===");
            for (StructureType t : StructureType.values()) {
                sender.sendMessage(ChatColor.AQUA + t.getKey() + ChatColor.GRAY + " - " + t.getDisplayName());
            }
            return true;
        }

        if (!sender.hasPermission("eaglerstructures.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to spawn custom structures.");
            return true;
        }

        if (args[0].equalsIgnoreCase("spawn") && sender instanceof Player player) {
            StructureType chosen;
            if (args.length >= 2) {
                chosen = StructureType.fromKey(args[1]);
                if (chosen == null) {
                    player.sendMessage(ChatColor.RED + "Unknown structure type. Use /cstruct list.");
                    return true;
                }
            } else {
                StructureType[] all = StructureType.values();
                chosen = all[ThreadLocalRandom.current().nextInt(all.length)];
            }

            Location target = player.getLocation().getBlock().getLocation().add(4, 0, 0);
            builder.buildStructure(target, chosen);
            player.sendMessage(ChatColor.GREEN + "Spawned " + chosen.getDisplayName() + " nearby with custom enchanted loot!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /cstruct <spawn|list> [structure_type]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("list");
            if (sender.hasPermission("eaglerstructures.admin")) {
                out.add("spawn");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            if (!sender.hasPermission("eaglerstructures.admin")) return out;
            for (StructureType t : StructureType.values()) out.add(t.getKey());
        }
        return out;
    }
}
