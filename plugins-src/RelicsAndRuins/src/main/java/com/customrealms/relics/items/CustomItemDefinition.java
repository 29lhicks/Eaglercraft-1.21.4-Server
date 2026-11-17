package com.customrealms.relics.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CustomItemDefinition {
    public enum Category { ARMOR, WEAPON, TOOL, ARTIFACT }
    public enum Source { CRAFTABLE, BOSS_DROP, STRUCTURE_LOOT }

    private final String id;
    private final String displayName;
    private final NamedTextColor nameColor;
    private final Material material;
    private final Category category;
    private final Source source;
    private final int customModelData;
    private final List<String> buffLore;
    private final List<String> nerfLore;
    private final List<AttrEntry> attributes = new ArrayList<>();

    public record AttrEntry(Attribute attribute, double amount, AttributeModifier.Operation op, EquipmentSlotGroup slot) {}

    public CustomItemDefinition(String id, String displayName, NamedTextColor nameColor, Material material,
                                Category category, Source source, int customModelData,
                                List<String> buffLore, List<String> nerfLore) {
        this.id = id;
        this.displayName = displayName;
        this.nameColor = nameColor;
        this.material = material;
        this.category = category;
        this.source = source;
        this.customModelData = customModelData;
        this.buffLore = buffLore;
        this.nerfLore = nerfLore;
    }

    public CustomItemDefinition addAttr(Attribute attr, double amount, AttributeModifier.Operation op, EquipmentSlotGroup slot) {
        attributes.add(new AttrEntry(attr, amount, op, slot));
        return this;
    }

    public ItemStack build(NamespacedKey idKey) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName, nameColor).decoration(TextDecoration.ITALIC, false));
            meta.setCustomModelData(customModelData);
            meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("[" + category.name() + " | " + source.name().replace("_", " ") + "]", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            for (String b : buffLore) {
                lore.add(Component.text("+ " + b, NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            }
            for (String n : nerfLore) {
                lore.add(Component.text("- " + n, NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            }
            lore.add(Component.empty());
            lore.add(Component.text("Compatible with Vanilla & Custom Enchants", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            int idx = 0;
            for (AttrEntry entry : attributes) {
                NamespacedKey modKey = new NamespacedKey("relicsandruins", id + "_" + (idx++));
                AttributeModifier modifier = new AttributeModifier(modKey, entry.amount(), entry.op(), entry.slot());
                meta.addAttributeModifier(entry.attribute(), modifier);
            }

            stack.setItemMeta(meta);
        }
        return stack;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Category getCategory() { return category; }
    public Source getSource() { return source; }
}
