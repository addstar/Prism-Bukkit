package me.botsko.prism.wands;

import me.botsko.prism.config.PrismConfig;
import me.botsko.prism.utils.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public abstract class WandBase implements Wand {

    protected boolean itemGiven = false;

    protected PrismConfig.WandMode wandMode;

    protected Material item = Material.AIR;

    protected ItemStack originalItem;

    /**
     * Set the item has been given.
     *
     * @param given boolean.
     */
    public void setItemWasGiven(boolean given) {
        this.itemGiven = given;
    }

    /**
     * True if it was given.
     * @return boolean.
     */
    public boolean itemWasGiven() {
        return itemGiven;
    }

    /**
     * Get the wand mode.
     * @return String.
     */
    public PrismConfig.WandMode getWandMode() {
        return wandMode;
    }

    /**
     * Set the wand mode.
     *
     * @param mode String
     */
    public void setWandMode(PrismConfig.WandMode mode) {
        wandMode = mode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Material getItem() {
        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setItem(Material material) {
        item = material;
    }

    /**
     * Get from key.
     *
     * @param key String
     */
    public void setItemFromKey(String key) {
        item = Material.matchMaterial(key);
    }

    /**
     * Set original item.
     * @param item ItemStack
     */
    public void setOriginallyHeldItem(ItemStack item) {
        if (item.getType() != Material.AIR) {
            originalItem = item;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void disable(Player player) {
        final PlayerInventory inv = player.getInventory();
        if (itemWasGiven()) {
            int itemSlot;
            // Likely is what they're holding
            if (inv.getItemInMainHand().getType() == item) {
                itemSlot = inv.getHeldItemSlot();
            } else {
                itemSlot = InventoryUtils.inventoryHasItem(inv, item);
            }
            if (itemSlot > -1) {
                InventoryUtils.subtractAmountFromPlayerInvSlot(inv, itemSlot, 1);
                InventoryUtils.updateInventory(player);
            }
        }
        if (originalItem != null) {
            InventoryUtils.moveItemToHand(inv, originalItem.getType());
        }
    }
}
