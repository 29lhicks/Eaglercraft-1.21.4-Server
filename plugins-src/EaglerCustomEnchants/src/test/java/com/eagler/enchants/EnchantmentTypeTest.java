package com.eagler.enchants;

import org.bukkit.Material;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnchantmentTypeTest {

    @Test
    public void meleeCustomEnchantmentsApplyToTheSoulreaverScythe() {
        for (EnchantmentType type : EnchantmentType.values()) {
            if (type.getTargetGroup() == EnchantmentType.TargetGroup.MELEE_WEAPON) {
                assertTrue(type.getKey() + " should apply to the scythe",
                        type.canApplyTo(Material.NETHERITE_HOE, null));
            }
        }

        assertFalse(EnchantmentType.GRAPPLE.canApplyTo(Material.NETHERITE_HOE, null));
    }

    @Test
    public void newEnchantmentsRespectTheirEquipmentTargets() {
        assertTrue(EnchantmentType.SOULBOUND.canApplyTo(Material.DIAMOND_SWORD, null));
        assertTrue(EnchantmentType.SOULBOUND.canApplyTo(Material.DIAMOND_CHESTPLATE, null));
        assertFalse(EnchantmentType.SOULBOUND.canApplyTo(Material.BAKED_POTATO, null));

        assertTrue(EnchantmentType.AEGIS.canApplyTo(Material.SHIELD, null));
        assertFalse(EnchantmentType.AEGIS.canApplyTo(Material.DIAMOND_SWORD, null));
        assertTrue(EnchantmentType.PROSPECTOR.canApplyTo(Material.DIAMOND_PICKAXE, null));
        assertFalse(EnchantmentType.PROSPECTOR.canApplyTo(Material.DIAMOND_AXE, null));
        assertTrue(EnchantmentType.TIDECALLER.canApplyTo(Material.TRIDENT, null));
    }

    @Test
    public void weaponEnchantsApplyOnlyToTheCustomBoomerangDisk() {
        assertTrue(EnchantmentType.LIFESTEAL.canApplyTo(Material.MUSIC_DISC_PIGSTEP, "boomerang_disk"));
        assertTrue(EnchantmentType.LONGSHOT.canApplyTo(Material.MUSIC_DISC_PIGSTEP, "boomerang_disk"));
        assertTrue(EnchantmentType.TIDECALLER.canApplyTo(Material.MUSIC_DISC_PIGSTEP, "boomerang_disk"));
        assertTrue(EnchantmentType.SOULBOUND.canApplyTo(Material.MUSIC_DISC_PIGSTEP, "boomerang_disk"));
        assertFalse(EnchantmentType.EXPLOSIVE_ARROWS.canApplyTo(Material.MUSIC_DISC_PIGSTEP, "boomerang_disk"));
        assertFalse(EnchantmentType.LIFESTEAL.canApplyTo(Material.MUSIC_DISC_PIGSTEP, null));
        assertFalse(EnchantmentType.LIFESTEAL.canApplyTo(Material.DIRT, "boomerang_disk"));
    }
}
