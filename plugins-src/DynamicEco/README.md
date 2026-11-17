# DynamicEco

DynamicEco is a standalone Spigot 1.21.4 plugin and does not require the other server plugins to be installed.

## Build

Run `Build-DynamicEco-1.21.4.bat` from the repository root on Windows with Java 21 and Maven installed. It builds the plugin and copies the JAR into `server/plugins` when that folder exists.

Alternatively, run `mvn clean package` in `plugins-src/DynamicEco`. The JAR is written to `target/DynamicEco-1.0.0.jar`.

## Commands

- `/bal [player]` checks your balance, or (with `dynamiceco.balance.others`) the balance of an online player.
- `/pay <player> <amount>` pays an online player.
- `/market price <item>` checks the current buy and sell prices.
- `/market buy <item> [amount]` buys market items. Items that do not fit in the inventory are dropped nearby.
- `/market sell <item> [amount]` sells items from your inventory.
- `/market price hand` checks the configured price of the custom gear or boss material you are holding.
- `/market sell hand [amount]` sells configured custom gear or boss materials held in your main hand. Custom enchanted books are not accepted. Default prices are in `custom-items` in the config.

Market items, starting balance, price volatility, and save/stabilization intervals are configured in `plugins/DynamicEco/config.yml`.
