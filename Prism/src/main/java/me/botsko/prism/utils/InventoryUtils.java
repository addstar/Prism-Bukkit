package me.botsko.prism.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A utility class to manage inventory.
 *
 * @author botskonet
 **/
public class InventoryUtils {

    private static final EnumMap<Material, EquipmentSlot> slots = new EnumMap<>(Material.class);

    static {
        slots.put(Material.LEATHER_BOOTS, EquipmentSlot.FEET);
        slots.put(Material.CHAINMAIL_BOOTS, EquipmentSlot.FEET);
        slots.put(Material.IRON_BOOTS, EquipmentSlot.FEET);
        slots.put(Material.GOLDEN_BOOTS, EquipmentSlot.FEET);
        slots.put(Material.DIAMOND_BOOTS, EquipmentSlot.FEET);

        slots.put(Material.LEATHER_LEGGINGS, EquipmentSlot.LEGS);
        slots.put(Material.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS);
        slots.put(Material.IRON_LEGGINGS, EquipmentSlot.LEGS);
        slots.put(Material.GOLDEN_LEGGINGS, EquipmentSlot.LEGS);
        slots.put(Material.DIAMOND_LEGGINGS, EquipmentSlot.LEGS);

        slots.put(Material.LEATHER_CHESTPLATE, EquipmentSlot.CHEST);
        slots.put(Material.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST);
        slots.put(Material.IRON_CHESTPLATE, EquipmentSlot.CHEST);
        slots.put(Material.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST);
        slots.put(Material.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST);

        slots.put(Material.LEATHER_HELMET, EquipmentSlot.HEAD);
        slots.put(Material.CHAINMAIL_HELMET, EquipmentSlot.HEAD);
        slots.put(Material.IRON_HELMET, EquipmentSlot.HEAD);
        slots.put(Material.GOLDEN_HELMET, EquipmentSlot.HEAD);
        slots.put(Material.DIAMOND_HELMET, EquipmentSlot.HEAD);

        slots.put(Material.SKELETON_SKULL, EquipmentSlot.HEAD);
        slots.put(Material.WITHER_SKELETON_SKULL, EquipmentSlot.HEAD);
        slots.put(Material.CREEPER_HEAD, EquipmentSlot.HEAD);
        slots.put(Material.DRAGON_HEAD, EquipmentSlot.HEAD);
        slots.put(Material.PLAYER_HEAD, EquipmentSlot.HEAD);
        slots.put(Material.ZOMBIE_HEAD, EquipmentSlot.HEAD);
        slots.put(Material.CARVED_PUMPKIN, EquipmentSlot.HEAD);
        slots.put(Material.TURTLE_HELMET, EquipmentSlot.HEAD);

    }

    /**
     * Easier to suppress deprecation errors.
     *
     * @param p Player
     */
    public static void updateInventory(Player p) {
        p.updateInventory();
    }

    /**
     * Returns ItemStock for a Slot.
     * @param equipment Equipment
     * @param slot Slot
     * @return ItemStack
     */
    public static ItemStack getEquipment(EntityEquipment equipment, EquipmentSlot slot) {
        return switch (slot) {
            case HAND -> equipment.getItemInMainHand();
            case OFF_HAND -> equipment.getItemInOffHand();
            case FEET -> equipment.getBoots();
            case LEGS -> equipment.getLeggings();
            case CHEST -> equipment.getChestplate();
            case HEAD -> equipment.getHelmet();
            default -> throw new IllegalArgumentException("EquipmentSlot " + slot.name() + " not recognised");
        };
    }

    /**
     * Set the ItemStack for  Slot.
     * @param equipment EntityEquipment
     * @param slot EquipmentSlot
     * @param item ItemStack
     */
    public static void setEquipment(EntityEquipment equipment, EquipmentSlot slot, ItemStack item) {
        switch (slot) {
            case HAND -> equipment.setItemInMainHand(item);
            case OFF_HAND -> equipment.setItemInOffHand(item);
            case FEET -> equipment.setBoots(item);
            case LEGS -> equipment.setLeggings(item);
            case CHEST -> equipment.setChestplate(item);
            case HEAD -> equipment.setHelmet(item);
            default -> {
            }
            //should never be hit
        }
    }

    public static EquipmentSlot getTargetArmorSlot(Material material) {
        return slots.getOrDefault(material, EquipmentSlot.HAND);
    }

