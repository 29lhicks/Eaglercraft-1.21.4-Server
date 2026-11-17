package com.customrealms.relics.structures;

import com.customrealms.relics.items.CustomItemRegistry;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.concurrent.ThreadLocalRandom;

public class VanillaStructureLootListener implements Listener {
    private final CustomItemRegistry registry;

    public VanillaStructureLootListener(CustomItemRegistry registry) {
        this.registry = registry;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLootGenerate(LootGenerateEvent event) {
        var key = event.getLootTable().getKey();
        if (!"minecraft".equals(key.getNamespace())) return;

        String lootTable = key.getKey();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (lootTable.startsWith("chests/") && random.nextDouble() < 0.10) {
            ItemStack potato = registry.createItem(CustomItemRegistry.EVERLASTING_BAKED_POTATO_ID);
            if (potato != null) event.getLoot().add(potato);
        }
        if (isTargetStructureLoot(event.getWorld(), lootTable) && random.nextDouble() < 0.40) {
            event.getLoot().add(createOverpoweredBook());
        }
    }

    private boolean isTargetStructureLoot(World world, String lootTable) {
        if (world.getEnvironment() == World.Environment.NETHER) {
            return lootTable.equals("chests/nether_bridge")
                    || lootTable.equals("chests/ruined_portal")
                    || lootTable.startsWith("chests/bastion_");
        }
        return world.getEnvironment() == World.Environment.THE_END
                && lootTable.equals("chests/end_city_treasure");
    }

    private ItemStack createOverpoweredBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        if (book.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            meta.addStoredEnchant(Enchantment.UNBREAKING, 4, true);
            meta.addStoredEnchant(Enchantment.MENDING, 2, true);
            book.setItemMeta(meta);
        }
        return book;
    }
}
