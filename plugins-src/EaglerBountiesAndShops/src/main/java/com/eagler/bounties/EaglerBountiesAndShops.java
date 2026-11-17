package com.eagler.bounties;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EaglerBountiesAndShops extends JavaPlugin {

    private EcoBridge ecoBridge;
    private BountyManager bountyManager;
    private BlackMarketGui blackMarketGui;

    @Override
    public void onEnable() {
        this.ecoBridge = new EcoBridge(this);
        this.bountyManager = new BountyManager(this, ecoBridge);
        this.blackMarketGui = new BlackMarketGui(ecoBridge);

        getServer().getPluginManager().registerEvents(
                new BountyShopListener(this, ecoBridge, bountyManager, blackMarketGui), this
        );

        if (getCommand("shop") != null) {
            getCommand("shop").setExecutor((sender, command, label, args) -> {
                if (sender instanceof Player player) {
                    blackMarketGui.openShop(player);
                } else {
                    sender.sendMessage("Only players can open the Black Market shop.");
                }
                return true;
            });
        }

        if (getCommand("bounty") != null) {
            getCommand("bounty").setExecutor((sender, command, label, args) -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can view bounties.");
                    return true;
                }
                if (args.length > 0 && args[0].equalsIgnoreCase("reroll")) {
                    if (ecoBridge.withdraw(player.getUniqueId(), 50.0)) {
                        bountyManager.assignNewBounty(player);
                        player.sendMessage(ChatColor.YELLOW + "Paid $50.00 to reroll your bounty contract!");
                    } else {
                        player.sendMessage(ChatColor.RED + "You need $50.00 to reroll your bounty.");
                    }
                    return true;
                }
                bountyManager.sendBountyStatus(player);
                return true;
            });
        }

        getLogger().info("EaglerBountiesAndShops enabled! Linked with DynamicEco, Bosses, Gear, and Enchants.");
    }
}