    /**
     * True if the player has any armor.
     *
     * @param p Player
     * @return bool
     */
    public static boolean playerArmorIsEmpty(Player p) {
        for (ItemStack item : p.getInventory().getArmorContents()) {
            if (item != null && !item.getType().equals(Material.AIR)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the slot id of a specific item type, or -1 if none.
     *
     * @param inv Inventory
     * @return slot id integer
     */
    public static int inventoryHasItem(Inventory inv, Material material) {
        int currentSlot = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == material) {
                return currentSlot;
            }
            currentSlot++;
        }
        return -1;
    }

    /**
     * Get items from inventory matching item in hand.
     * @param player Player
     * @param desiredQuantity qty
     * @return ItemStack
     */
    @SuppressWarnings("unused")
    public static ItemStack extractItemsMatchingHeldItemFromPlayer(Player player, int desiredQuantity) {

        if (player == null || !ItemUtils.isValidItem(player.getInventory().getItemInMainHand())) {
            throw new IllegalArgumentException("Invalid player or invalid held item.");
        }

        int quantityFound = 0;
        ItemStack itemDefinition = player.getInventory().getItemInMainHand().clone();

        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null) {
                continue;
            }
            if (ItemUtils.equals(item, itemDefinition)) {

                // check how many items we need
                int diff = desiredQuantity - quantityFound;

                // Consume whole stack
                if (diff > item.getAmount()) {
                    quantityFound += item.getAmount();
                    player.getInventory().clear(slot);
                } else {
                    quantityFound += diff;
                    item.setAmount(item.getAmount() - diff);
                    player.getInventory().setItem(slot, item);
                }
            }
            if (desiredQuantity == quantityFound) {
                break;
            }
        }

        itemDefinition.setAmount(quantityFound);

