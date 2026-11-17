package com.eagler.guide;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EaglerServerGuide extends JavaPlugin implements Listener {

    private GuideKnowledgeBase knowledgeBase;
    private GuideGuiManager guiManager;

    @Override
    public void onEnable() {
        knowledgeBase = new GuideKnowledgeBase();
        guiManager = new GuideGuiManager(knowledgeBase);

        Bukkit.getPluginManager().registerEvents(guiManager, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        GuideCommand command = new GuideCommand(knowledgeBase, guiManager);
        if (getCommand("guide") != null) {
            getCommand("guide").setExecutor(command);
            getCommand("guide").setTabCompleter(command);
        } else {
            throw new IllegalStateException("The /guide command is missing from plugin.yml.");
        }

        getLogger().info("Enabled with " + knowledgeBase.getAllEntries().size()
                + " indexed entries across the server plugins.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (player.isOnline()) {
                player.sendMessage(ChatColor.GOLD + "✦ [Server Guide] " + ChatColor.YELLOW + "Type "
                        + ChatColor.AQUA + "/guide" + ChatColor.YELLOW + " for the interactive menu or "
                        + ChatColor.AQUA + "/ai <question>" + ChatColor.YELLOW + " to search.");
            }
        }, 40L);
    }
}
