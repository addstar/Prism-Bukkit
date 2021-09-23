package me.botsko.prism.utils;

import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.api.objects.MaterialState;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class ItemUtils {

    private static final EnumSet<Material> badWands = EnumSet.of(Material.WATER, Material.LAVA, Material.FIRE,
          Material.FLINT_AND_STEEL, Material.NETHER_PORTAL, Material.END_PORTAL);

    public static boolean isBadWand(Material material) {
        return badWands.contains(material);
    }

    /**
     * Returns name as lowercase and adds a durability to the end.
     *
     * @param stack ItemStack
     * @return String
     */
    public static String smallString(ItemStack stack) {
        if (stack != null) {
            String result = stack.getType().name().toLowerCase();

            short durability = (short) getItemDamage(stack);
            if (durability > 0) {
                result += ":" + durability;
            }
            return result;
        }
        return null;
    }

    /**
     * Get the amount an item is damaged or 0.
     *
     * @param stack ItemStack
     * @return int
     */
    public static int getItemDamage(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();

        if (meta instanceof Damageable d) {

            return d.getDamage();
        }

        return 0;
    }

    /**
     * Converts a lowercase string into an item  formatted as
     * 'materialname[:itemdamage]'.
     *
     * @param smallString String
     * @return ItemStack
     */
    public static ItemStack itemOf(String smallString) {
        if (smallString != null) {
            String[] parts = smallString.split(":", 2);
            Material mat = Material.matchMaterial(parts[0].toUpperCase());

            if (mat != null) {
                if (parts.length > 1) {
                    try {
                        ItemStack stack = new ItemStack(mat, 1);
                        MaterialState.setItemDamage(stack, Short.parseShort(parts[1]));

                        return stack;
                    } catch (NumberFormatException e) {
                        PrismLogHandler.debug(" Item could not have damage parsed. Data:" + smallString + " Error:"
                              + e.getMessage());
                    }
                }
                return new ItemStack(mat, 1);
            }
        }
        return null;
    }

    /**
     * Check is a valid item ( not null or air).
     *
     * @param item ItemStack
     * @return bool
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean isValidItem(ItemStack item) {
        return (item != null && !item.getType().equals(Material.AIR));
    }

    /**
     * Check ItemStack are the same Type.
     *
     * @param a ItemStack
     * @param b ItemStack
     * @return bool
     */
    public static boolean isSameType(ItemStack a, ItemStack b) {
        return a.getType().equals(b.getType());
    }

    /**
     * Check if an ItemStack is equal.
     * 1. Type
     * 2. Display name
     * 3. If leather check the colour.
     * 4. Lore
     * 5. Enchants.
     * 6. SkullMeta is checked if appropriate.
     * 7. Bookmeta - title , page count and author
     * 8. PotionMeta - checked
     * 9 FireworkMeta including effect meta.
     *
     * @param a Itemstack
     * @param b ItemStack
     * @return bool
     */
    public static boolean equals(ItemStack a, ItemStack b) {

        ItemMeta metaA = a.getItemMeta();
        ItemMeta metaB = b.getItemMeta();

        // Type/dura
        if (!isSameType(a, b)) {
            return false;
        }
        if (metaA == null && metaB == null) {
            return true;
        } else if (metaA == null || metaB == null) {
            return false;
        }
        if (!metaA.getDisplayName().equals(metaB.getDisplayName())) {
            return false;
        }

        // Coloring
        if (metaA instanceof LeatherArmorMeta colorA) {
            if (!(metaB instanceof LeatherArmorMeta colorB)) {
                return false;
            }
            if (!colorA.getColor().equals(colorB.getColor())) {
                return false;
            }
        }

        // Lore
        if (metaA.getLore() != null && metaA.getLore() != null) {
            for (String lore : metaA.getLore()) {
                if (!Objects.requireNonNull(metaB.getLore()).contains(lore)) {
                    return false;
                }
            }
        } else if (!(metaA.getLore() == null && metaB.getLore() == null)) {
            return false;
        }

        // Enchants
        if (enchantsUnEqual(a.getEnchantments(), b.getEnchantments())) {
            return false;
        }

        // Books
        if (metaA instanceof BookMeta bookA) {
            if (!(metaB instanceof BookMeta bookB)) {
                return false;
            }

            // Author
            if (bookA.getAuthor() != null) {
                if (!bookA.getAuthor().equals(bookB.getAuthor())) {
                    return false;
                }
            }

            if (bookA.getTitle() != null) {
                if (!bookA.getTitle().equals(bookB.getTitle())) {
                    return false;
                }
            }

            // Pages
            if (bookA.getPageCount() != bookB.getPageCount()) {
                return false;
            }

            for (int page = 0; page < bookA.getPages().size(); page++) {
                String pageContentA = bookA.getPages().get(page);
                if (pageContentA != null && !pageContentA.equals(bookB.getPages().get(page))) {
                    return false;
                }
            }
        }

        // Enchanted books
        if (metaA instanceof EnchantmentStorageMeta enchA) {

            if (!(metaB instanceof EnchantmentStorageMeta enchB)) {
                return false;
            }

            if (enchA.hasStoredEnchants() != enchB.hasStoredEnchants()) {
                return false;
            }

            if (enchantsUnEqual(enchA.getStoredEnchants(), enchB.getStoredEnchants())) {
                return false;
            }

        }

        // Skulls
        if (metaA instanceof SkullMeta skullA) {
            if (!(metaB instanceof SkullMeta skullB)) {
                return false;
            }

            if (skullA.hasOwner() != skullB.hasOwner()) {
                return false;
            }

            return skullA.hasOwner()
                  && Objects.requireNonNull(skullA.getOwningPlayer()).getUniqueId()
                  .equals(Objects.requireNonNull(skullB.getOwningPlayer()).getUniqueId());
        }

        // Potions
        if (metaA instanceof PotionMeta potA) {
            if (!(metaB instanceof PotionMeta potB)) {
                return false;
            }

            for (int c = 0; c < potA.getCustomEffects().size(); c++) {
                PotionEffect e = potA.getCustomEffects().get(c);
                if (!e.equals(potB.getCustomEffects().get(c))) {
                    return true;
                }
            }
        }

        // Fireworks
        if (metaA instanceof FireworkMeta fwA) {
            if (!(metaB instanceof FireworkMeta fwB)) {
                return false;
            }

            if (fwA.getPower() != fwB.getPower()) {
                return false;
            }

            for (int e = 0; e < fwA.getEffects().size(); e++) {
                if (!fwA.getEffects().get(e).equals(fwB.getEffects().get(e))) {
                    return false;
                }
            }
        }

        // Firework Effects
        if (metaA instanceof FireworkEffectMeta fwA) {
            if (!(metaB instanceof FireworkEffectMeta fwB)) {
                return false;
            }

            FireworkEffect effectA = fwA.getEffect();
            FireworkEffect effectB = fwB.getEffect();
            if (effectA == null && effectB == null) {
                return true;
            }
            if (effectA == null) {
                return false;
            }
            if (effectB == null) {
                return false;
            }
            if (!effectA.getType().equals(effectB.getType())) {
                return false;
            }

            if (effectA.getColors().size() != effectB.getColors().size()) {
                return false;
            }

            // Colors
            for (int c = 0; c < effectA.getColors().size(); c++) {
                if (!effectA.getColors().get(c).equals(effectB.getColors().get(c))) {
                    return false;
                }
            }

            if (effectA.getFadeColors().size() != effectB.getFadeColors().size()) {
                return false;
            }

            // Fade colors
            for (int c = 0; c < effectA.getFadeColors().size(); c++) {
                if (!effectA.getFadeColors().get(c).equals(effectB.getFadeColors().get(c))) {
                    return false;
                }
            }

            if (effectA.hasFlicker() != effectB.hasFlicker()) {
                return false;
            }
            return effectA.hasTrail() == effectB.hasTrail();

        }

        return true;

    }

    /**
     * Checks if 2 sets are different.
     *
     * @param a Map
     * @param b Map
     * @return bool
     */
    protected static boolean enchantsUnEqual(Map<Enchantment, Integer> a, Map<Enchantment, Integer> b) {

        // Enchants
        if (a.size() != b.size()) {
            return true;
        }

        // Match enchantments and levels
        for (Entry<Enchantment, Integer> entryA : a.entrySet()) {

            // If enchantment not present
            if (!b.containsKey(entryA.getKey())) {
                return true;
            }

            // If levels don't match
            if (!b.get(entryA.getKey()).equals(entryA.getValue())) {
                return true;
            }

        }

        return false;

    }

    /**
     * Return the % used durabilility.
     *
     * @return String %
     * @todo this is buggy, wth?
     */
    public static String getUsedDurabilityPercentage(ItemStack item) {

        short currentDurability = (short) getItemDamage(item);
        short maxDurability = item.getType().getMaxDurability();
        if (currentDurability > 0 && maxDurability > 0 && currentDurability != maxDurability) {
            double diff = (((float)currentDurability / (float)maxDurability) * 100);
            if (diff > 0) {
                return Math.floor(diff) + "%";
            }
        }
        return "";
    }

    /**
     * Returns the durability remaining.
     *
     * @return String
     */
    public static String getDurabilityPercentage(ItemStack item) {

        short currentDurability = (short) getItemDamage(item);
        short maxDurability = item.getType().getMaxDurability();
        if (currentDurability > 0 && maxDurability > 0 && currentDurability != maxDurability) {
            double diff = maxDurability - currentDurability;
            diff = ((diff / maxDurability) * 100);
            if (diff > 0) {
                return Math.floor(diff) + "%";
            }
            return "0%";
        }

        return "";
    }

    /**
     * Returns a proper full name for an item, which includes meta content as well.
     *
     * @return string
     */
    public static String getItemFullNiceName(ItemStack item) {

        StringBuilder itemName = new StringBuilder(item.getType().name().toLowerCase(Locale.ENGLISH)
              .replace('_', ' '));

        ItemMeta meta = null;

        if (item.hasItemMeta()) {
            meta = item.getItemMeta();
        }

        // Leather Coloring
        if (meta instanceof LeatherArmorMeta lam) {
            if (lam.getColor() != Bukkit.getItemFactory().getDefaultLeatherColor()) {
                itemName.append(" dyed");
            }
        } else if (meta instanceof SkullMeta skull) {
            if (skull.hasOwner()) {
                itemName.append(Objects.requireNonNull(skull.getOwningPlayer()).getName()).append("'s ");
            }
        } else if (meta instanceof BookMeta book) {
            itemName.append(" '").append(book.getTitle()).append("' by ").append(book.getAuthor());
        }

        if (meta instanceof EnchantmentStorageMeta bookEnchantments) {
            if (bookEnchantments.hasStoredEnchants()) {
                Map<Enchantment, Integer> enchs = bookEnchantments.getStoredEnchants();
                applyEnchantments(enchs, itemName);
            }
        }

        // Enchantments
        Map<Enchantment, Integer> enchs = item.getEnchantments();
        applyEnchantments(enchs, itemName);

        // Fireworks
        if (meta instanceof FireworkEffectMeta fireworkMeta) {
            if (fireworkMeta.hasEffect()) {
                FireworkEffect effect = fireworkMeta.getEffect();
                if (effect != null) {
                    if (!effect.getColors().isEmpty()) {
                        itemName.append(" ").append(effect.getColors().size()).append(" colors");
                    }
                    if (!effect.getFadeColors().isEmpty()) {
                        itemName.append(" ").append(effect.getFadeColors().size()).append(" fade colors");
                    }
                    if (effect.hasFlicker()) {
                        itemName.append(" flickering");
                    }
                    if (effect.hasTrail()) {
                        itemName.append(" with trail");
                    }
                }
            }
        }

        // Custom item names
        if (meta != null) {
            // TODO: API fail here, report and check later
            if (meta.hasDisplayName() && meta.getDisplayName().length() > 0) {
                itemName.append(" named \"").append(meta.getDisplayName()).append("\"");
            }
        }

        return itemName.toString();

    }

    /**
     * Modifies the passed String builder with a list of enchants.
     *
     * @param enchants Map
     * @param itemName Current StringBuilder
     */
    private static void applyEnchantments(Map<Enchantment, Integer> enchants, StringBuilder itemName) {
        int i = 1;
        if (enchants.size() > 0) {
            itemName.append(" with");
            for (Map.Entry<Enchantment, Integer> ench : enchants.entrySet()) {
                itemName.append(" ").append(
                      EnchantmentUtils.getClientSideEnchantmentName(ench.getKey(), ench.getValue()));
                itemName.append(i < enchants.size() ? ", " : "");
                i++;
            }
        }
    }

    /**
     * Determines if an itemstack can be stacked. Maz stack size, meta data, and
     * more taken into account.
     *
     * @param item ItemStack
     */
    @SuppressWarnings("unused")
    public static boolean canSafelyStack(ItemStack item) {
        // Can't stack
        if (item.getMaxStackSize() == 1) {
            return false;
        }
        // Has meta
        ItemMeta im = item.getItemMeta();
        if (im == null) {
            return true;
        }
        return !im.hasDisplayName() && !im.hasEnchants() && !im.hasLore();
    }

    /**
     * Drop an item at a given location.
     *
     * @param location  The location to drop the item at
     * @param itemStack The item to drop
     */
    public static void dropItem(Location location, ItemStack itemStack) {
        if (location.getWorld() != null) {
            location.getWorld().dropItemNaturally(location, itemStack);
        }
    }

    /**
     * Drop items at a given location.
     *
     * @param location The location to drop the items at
     * @param is       The items to drop
     * @param quantity The amount of items to drop
     */
    @SuppressWarnings("unused")
    public static void dropItem(Location location, ItemStack is, int quantity) {
        for (int i = 0; i < quantity; i++) {
            dropItem(location, is);
        }
    }
}
