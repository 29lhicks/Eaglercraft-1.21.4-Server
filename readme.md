Eaglercraft 1.21.4 Server by Ryan Norshare !
<br>
**Step 1:** Installing some stuff :D in a new terminal tyoe `sudo apt install default-jdk && sudo apt install default-jre -y` 
<br>
<br>
**Step 2:** Open two new terminals type `cd server && sudo java -jar server.jar` in the first and in the second type `cd bungee && sudo java -jar bungee.jar` 
<br>
<br>
**Step 3:** Forward ports 8081 (Eaglercraft WebSocket) and 25577 (BungeeCord Java) and make port 8081 public; the link next to it is your server IP.
<br>
<br>
**Step 4:** replace "https://" with "wss://" 
<br>
<br>
**Step 5 (Optional):** to add plugins go to server>plugins and upload them there and then restart your server with `stop` then `sudo java -jar server.jar`  
<br>
<br>
**XP Booster:** XPBooster is installed in `server/plugins` and configured for a global 5x vanilla XP boost every day. Restart the server to load it. Its settings are in `server/plugins/XPBooster/settings.yml`.
<br>
<br>
**Economy and shops:** EssentialsX provides player balances and payments, Vault connects it to other plugins, and EconomyShopGUI provides the item shop. Use `/shop`, `/balance`, and `/pay <player> <amount>`. XPBooster's boost shop is available with `/boosts`. New players start with the EssentialsX default balance of $0; players can earn money by selling items in `/shop`. Restart the server to load the new plugins.

**Homes:** HuskHomes handles `/sethome` and `/home` for all players, with a default limit of three homes per player. The duplicate EssentialsX home commands are disabled to avoid permission conflicts.
<br>
<br>
**Spawn egg drops:** SpawnEggDrops is installed and configured so eligible mobs have a 5% chance to drop one matching spawn egg when killed. Change `spawn_egg_drop_percentage` in `server/plugins/SpawnEggDrops/config.yml` to adjust it. Restart the server to load the plugin.
<br>
<br>
**CrazyEnchantments:** Custom enchantments can be applied to all Paper 1.21.4 item materials, and the per-item custom-enchantment count limit is disabled. Unsafe enchantment levels are enabled, allowing levels above an enchantment's configured maximum when combining/applying books.
<br>
<br>
Note: Remember this server is free and so it is not going to be online 24/7 or lag free so i recommend installing the plugin "Clear Lag" which automatically clears entitys
