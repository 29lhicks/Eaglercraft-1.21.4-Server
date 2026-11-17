package com.eagler.guide;

import com.eagler.guide.GuideEntry.Category;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class GuideKnowledgeBase {

    private final List<GuideEntry> entries = new ArrayList<>();

    public GuideKnowledgeBase() {
        registerAll();
    }

    private void add(String id, Category category, String title, Material icon, List<String> keywords,
                     String... details) {
        entries.add(new GuideEntry(id, category, title, icon, keywords, Arrays.asList(details)));
    }

    private void registerAll() {
        add("commands_overview", Category.GETTING_STARTED, ChatColor.GOLD + "Player Commands Directory",
                Material.COMMAND_BLOCK,
                List.of("command", "commands", "help", "how", "start", "begin", "menu", "controls"),
                ChatColor.AQUA + "/guide" + ChatColor.GRAY + " or " + ChatColor.AQUA + "/ai <question>"
                        + ChatColor.WHITE + " - Open this guide or search a topic.",
                ChatColor.AQUA + "/recipes" + ChatColor.WHITE
                        + " - View interactive 3x3 crafting recipes for custom gear and relics.",
                ChatColor.AQUA + "/recipebook" + ChatColor.WHITE
                        + " - Claim the physical Artisan's Recipe Tome.",
                ChatColor.AQUA + "/relics list" + ChatColor.WHITE
                        + " - Browse the custom relic catalog.",
                ChatColor.AQUA + "/skills" + ChatColor.WHITE
                        + " - Open your Skill Tree and spend skill points.",
                ChatColor.AQUA + "/bounty" + ChatColor.WHITE + " and " + ChatColor.AQUA + "/blackmarket"
                        + ChatColor.WHITE + " - Hunt player bounties and access the Black Market.",
                ChatColor.AQUA + "/bal" + ChatColor.WHITE + ", " + ChatColor.AQUA + "/pay" + ChatColor.WHITE
                        + ", " + ChatColor.AQUA + "/market <price|buy|sell>" + ChatColor.WHITE
                        + " - Use the dynamic economy.");

        add("progression_roadmap", Category.GETTING_STARTED, ChatColor.GREEN + "Server Progression Roadmap",
                Material.COMPASS, List.of("progression", "roadmap", "tier", "strong", "best", "gear", "new"),
                ChatColor.YELLOW + "1. Early Game:" + ChatColor.WHITE
                        + " Explore surface structures for custom enchanted books and starter relics.",
                ChatColor.YELLOW + "2. Mid Game:" + ChatColor.WHITE
                        + " Hunt elite mobs for boss materials.",
                ChatColor.YELLOW + "3. Late Game:" + ChatColor.WHITE
                        + " Craft summoning crystals and defeat the four world bosses.",
                ChatColor.YELLOW + "4. Endgame:" + ChatColor.WHITE
                        + " Forge boss armor sets, relic artifacts, and custom enchantments.");

        add("relics_armor_craftable", Category.RELICS,
                ChatColor.GOLD + "Craftable Relic Armor (4 Pieces)", Material.NETHERITE_HELMET,
                List.of("relic", "relics", "titan", "siege", "helm", "cuirass", "zephyr", "hood", "bloodforged",
                        "visor"),
                ChatColor.GOLD + "Titan Siege Helm:" + ChatColor.GREEN
                        + " +4 HP, +4 Armor, +25% Knockback Resistance" + ChatColor.RED + " (-12% Speed)"
                        + ChatColor.GRAY + " [Netherite + Nether Star]",
                ChatColor.GOLD + "Titan Siege Cuirass:" + ChatColor.GREEN
                        + " +6 HP, +9 Armor, Resistance I" + ChatColor.RED + " (-18% Speed, -10% Attack Speed)",
                ChatColor.AQUA + "Zephyr Windrunner Hood:" + ChatColor.GREEN
                        + " +20% Speed, Night Vision" + ChatColor.RED + " (-3 Max HP)"
                        + ChatColor.GRAY + " [Feathers + Diamond Helmet]",
                ChatColor.DARK_RED + "Bloodforged Visor:" + ChatColor.GREEN
                        + " +25% Attack Damage, +15% Attack Speed" + ChatColor.RED + " (-4 HP, Glowing)"
                        + ChatColor.GRAY + " [Redstone Block + Blaze Rod + Iron Helmet]");

        add("relics_armor_loot_boss", Category.RELICS,
                ChatColor.DARK_PURPLE + "Custom Boss & Structure Relic Armor (4 Pieces)", Material.NETHERITE_CHESTPLATE,
                List.of("abyssal", "carapace", "warden", "greaves", "sculk", "solar", "solaris", "cloudstep",
                        "boots"),
                ChatColor.DARK_PURPLE + "Abyssal Carapace (Custom Boss Drop):" + ChatColor.GREEN
                        + " +8 HP, +10 Armor, Darkness Immune" + ChatColor.RED + " (Weakness in sunlight)",
                ChatColor.DARK_AQUA + "Sculk Greaves (Custom Boss Drop):" + ChatColor.GREEN
                        + " +80% Sneak Speed, +6 Armor" + ChatColor.RED + " (-10% Sprint Speed)",
                ChatColor.AQUA + "Zephyr Cloudstep Boots (Structure):" + ChatColor.GREEN
                        + " +25% Speed, Jump II, +4 Safe Fall" + ChatColor.RED + " (-2 Toughness)",
                ChatColor.YELLOW + "Solaris Flame Greaves (Structure):" + ChatColor.GREEN
                        + " Fire Immunity, +15% Damage" + ChatColor.RED + " (Hurt by water)");

        add("relics_weapons", Category.RELICS, ChatColor.RED + "Relic Weapons (8 Weapons)",
                Material.NETHERITE_SWORD,
                List.of("scythe", "soul", "reaver", "chronos", "rapier", "quickblade", "earthshatter", "maul",
                        "venomfang", "starfall", "longbow", "void", "cleaver", "mace", "maces",
                        "cinderwake", "wardenfall"),
                ChatColor.GRAY + "Custom boss relic drops come from Ignis, Boreas, Malakor, and Goliath—not vanilla bosses.",
                ChatColor.DARK_PURPLE + "Soul Reaver Scythe (Custom Boss Drop):" + ChatColor.GREEN
                        + " 11 Damage, +1 Reach, Heals 2 HP/kill" + ChatColor.RED + " (-25% Attack Speed)",
                ChatColor.DARK_RED + "Voidrend Executioner (Custom Boss Drop):" + ChatColor.GREEN
                        + " 12 Damage, +50% Damage vs. targets below 40% HP"
                        + ChatColor.RED + " (1 self-damage/swing)",
                ChatColor.AQUA + "Chronos Quickblade (Craft):" + ChatColor.GREEN
                        + " Fast swing, +15% Speed" + ChatColor.RED + " (6.5 Base Damage)"
                        + ChatColor.GRAY + " [Amethyst + Diamond Sword + Clock]",
                ChatColor.GOLD + "Earthshatter Greatmaul (Craft):" + ChatColor.GREEN
                                + " 14 Damage, vanilla mace falling smash, AoE knockback"
                                + ChatColor.GRAY + " [4 Crying Obsidian + Netherite Ingot + 2 Breeze Rods]"
                                + ChatColor.RED + " (-15% Speed)",
                        ChatColor.RED + "Cinderwake Mace (Craft):" + ChatColor.GREEN
                                + " 7 Damage, vanilla mace falling smash, ignites nearby enemies"
                                + ChatColor.GRAY + " [2 Breeze Rods + 2 Magma Creams + Heavy Core]",
                        ChatColor.DARK_AQUA + "Wardenfall Mace (Custom Boss Drop):" + ChatColor.GREEN
                                + " Guaranteed Goliath drop; 14 Damage, vanilla mace falling smash, 6-damage quake + 2 HP heal.",
                ChatColor.DARK_GREEN + "Venomfang Stiletto (Structure):" + ChatColor.GREEN
                        + " Poison II + Wither I on hit" + ChatColor.RED + " (-2 Max HP)",
                ChatColor.LIGHT_PURPLE + "Starfall Longbow (Structure):" + ChatColor.GREEN
                        + " Summons lightning on full draw" + ChatColor.RED + " (-10% Speed)");

        add("relics_tools", Category.RELICS, ChatColor.YELLOW + "Relic Super-Tools (5 Tools)",
                Material.NETHERITE_PICKAXE,
                List.of("veinminer", "worldcore", "treecapitator", "timberlord", "molten", "shovel", "excavator",
                        "harvester", "gravity", "hammer", "terraformer"),
                ChatColor.GOLD + "Worldcore Vein-Breaker (Craft):" + ChatColor.GREEN
                        + " 3x3 Tunnel Mining, +2 Reach" + ChatColor.RED + " (Drains 1 Hunger per 3x3)",
                ChatColor.GREEN + "Timberlord Felling Axe (Craft):" + ChatColor.GREEN
                        + " Fells 32 vertical logs instantly, +25% Speed",
                ChatColor.GREEN + "Verdant Bountiful Hoe (Craft):" + ChatColor.GREEN
                        + " 3x3 Auto-Replant Harvest" + ChatColor.RED + " (-20% Attack Damage)",
                ChatColor.RED + "Magma-Core Excavator (Structure):" + ChatColor.GREEN
                        + " Auto-smelts Sand to Glass and Clay to Terracotta" + ChatColor.RED + " (-15% Speed)",
                ChatColor.AQUA + "Astral Terraformer (Custom Boss Drop):" + ChatColor.GREEN
                        + " +40% Break Speed, +3 Reach, Haste II" + ChatColor.RED + " (-4 Armor)");

        add("relics_offhand", Category.RELICS, ChatColor.LIGHT_PURPLE + "Off-Hand Relic Artifacts (10 Artifacts)",
                Material.TOTEM_OF_UNDYING,
                List.of("offhand", "off-hand", "artifact", "artifacts", "heart", "abyss", "blood", "chalice",
                        "hourglass", "aegis", "prism", "ember", "totem", "idol", "riftwalker", "rift",
                        "stormcaller", "storm", "frostbound", "frost", "soulbloom", "pendant", "bastion"),
                ChatColor.DARK_PURPLE + "Heart of the Abyss (Custom Boss Drop):" + ChatColor.GREEN
                        + " +8 Max HP, Regen I" + ChatColor.RED + " (-20% Move Speed)",
                ChatColor.DARK_RED + "Crimson Blood Chalice (Structure):" + ChatColor.GREEN
                        + " +30% Attack Damage, 1.5 HP Lifesteal" + ChatColor.RED + " (-6 HP, +15% Damage Taken)",
                ChatColor.AQUA + "Hourglass of Chronos (Craft):" + ChatColor.GREEN
                        + " +30% Speed, +25% Attack Speed" + ChatColor.RED + " (Hunger II)"
                        + ChatColor.GRAY + " [Gold Block + Clock + Pearl + Amethyst]",
                ChatColor.GOLD + "Aegis Bastion Prism (Craft):" + ChatColor.GREEN
                        + " +6 Armor, +4 Toughness, Resistance I"
                        + ChatColor.RED + " (-30% Damage, -15% Speed)",
                ChatColor.YELLOW + "Ignis Phoenix Idol (Structure):" + ChatColor.GREEN
                        + " Fire Immunity, ignites attackers" + ChatColor.RED + " (Slow/Weak in rain or water)",
                ChatColor.LIGHT_PURPLE + "Riftwalker Charm (Craft):" + ChatColor.GREEN
                        + " +10% Speed; right-click to dash forward (15s cooldown)"
                        + ChatColor.GRAY + " [4 Ender Pearls + 4 Phantom Membranes + Echo Shard]",
                ChatColor.BLUE + "Stormcaller Totem (Custom Boss Drop):" + ChatColor.GREEN
                        + " Right-click to strike up to 4 nearby enemies for 6 damage (25s cooldown)"
                        + ChatColor.RED + " (-10% Speed)",
                ChatColor.AQUA + "Frostbound Idol (Structure Loot):" + ChatColor.GREEN
                        + " +2 Armor; right-click to freeze and slow nearby enemies (20s cooldown)"
                        + ChatColor.RED + " (-10% Attack Speed)",
                ChatColor.GREEN + "Soulbloom Pendant (Craft):" + ChatColor.GREEN
                        + " +4 Max HP; right-click to heal 6 HP and cleanse ailments (30s cooldown)"
                        + ChatColor.GRAY + " [4 Echo Shards + 4 Amethyst Shards + Golden Apple]",
                ChatColor.GOLD + "Bastion Heart (Custom Boss Drop):" + ChatColor.GREEN
                        + " +4 Armor, +2 Toughness; right-click for Absorption II and Resistance I (40s cooldown)"
                        + ChatColor.RED + " (-15% Speed)");

        add("early_game_gear_recipes", Category.CUSTOM_GEAR,
                ChatColor.GREEN + "Starter Gear Recipes (11)", Material.COPPER_INGOT,
                List.of("recipe", "recipes", "craft", "craftable", "starter", "early", "trailblazer",
                        "ironbound", "copperbound", "shortsword", "hatchet", "bow"),
                ChatColor.GREEN + "Trailblazer armor:" + ChatColor.WHITE
                        + " Hood 3 Leather + 2 Copper; Tunic 6 Leather + 1 Copper; Leggings 4 Leather + 2 Copper; Boots 2 Leather + 2 Copper. Full set: +10% speed.",
                ChatColor.GRAY + "Ironbound armor:" + ChatColor.WHITE
                        + " Helmet 3 Iron + 2 Copper; Chestplate 6 Iron + 1 Copper; Leggings 4 Iron + 2 Copper; Boots 2 Iron + 2 Copper. Full set: +1 Heart.",
                ChatColor.GOLD + "Weapons:" + ChatColor.WHITE
                        + " Copperbound Shortsword: 2 Copper + Stick (6 damage); Ironbound Hatchet: 3 Iron + 2 Sticks + Copper (10 damage); Trailblazer Bow: 3 Sticks + 2 Copper + 2 String (Power I).",
                ChatColor.AQUA + "Use /recipe or right-click the Artisan's Recipe Tome to view each 3x3 layout.");

        add("skybound_bow", Category.CUSTOM_GEAR, ChatColor.AQUA + "Skybound Bow", Material.BOW,
                List.of("skybound", "skybound bow", "levitation", "levitation bow", "custom bow"),
                ChatColor.AQUA + "Effect:" + ChatColor.WHITE
                        + " Arrows that hit a living target apply Levitation I for 2 seconds.",
                ChatColor.GOLD + "Craft:" + ChatColor.WHITE
                        + " 1 Tempest Essence, 1 Breeze Rod, 2 Phantom Membranes, and 3 String. Use /recipe to view the layout.");

        add("boomerang_disk", Category.CUSTOM_GEAR, ChatColor.AQUA + "Returning Disk",
                Material.MUSIC_DISC_PIGSTEP,
                List.of("disk", "disc", "boomerang", "throwing disk", "returning disk"),
                ChatColor.AQUA + "Throw:" + ChatColor.WHITE
                        + " Right-click to throw the disk up to 16 blocks. It deals 8 damage, ricochets off blocks and targets, "
                        + "and returns to your inventory.",
                ChatColor.GREEN + "Craft:" + ChatColor.WHITE
                        + " 1 Diamond surrounded by 4 Iron Ingots. Use /recipe for the layout.",
                ChatColor.LIGHT_PURPLE + "Enchanting:" + ChatColor.WHITE
                        + " Custom weapon enchants and Sharpness, Smite, Bane of Arthropods, Knockback, Fire Aspect, "
                        + "Looting, and Curse of Vanishing can be applied with books in an anvil.");

        add("everlasting_baked_potato", Category.RELICS, ChatColor.GOLD + "Everlasting Baked Potato",
                Material.BAKED_POTATO,
                List.of("everlasting", "baked potato", "potato", "food", "troll", "recipe"),
                ChatColor.GOLD + "Craft:" + ChatColor.WHITE
                        + " Surround 1 Poisonous Potato with 8 Baked Potatoes.",
                ChatColor.AQUA + "Find it in vanilla and custom structure loot chests.",
                ChatColor.GRAY + "A special food item that restores hunger when eaten.");

        add("armor_sets", Category.CUSTOM_GEAR, ChatColor.GOLD + "4 Full Boss Armor Sets (/recipes)",
                Material.NETHERITE_CHESTPLATE,
                List.of("juggernaut", "zephyr", "gale", "pyromancer", "pyro", "shadowstalker", "shadow", "set",
                        "sets", "armor"),
                ChatColor.GOLD + "Juggernaut Set (Titan Heartstone):" + ChatColor.WHITE
                        + " Full set = +8 Hearts, Resistance I, Knockback Immune (-40% Speed).",
                ChatColor.AQUA + "Zephyr Gale Set (Tempest Essence):" + ChatColor.WHITE
                        + " Full set = +52% Speed, Mid-Air Double Jump, No Fall Damage (-4 Hearts).",
                ChatColor.RED + "Pyromancer Set (Infernal Core):" + ChatColor.WHITE
                        + " Full set = +3 Hearts, +4 Damage, Fire Immunity and Igniting Strikes.",
                ChatColor.DARK_PURPLE + "Shadowstalker Set (Abyssal Soul Shard):" + ChatColor.WHITE
                        + " Full set = Sneak Invisibility and +50% Ambush Crit Damage.");

        add("boss_weapons_health", Category.CUSTOM_GEAR, ChatColor.AQUA + "Boss Weapons, Tools & Health Items",
                Material.APPLE,
                List.of("executioner", "valkyrie", "spear", "skyhook", "grapple", "rod", "soulreaver", "phantom",
                        "earthshaker", "heart", "crystal", "elixir", "bandage", "health"),
                ChatColor.RED + "Executioner's Greataxe:" + ChatColor.WHITE
                        + " +35% damage vs. enemies below 50% HP.",
                ChatColor.AQUA + "Valkyrie Storm Spear & Skyhook Rod:" + ChatColor.WHITE
                        + " Gale dash (4s cooldown) and grappling hook mobility.",
                ChatColor.LIGHT_PURPLE + "Soulreaver Scythe & Phantom Rapier:" + ChatColor.WHITE
                        + " AoE cleave lifesteal and 6-block shadow-step blink.",
                ChatColor.GREEN + "Health Items:" + ChatColor.WHITE
                        + " Crimson Heart Crystal (+1 permanent heart up to +10), Vitality Elixir, Field Medic Bandage.");

        add("world_bosses", Category.BOSSES, ChatColor.RED + "4 Summonable World Bosses",
                Material.DRAGON_HEAD,
                List.of("boss", "bosses", "ignis", "boreas", "malakor", "goliath", "summon", "crystal", "crystals"),
                ChatColor.RED + "1. Ignis (320 HP):" + ChatColor.WHITE
                        + " Drops Infernal Cores. Crystal: 4 Blaze Powder + 4 Magma Cream + Eye of Ender.",
                ChatColor.AQUA + "2. Boreas (280 HP):" + ChatColor.WHITE
                        + " Drops Tempest Essence. Crystal: 4 Breeze Rods + 4 Phantom Membranes + Eye of Ender.",
                ChatColor.DARK_PURPLE + "3. Malakor (300 HP):" + ChatColor.WHITE
                        + " Drops Abyssal Soul Shards. Crystal: 4 Ender Pearls + 4 Amethyst Shards + Eye of Ender.",
                ChatColor.GOLD + "4. Goliath (420 HP):" + ChatColor.WHITE
                        + " Drops Titan Heartstones. Crystal: 4 Obsidian + 4 Iron Blocks + Eye of Ender.",
                ChatColor.GRAY + "Vanilla bosses (Ender Dragon, Wither, Warden, Elder Guardian) also drop Mythic Relics.");

        add("elite_mobs", Category.BOSSES, ChatColor.YELLOW + "4 Natural Elite Mobs", Material.ZOMBIE_HEAD,
                List.of("elite", "elites", "cinder", "knight", "gale", "stalker", "void", "acolyte", "obsidian",
                        "sentinel"),
                ChatColor.WHITE + "Elite mobs spawn naturally (10% chance alongside night/cave monsters):",
                ChatColor.RED + "• Cinder Knight" + ChatColor.GRAY + " -> Drops Infernal Cores",
                ChatColor.AQUA + "• Gale Stalker" + ChatColor.GRAY + " -> Drops Tempest Essence",
                ChatColor.DARK_PURPLE + "• Void Acolyte" + ChatColor.GRAY + " -> Drops Abyssal Soul Shards",
                ChatColor.DARK_GRAY + "• Obsidian Sentinel" + ChatColor.GRAY + " -> Drops Titan Heartstones");

        add("enchants_tools", Category.ENCHANTS, ChatColor.GOLD + "Tool & Utility Custom Enchantments",
                Material.DIAMOND_PICKAXE,
                List.of("enchant", "enchants", "enchantment", "grapple", "veinminer", "autosmelt", "auto-smelt",
                        "telekinesis", "timber", "excavate", "replenish", "experience", "recycler", "prospector",
                        "archaeologist", "horticulturist", "deep miner", "angler", "mariner"),
                ChatColor.AQUA + "Grapple (Fishing Rod):" + ChatColor.WHITE
                        + " Launches you toward your hooked bobber with fall immunity.",
                ChatColor.GOLD + "Veinminer / Timber / Excavate:" + ChatColor.WHITE
                        + " Mines ore veins, fells trees, or digs 3x3 areas (sneak to disable).",
                ChatColor.RED + "Auto-Smelt & Telekinesis:" + ChatColor.WHITE
                        + " Smelts mined ores automatically and teleports drops into your inventory.",
                ChatColor.GREEN + "Replenish & Experience:" + ChatColor.WHITE
                        + " Auto-replants crops using seeds and multiplies XP drops.",
                ChatColor.YELLOW + "New utility enchants:" + ChatColor.WHITE
                        + " Recycler sometimes saves tool durability; Prospector, Archaeologist, and Horticulturist can yield one extra drop. Deep Miner grants modest Haste underground; Angler and Mariner improve fishing rewards.");

        add("enchants_combat_armor", Category.ENCHANTS,
                ChatColor.LIGHT_PURPLE + "Combat & Armor Custom Enchantments", Material.ENCHANTED_BOOK,
                List.of("lifesteal", "thunderstrike", "frostbite", "poison", "explosive", "arrows", "glowing",
                        "saturation", "molten", "hardened", "gears", "springs", "lava", "walker", "soulbound",
                        "hunter", "duelist", "finisher", "backstab", "crippling", "stagger", "longshot", "pinning",
                        "retrieval", "fireguard", "blastguard", "projectile ward", "featherfall", "frostguard",
                        "bulwark", "second wind", "swift swim", "gills", "tidecaller", "scavenger", "voidproof",
                        "aegis"),
                ChatColor.DARK_RED + "Weapons:" + ChatColor.WHITE
                        + " Lifesteal heals on hit; Thunderstrike calls lightning; Frostbite slows; Poison Aspect and Explosive Arrows affect enemies. Hunter, Duelist, Finisher, and Backstab add small situational bonuses.",
                ChatColor.AQUA + "Armor:" + ChatColor.WHITE
                        + " Glowing (Night Vision), Saturation (auto-feed), Molten (ignites attackers), Hardened (saves durability), Gears (Speed), Springs (Jump), Lava Walker. Defensive enchants give modest reductions; Second Wind has a two-minute cooldown.",
                ChatColor.LIGHT_PURPLE + "Soulbound:" + ChatColor.WHITE
                        + " Keeps one Soulbound item on death. Curse of Vanishing overrides it; /ce list shows every enchant and its exact description.",
                ChatColor.GRAY + "Obtain through enchanting, librarian villagers, structure chests, or bosses. Combine in anvils!");

        add("structures_all", Category.STRUCTURES, ChatColor.GREEN + "18 Naturally Generating World Structures",
                Material.CHEST,
                List.of("structure", "structures", "ruin", "ruins", "obelisk", "forge", "shrine", "crypt",
                        "watchtower", "cairn", "loot", "nether", "end", "bastion", "fortress", "end city",
                        "unbreaking", "mending", "ancient debris"),
                ChatColor.GOLD + "Relic Ruins (Overworld):" + ChatColor.WHITE
                        + " Sunken Obelisk, Forgotten Forge, Astral Shrine, Crypt Sanctum (may contain structure relics).",
                ChatColor.AQUA + "Biome Structures:" + ChatColor.WHITE
                        + " Bandit Watchtower (Plains/Forest), Ancient Mossy Shrine (Swamp/Jungle), Buried Sun Obelisk (Desert/Badlands), Frozen Cairn (Snowy/Peaks), Shoreline Ruin (Beaches).",
                ChatColor.RED + "Nether Ruins:" + ChatColor.WHITE
                        + " Ashen Watchtower, Crimson Cache, Basalt Forge, Soulfire Shrine, Gilded Bastion Vault, Wither Reliquary.",
                ChatColor.LIGHT_PURPLE + "End Ruins:" + ChatColor.WHITE
                        + " Voidspire Observatory, Chorus Observatory, Dragon's Cache.",
                ChatColor.GOLD + "New Nether/End chests contain a custom relic and powerful vanilla gear/books. Netherite gear and Elytra may have Unbreaking IV and Mending II.",
                ChatColor.RED + "Ancient Debris:" + ChatColor.WHITE
                        + " A datapack doubles generation attempts in newly generated Nether chunks.",
                ChatColor.GRAY + "Vanilla Bastion, Nether Fortress, Ruined Portal, and End City loot may also include an Unbreaking IV + Mending II book. Explore newly generated chunks.");

        add("economy_guide", Category.ECONOMY_SKILLS, ChatColor.GREEN + "Dynamic Supply & Demand Economy",
                Material.GOLD_INGOT,
                List.of("eco", "economy", "money", "bal", "balance", "pay", "market", "buy", "sell", "price",
                        "prices"),
                ChatColor.YELLOW + "How it works:" + ChatColor.WHITE
                        + " Item prices rise when players buy heavily and fall when players sell in quantity.",
                ChatColor.AQUA + "/bal [player]" + ChatColor.WHITE + " - Check your balance (starts at $500).",
                ChatColor.AQUA + "/pay <player> <amount>" + ChatColor.WHITE + " - Send money to another player.",
                ChatColor.AQUA + "/market <price|buy|sell> <item> [amount]" + ChatColor.WHITE
                        + " - Trade Diamond, Iron, Gold, Emerald, Oak Log, or Cooked Beef.");

        add("skills_bounties_guide", Category.ECONOMY_SKILLS,
                ChatColor.DARK_RED + "Skill Trees, Bounties & Black Market",
                Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                List.of("skill", "skills", "tree", "bounty", "bounties", "blackmarket", "shop", "points"),
                ChatColor.AQUA + "/skills" + ChatColor.WHITE
                        + " - Open the Skill Tree GUI to unlock permanent combat, mining, and agility upgrades.",
                ChatColor.RED + "/bounty" + ChatColor.WHITE
                        + " - Place cash bounties on rivals or claim rewards by eliminating targets.",
                ChatColor.DARK_PURPLE + "/blackmarket" + ChatColor.WHITE
                        + " - Spend currency on rare contraband gear and upgrades.");
    }

    public List<GuideEntry> getAllEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<GuideEntry> getByCategory(Category category) {
        return entries.stream().filter(entry -> entry.category() == category).collect(Collectors.toList());
    }

    public List<GuideEntry> search(String query) {
        String clean = query.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s-]", " ");
        String[] tokens = clean.split("\\s+");

        Map<GuideEntry, Integer> scores = new HashMap<>();
        for (GuideEntry entry : entries) {
            int score = 0;
            String title = ChatColor.stripColor(entry.title()).toLowerCase(Locale.ROOT);
            String details = entry.details().stream()
                    .map(ChatColor::stripColor)
                    .collect(Collectors.joining(" "))
                    .toLowerCase(Locale.ROOT);

            for (String token : tokens) {
                if (token.length() < 2) {
                    continue;
                }
                if (entry.keywords().contains(token)) {
                    score += 10;
                }
                if (title.contains(token)) {
                    score += 6;
                }
                if (details.contains(token)) {
                    score += 2;
                }
            }
            if (score > 0) {
                scores.put(entry, score);
            }
        }

        return scores.entrySet().stream()
                .sorted((first, second) -> Integer.compare(second.getValue(), first.getValue()))
                .map(Map.Entry::getKey)
                .limit(3)
                .collect(Collectors.toList());
    }
}