        return itemDefinition;

    }

    /**
     * Moves a specific item to the player's hand, returns false if the item doesn't
     * exist in the inventory.
     *
     * @param inv Inventory
     * @return boolean on success.
     */
    public static boolean moveItemToHand(PlayerInventory inv, Material material) {
        int slot = inventoryHasItem(inv, material);
        if (slot > -1) {
            ItemStack item = inv.getItem(slot);
            inv.clear(slot);
            // If the player has an item in-hand, switch to a vacant spot
            if (!playerHasEmptyHand(inv)) {
                inv.setItem(slot, inv.getItemInMainHand());
            }
            inv.setItemInMainHand(item);
            return true;
        }
        return false;
    }

    /**
     * Whether or not the player has an empty hand.
     *
     * @param inv Inventory
     * @return boolean
     */
    public static boolean playerHasEmptyHand(PlayerInventory inv) {
        return (inv.getItemInMainHand().getType() == Material.AIR);
    }

    /**
     * Adds an item to the inventory, returns a hashmap of leftovers.
     */
    public static HashMap<Integer, ItemStack> addItemToInventory(Inventory inv, ItemStack item) {
        return inv.addItem(item);
    }

    /**
     * Give an item to a player.
     * @param inv Inventory
     * @param item ItemStack
     * @return boolean on success.
     */
    public static boolean handItemToPlayer(PlayerInventory inv, ItemStack item) {
        // Ensure there's at least one empty inv spot
        if (inv.firstEmpty() != -1) {
            ItemStack originalItem = inv.getItemInMainHand().clone();
            // If the player has an item in-hand, switch to a vacant spot
            if (!playerHasEmptyHand(inv)) {
                // We need to manually add the item stack to a different
                // slot because by default, bukkit combines items with addItem
                // and that was causing items to be lost unless they were the max
                // stack size
                for (int i = 0; i <= inv.getSize(); i++) {
                    if (i == inv.getHeldItemSlot()) {
                        continue;
                    }
                    ItemStack current = inv.getItem(i);
                    if (current == null) {
                        inv.setItem(i, originalItem);
                        break;
                    }
                }
            }
            inv.setItemInMainHand(item);
            return true;
        }
        return false;
    }

    /**
     * Subtract a specific quantity from an inventory slots item stack.
     *
     * @param inv Inventory
     * @param slot Slot id
     * @param quant amount
     */
    public static void subtractAmountFromPlayerInvSlot(Inventory inv, int slot, int quant) {
        ItemStack itemAtSlot = inv.getItem(slot);
        if (itemAtSlot != null && quant <= 64) {
            itemAtSlot.setAmount(itemAtSlot.getAmount() - quant);
            if (itemAtSlot.getAmount() == 0) {
                inv.clear(slot);
            }
        }
    }

    /**
     * Drop items at player's location.
     *
     * @param leftovers Map
     * @param player Player
     */
    @SuppressWarnings("unused")
    public static void dropItemsByPlayer(Map<Integer, ItemStack> leftovers, Player player) {
        if (!leftovers.isEmpty()) {
            for (Entry<Integer, ItemStack> entry : leftovers.entrySet()) {
                player.getWorld().dropItemNaturally(player.getLocation(), entry.getValue());
            }
        }
    }

    /**
     * Is an inventory fully empty.
     *
     * @param in Inventory
     * @return bool
     */
    public static boolean isEmpty(Inventory in) {
        boolean ret = false;
        if (in == null) {
            return true;
        }
        for (ItemStack item : in.getContents()) {
            ret |= (item != null);
        }
        return !ret;
    }

    /*

      @param player
     * @param target
     * @return
     * @throws Exception
     */
    /*
     * public static void movePlayerInventoryToContainer( PlayerInventory inv, Block
     * target, HashMap<Integer,Short> filters ) throws Exception{ InventoryHolder
     * container = (InventoryHolder) target.getState(); if(
     * !moveInventoryToInventory( inv, container.getInventory(), false, filters ) ){
     * throw new Exception("Target container is full."); } }
     */

    /*

      @param player
     * @param target
     * @return
     * @throws Exception
     */
    /*
     * public static void moveContainerInventoryToPlayer( PlayerInventory inv, Block
     * target, HashMap<Integer,Short> filters ) throws Exception{ InventoryHolder
     * container = (InventoryHolder) target.getState(); moveInventoryToInventory(
     * container.getInventory(), inv, false, filters ); }
     */

    /*

      @param player
     * @param chest
     * @param fullFlag
     * @return
     */
    /*
     * public static boolean moveInventoryToInventory( Inventory from, Inventory to,
     * boolean fullFlag, HashMap<Integer,Short> filters ) {
     *
     * HashMap<Integer, ItemStack> leftovers;
     *
     * if (to.firstEmpty() != -1 && !fullFlag){ for (ItemStack item :
     * from.getContents()) { if(to.firstEmpty() == -1){ return false; } if (item !=
     * null && to.firstEmpty() != -1) {
     *
     * boolean shouldTransfer = false; if( filters.size() > 0 ){ for(
     * Entry<Integer,Short> entry : filters.entrySet() ){ if( entry.getKey() ==
     * item.getTypeId() && entry.getValue() == item.getDurability() ){
     * shouldTransfer = true; } } } else { shouldTransfer = true; }
     *
     * if( !shouldTransfer ) continue;
     *
     * leftovers = to.addItem(item); if (leftovers.size() == 0) {
     * from.removeItem(item); } else { from.removeItem(item);
     * from.addItem(leftovers.get(0)); }
     *
     * } } return true; } return false; }
     */

    /*

      @param stack
     * @param player
     * @return
     */
    /*
     * public static ItemStack[] sortItemStack(ItemStack[] stack, Player player) {
     * return sortItemStack(stack, 0, stack.length, player); }
     */

    /*

      @param stack
     * @param start
     * @param end
     * @param player
     * @return
     */
    /*
     * public static ItemStack[] sortItemStack(ItemStack[] stack, int start, int
     * end, Player player) { stack = stackItems(stack, start, end);
     * recQuickSort(stack, start, end - 1); return stack; }
     */

    /*

      @param items
     * @param start
     * @param end
     * @return
     */
    /*
     * private static ItemStack[] stackItems(ItemStack[] items, int start, int end)
     * { for (int i = start; i < end; i++) { ItemStack item = items[i];
     *
     * // Avoid infinite stacks and stacks with durability if (item == null ||
     * item.getAmount() <= 0 || !ItemUtils.canSafelyStack( item )) { continue; }
     *
     * int max_stack = item.getMaxStackSize(); if (item.getAmount() < max_stack){
     * int needed = max_stack - item.getAmount(); // Number of needed items until
     * max_stack
     *
     * // Find another stack of the same type for (int j = i + 1; j < end; j++) {
     * ItemStack item2 = items[j];
     *
     * // Avoid infinite stacks and stacks with durability if (item2 == null ||
     * item2.getAmount() <= 0 || !ItemUtils.canSafelyStack( item )) { continue; }
     *
     * // Same type? // Blocks store their color in the damage value if
     * (item2.getType() == item.getType() &&
     * (!ItemUtils.dataValueUsedForSubitems(item.getTypeId()) ||
     * item.getDurability() == item2.getDurability())) { // This stack won't fit in
     * the parent stack if (item2.getAmount() > needed) { item.setAmount(max_stack);
     * item2.setAmount(item2.getAmount() - needed); break; } else {
     * item.setAmount(item.getAmount() + item2.getAmount()); needed = max_stack -
     * item.getAmount(); items[j].setType(Material.AIR); } } } } } return items; }
     */

    /*

      @param list
     * @param first
     * @param second
     */
    /*
     * private static void swap(ItemStack[] list, int first, int second) { ItemStack
     * temp; temp = list[first]; list[first] = list[second]; list[second] = temp; }
     */

    /*

      @param list
     * @param first
     * @param last
     * @return
     */
    /*
     * private static int partition(ItemStack[] list, int first, int last) {
     * ItemStack pivot;
     *
     * int smallIndex;
     *
     * swap(list, first, (first + last) / 2);
     *
     * pivot = list[first]; smallIndex = first;
     *
     * for (int index = first + 1; index <= last; index++) {
     *
     * ItemStack item = list[index];
     *
     * if( ItemUtils.equals( item, pivot ) ){ smallIndex++; swap(list, smallIndex,
     * index); } }
     *
     * swap(list, first, smallIndex); return smallIndex;
     *
     * }
     */

    /*

      @param list
     * @param first
     * @param last
     */
    /*
     * private static void recQuickSort(ItemStack[] list, int first, int last) { if
     * (first < last) { int pivotLocation = partition(list, first, last);
     * recQuickSort(list, first, pivotLocation - 1); recQuickSort(list,
     * pivotLocation + 1, last); } }
     */
}