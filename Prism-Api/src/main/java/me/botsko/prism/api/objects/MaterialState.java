package me.botsko.prism.api.objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

/**
 * Created for Prism
 * Created by Narimm on 10/01/2021.
 */
public class MaterialState {
    public Material material;
    public String state;

    public MaterialState() {
    }

    public MaterialState(Material material, String state) {
        this.material = material;
        this.state = state;
    }

    /**
     * Get BlockData.
     *
     * @return BlockData
     */
    public BlockData asBlockData() {
        try {
            BlockData data = Bukkit.createBlockData(material, state);

            // In the event that we tried to get BlockData for an item and it returned air
            if (data.getMaterial() == material) {
                return data;
            }
        } catch (IllegalArgumentException ignored) {
            //ignored
        }

        return null;
    }

    /**
     * Get as Item.
     *
     * @return ItemStack
     */
    public ItemStack asItem() {
        ItemStack item = new ItemStack(material, 1);

        if (!state.isEmpty()) {
            try {
                setItemDamage(item, Short.parseShort(state));
            } catch (NumberFormatException ignored) {
                //ignored
            }

        }

        return item;
    }

    @Override
    public String toString() {
        return material.name().toLowerCase(Locale.ENGLISH) + state;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Sets an item as damaged by the amount given as the second param.
     *
     * @param stack  ItemStack
     * @param damage Integer
     */
    public static void setItemDamage(ItemStack stack, int damage) {
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(stack.getType());

        if (meta instanceof Damageable d) {

            d.setDamage(damage);
            stack.setItemMeta(meta);
        }
    }
}
