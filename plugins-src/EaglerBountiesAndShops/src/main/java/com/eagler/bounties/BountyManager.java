package com.eagler.bounties;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BountyManager {

    public record BountyContract(
            String targetId,
            String displayName,
            int requiredKills,
            double rewardMoney
    ) {}

    public static class PlayerProgress {
        public BountyContract contract;
        public int kills;

        public PlayerProgress(BountyContract contract) {
            this.contract = contract;
            this.kills = 0;
        }
    }

    private static final List<BountyContract> CONTRACTS = List.of(
            new BountyContract("ZOMBIE", "Slay 12 Zombies", 12, 75.0),
            new BountyContract("SKELETON", "Slay 10 Skeletons", 10, 80.0),
            new BountyContract("CREEPER", "Slay 8 Creepers", 8, 95.0),
            new BountyContract("ENDERMAN", "Slay 5 Endermen", 5, 110.0),
            new BountyContract("BLAZE", "Slay 6 Blazes", 6, 120.0),
            new BountyContract("elite:cinder_knight", "Hunt 2 Cinder Knights", 2, 240.0),
            new BountyContract("elite:gale_stalker", "Hunt 2 Gale Stalkers", 2, 230.0),
            new BountyContract("elite:void_acolyte", "Hunt 2 Void Acolytes", 2, 250.0),
            new BountyContract("elite:obsidian_sentinel", "Hunt 2 Obsidian Sentinels", 2, 260.0),
            new BountyContract("boss:ignis", "Defeat Ignis, The Magma Sovereign", 1, 550.0),
            new BountyContract("boss:boreas", "Defeat Boreas, Stormwing Warden", 1, 550.0),
            new BountyContract("boss:malakor", "Defeat Malakor, Void Reaper", 1, 600.0),
            new BountyContract("boss:goliath", "Defeat Goliath, Obsidian Colossus", 1, 650.0)
    );

    private final JavaPlugin plugin;
    private final EcoBridge ecoBridge;
    private final Map<UUID, PlayerProgress> activeBounties = new HashMap<>();

    public BountyManager(JavaPlugin plugin, EcoBridge ecoBridge) {
        this.plugin = plugin;
        this.ecoBridge = ecoBridge;
    }

    public PlayerProgress getOrAssign(Player player) {
        return activeBounties.computeIfAbsent(player.getUniqueId(), id ->
                new PlayerProgress(CONTRACTS.get(ThreadLocalRandom.current().nextInt(CONTRACTS.size())))
        );
    }

    public void assignNewBounty(Player player) {
        BountyContract next = CONTRACTS.get(ThreadLocalRandom.current().nextInt(CONTRACTS.size()));
        activeBounties.put(player.getUniqueId(), new PlayerProgress(next));
        sendBountyStatus(player);
    }

    public void sendBountyStatus(Player player) {
        PlayerProgress prog = getOrAssign(player);
        player.sendMessage(ChatColor.GOLD + "=== Active Bounty Contract ===");
        player.sendMessage(ChatColor.YELLOW + "Task: " + ChatColor.WHITE + prog.contract.displayName()
                + ChatColor.GRAY + " (" + prog.kills + "/" + prog.contract.requiredKills() + ")");
        player.sendMessage(ChatColor.GREEN + "Reward: $" + String.format("%.2f", prog.contract.rewardMoney())
                + ChatColor.DARK_GRAY + " | Use /bounty reroll ($50)");
    }

    public void recordKill(Player killer, LivingEntity victim) {
        PlayerProgress prog = getOrAssign(killer);
        String customId = ecoBridge.getBossOrEliteId(victim);
        EntityType vanillaType = victim.getType();

        if (customId != null) {
            if (customId.startsWith("boss:")) {
                if (ecoBridge.deposit(killer.getUniqueId(), 250.0)) {
                    killer.sendMessage(ChatColor.GOLD + "+$250.00 Boss Slaying Bonus deposited to your balance!");
                }
            } else if (customId.startsWith("elite:")) {
                if (ecoBridge.deposit(killer.getUniqueId(), 45.0)) {
                    killer.sendMessage(ChatColor.AQUA + "+$45.00 Elite Hunter Bonus!");
                }
            }
        }

        boolean matches = (customId != null && customId.equalsIgnoreCase(prog.contract.targetId()))
                || (customId == null && vanillaType.name().equalsIgnoreCase(prog.contract.targetId()));

        if (!matches) return;

        prog.kills++;
        if (prog.kills >= prog.contract.requiredKills()) {
            double reward = prog.contract.rewardMoney();
            if (!ecoBridge.deposit(killer.getUniqueId(), reward)) {
                killer.sendMessage(ChatColor.RED + "The bounty reward could not be deposited. Keep this contract and try again later.");
                return;
            }
            killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            killer.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "BOUNTY COMPLETE! "
                    + ChatColor.YELLOW + "Earned $" + String.format("%.2f", reward) + "!");
            assignNewBounty(killer);
        } else {
            killer.sendMessage(ChatColor.YELLOW + "Bounty Progress: " + prog.kills + "/" + prog.contract.requiredKills()
                    + " (" + prog.contract.displayName() + ")");
        }
    }
}
