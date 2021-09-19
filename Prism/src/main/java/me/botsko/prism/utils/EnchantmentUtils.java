package me.botsko.prism.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EnchantmentUtils {

    /**
     * Given an enchantment, does the current item have any that conflict.
     *
     * @return bool
     */
    @SuppressWarnings("unused")
    public static boolean hasConflictingEnchanment(ItemStack item, Enchantment ench) {
        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        boolean conflict = false;
        for (Enchantment e : enchantments.keySet()) {
            if (ench.conflictsWith(e)) {
                conflict = true;
            }
        }
        return (enchantments.containsKey(ench) || conflict);
    }

    /**
     * Return the enchantment based on a common name.
     *
     * @param name String
     * @return Enchantment
     */
    @SuppressWarnings("unused")
    public static Enchantment getEnchantmentFromCommonName(String name) {
        switch (name.toLowerCase()) {
            case "aquaaffinity":
                return Enchantment.WATER_WORKER;
            case "bane":
                return Enchantment.DAMAGE_ARTHROPODS;
            case "efficiency":
                return Enchantment.DIG_SPEED;
            case "explosion":
                return Enchantment.PROTECTION_EXPLOSIONS;
            case "fall":
                return Enchantment.PROTECTION_FALL;
            case "fire":
                return Enchantment.PROTECTION_FIRE;
            case "fireaspect":
                return Enchantment.FIRE_ASPECT;
            case "flame":
                return Enchantment.ARROW_FIRE;
            case "fortune":
                return Enchantment.LOOT_BONUS_BLOCKS;
            case "infinity":
                return Enchantment.ARROW_INFINITE;
            case "knockback":
                return Enchantment.KNOCKBACK;
            case "power":
                return Enchantment.ARROW_DAMAGE;
            case "looting":
                return Enchantment.LOOT_BONUS_MOBS;
            case "projectile":
                return Enchantment.PROTECTION_PROJECTILE;
            case "protection":
                return Enchantment.PROTECTION_ENVIRONMENTAL;
            case "punch":
                return Enchantment.ARROW_KNOCKBACK;
            case "respiration":
                return Enchantment.OXYGEN;
            case "sharpness":
                return Enchantment.DAMAGE_ALL;
            case "silktouch":
                return Enchantment.SILK_TOUCH;
            case "smite":
                return Enchantment.DAMAGE_UNDEAD;
            case "unbreaking":
                return Enchantment.DURABILITY;
            default:
                String formattedName = name.replace(' ','_');
                NamespacedKey key = NamespacedKey.minecraft(formattedName);
                return Enchantment.getByKey(key);
        }
    }

    /**
     * Return the common name for an enchantment.
     *
     * @param enchantment Keyed
     * @param level int
     * @return String
     */
    public static String getClientSideEnchantmentName(Enchantment enchantment, int level) {

        String enchantName;

        if (enchantment.equals(Enchantment.ARROW_DAMAGE)) {
            enchantName = "power";
        } else if (enchantment.equals(Enchantment.ARROW_FIRE)) {
            enchantName = "flame";
        } else if (enchantment.equals(Enchantment.ARROW_INFINITE)) {
            enchantName = "infinity";
        } else if (enchantment.equals(Enchantment.ARROW_KNOCKBACK)) {
            enchantName = "punch";
        } else if (enchantment.equals(Enchantment.DAMAGE_ALL)) {
            enchantName = "sharpness";
        } else if (enchantment.equals(Enchantment.DAMAGE_ARTHROPODS)) {
            enchantName = "bane of anthropods";
        } else if (enchantment.equals(Enchantment.DAMAGE_UNDEAD)) {
            enchantName = "damage undead";
        } else if (enchantment.equals(Enchantment.DIG_SPEED)) {
            enchantName = "efficiency";
        } else if (enchantment.equals(Enchantment.DURABILITY)) {
            enchantName = "unbreaking";
        } else if (enchantment.equals(Enchantment.LOOT_BONUS_BLOCKS)) {
            enchantName = "fortune";
        } else if (enchantment.equals(Enchantment.LOOT_BONUS_MOBS)) {
            enchantName = "looting";
        } else if (enchantment.equals(Enchantment.OXYGEN)) {
            enchantName = "respiration";
        } else if (enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL)) {
            enchantName = "protection";
        } else if (enchantment.equals(Enchantment.PROTECTION_EXPLOSIONS)) {
            enchantName = "blast protection";
        } else if (enchantment.equals(Enchantment.PROTECTION_FALL)) {
            enchantName = "feather falling";
        } else if (enchantment.equals(Enchantment.PROTECTION_FIRE)) {
            enchantName = "fire protection";
        } else if (enchantment.equals(Enchantment.PROTECTION_PROJECTILE)) {
            enchantName = "projectile protection";
        } else if (enchantment.equals(Enchantment.WATER_WORKER)) {
            enchantName = "aqua affinity";
        } else if (enchantment.equals(Enchantment.VANISHING_CURSE)) {
            enchantName = "vanishing curse";
        } else {
            // can leave as-is: SILK_TOUCH, FIRE_ASPECT, KNOCKBACK, THORNS, LUCK, LURE
            enchantName = enchantment.getKey().getKey().toLowerCase().replace("_", " ");
        }
        switch (level) {
            case 1:
                enchantName += " I";
                break;
            case 2:
                enchantName += " II";
                break;
            case 3:
                enchantName += " III";
                break;
            case 4:
                enchantName += " IV";
                break;
            case 5:
                enchantName += " V";
                break;
            default:
                enchantName += " " + level;
        }
        return enchantName;

    }
}
