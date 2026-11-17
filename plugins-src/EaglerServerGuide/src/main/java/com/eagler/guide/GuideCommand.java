package com.eagler.guide;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GuideCommand implements CommandExecutor, TabCompleter {

    private final GuideKnowledgeBase knowledgeBase;
    private final GuideGuiManager gui;

    public GuideCommand(GuideKnowledgeBase knowledgeBase, GuideGuiManager gui) {
        this.knowledgeBase = knowledgeBase;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                gui.openMainMenu(player);
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.1f);
            } else {
                sender.sendMessage("Run /guide <keyword> from console or /guide in-game for the GUI.");
            }
            return true;
        }

        String query = String.join(" ", args);
        List<GuideEntry> matches = knowledgeBase.search(query);
        if (matches.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "[Server Guide] No match for '" + query + "'.");
            sender.sendMessage(ChatColor.YELLOW + "Try: relics, bosses, ignis, armor sets, enchants, grapple, "
                    + "structures, market, skills, or bounty.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "✦ [Server Guide] Results for '"
                + ChatColor.YELLOW + query + ChatColor.GOLD + "':");
        for (GuideEntry entry : matches) {
            sender.sendMessage(ChatColor.DARK_GRAY + "----------------------------------------");
            sender.sendMessage(entry.title() + ChatColor.DARK_GRAY
                    + " [" + entry.category().getDisplayName() + "]");
            for (String line : entry.details()) {
                sender.sendMessage("  " + line);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = List.of(
                "commands", "progression", "relics", "titan", "zephyr", "scythe", "offhand",
                "armor", "juggernaut", "pyromancer", "shadowstalker",
                "bosses", "ignis", "boreas", "malakor", "goliath", "elites",
                "enchants", "grapple", "veinminer", "lifesteal",
                "structures", "market", "skills", "bounty"
        );
        if (args.length != 1) {
            return List.of();
        }

        String prefix = args[0].toLowerCase();
        List<String> matches = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion.startsWith(prefix)) {
                matches.add(suggestion);
            }
        }
        return matches;
    }
}
