package com.eagler.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EaglerSkillTree extends JavaPlugin {

    private SkillManager skillManager;
    private SkillTreeGui skillGui;

    @Override
    public void onEnable() {
        this.skillManager = new SkillManager(this);
        this.skillGui = new SkillTreeGui(skillManager);

        skillManager.startAttributeSyncTask();

        getServer().getPluginManager().registerEvents(new SkillListener(this, skillManager, skillGui), this);

        if (getCommand("skills") != null) {
            getCommand("skills").setExecutor((sender, command, label, args) -> {
                if (sender instanceof Player player) {
                    skillGui.openSkillTree(player);
                } else {
                    sender.sendMessage("Only players can open the Skill Tree GUI.");
                }
                return true;
            });
        }

        if (getCommand("skadmin") != null) {
            getCommand("skadmin").setExecutor((sender, command, label, args) -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /skadmin <give|reset> <player> [amount]");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                if (args[0].equalsIgnoreCase("give")) {
                    int amt = 1;
                    if (args.length >= 3) {
                        try {
                            amt = Integer.parseInt(args[2]);
                        } catch (NumberFormatException exception) {
                            sender.sendMessage(ChatColor.RED + "Amount must be a positive whole number.");
                            return true;
                        }
                    }
                    if (amt <= 0 || (long) skillManager.getSkillPoints(target) + amt > Integer.MAX_VALUE) {
                        sender.sendMessage(ChatColor.RED + "Amount must be positive and fit within the skill point limit.");
                        return true;
                    }
                    skillManager.addSkillPoints(target, amt, "Admin Grant");
                    sender.sendMessage(ChatColor.GREEN + "Granted " + amt + " skill point(s) to " + target.getName() + ".");
                } else if (args[0].equalsIgnoreCase("reset")) {
                    skillManager.resetSkills(target);
                    sender.sendMessage(ChatColor.GREEN + "Reset skill tree for " + target.getName() + ".");
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /skadmin <give|reset> <player> [amount]");
                }
                return true;
            });
        }

        getLogger().info("EaglerSkillTree enabled! Level 40 milestones & Boss kills now grant Skill Points.");
    }
}
