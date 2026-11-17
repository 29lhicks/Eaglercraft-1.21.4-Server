package com.customrealms.relics.network;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BungeeNotifier {
    private final JavaPlugin plugin;

    public BungeeNotifier(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void broadcastRareDropAcrossNetwork(Player trigger, String message) {
        if (!plugin.getConfig().getBoolean("bungeecord-announcements", true)) return;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Message");
        out.writeUTF("ALL");
        out.writeUTF(message);
        trigger.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
