package com.eagler.gear;

import org.bukkit.Material;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GearListenerTest {

    @Test
    public void soulreaverScytheIsHoeAndSharpnessCapsAtFive() {
        assertEquals(Material.NETHERITE_HOE, CustomGearType.SOULREAVER_SCYTHE.getMaterial());
        assertEquals(5, GearListener.mergeScytheSharpnessLevel(0, 5));
        assertEquals(5, GearListener.mergeScytheSharpnessLevel(4, 4));
        assertEquals(5, GearListener.mergeScytheSharpnessLevel(5, 5));
        assertEquals(3, GearListener.mergeScytheSharpnessLevel(3, 2));
    }

    @Test
    public void customWeaponDamageIncludesVanillaMaterialDamage() {
        assertEquals(2.0, GearManager.getAttackDamageModifier(CustomGearType.EXECUTIONER_GREATAXE), 0.001);
        assertEquals(1.0, GearManager.getAttackDamageModifier(CustomGearType.VALKYRIE_SPEAR), 0.001);
        assertEquals(9.5, GearManager.getAttackDamageModifier(CustomGearType.SOULREAVER_SCYTHE), 0.001);
        assertEquals(2.5, GearManager.getAttackDamageModifier(CustomGearType.PHANTOM_RAPIER), 0.001);
        assertEquals(1.0, GearManager.getAttackDamageModifier(CustomGearType.EARTHSHAKER_PICKAXE), 0.001);
    }

    @Test
    public void earlyGameMeleeWeaponsAddOnlyTheirIntendedDamage() {
        assertEquals(1.0, GearManager.getAttackDamageModifier(CustomGearType.COPPER_SHORTSWORD), 0.001);
        assertEquals(1.0, GearManager.getAttackDamageModifier(CustomGearType.IRONBOUND_HATCHET), 0.001);
    }

    @Test
    public void returningDiskHasOneSemiStrongDamageValue() {
        assertEquals(8.0, GearListener.getBoomerangDamage(CustomGearType.BOOMERANG_DISK), 0.001);
        assertEquals(Material.MUSIC_DISC_PIGSTEP, CustomGearType.BOOMERANG_DISK.getMaterial());
    }

    @Test
    public void zephyrDoubleJumpIsGrantedByTheBootsAlone() {
        assertTrue(GearManager.grantsZephyrDoubleJump(CustomGearType.ZEPHYR_BOOTS));
        assertFalse(GearManager.grantsZephyrDoubleJump(CustomGearType.ZEPHYR_HOOD));
        assertFalse(GearManager.grantsZephyrDoubleJump(CustomGearType.TRAILBLAZER_BOOTS));
    }
}
