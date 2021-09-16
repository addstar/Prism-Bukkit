package me.botsko.prism.serializers.items;

import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 29/10/2020.
 */
public class StorageItemStackSerializer extends ItemStackSerializer {
    public final List<ItemStackSerializer> inventoryContents = new ArrayList<>();

    @Override
    public ItemStack toBukkit() {
        ItemStack item =  super.toBukkit();
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof BlockStateMeta) {
            BlockState state = ((BlockStateMeta) meta).getBlockState();
            if (state instanceof Container) {
                Inventory inv = ((Container) state).getInventory();
                inventoryContents.forEach(itemStackSerializer -> inv.addItem(itemStackSerializer.toBukkit()));
                state.update();
            }
            ((BlockStateMeta) meta).setBlockState(state);
        }
        item.setItemMeta(meta);
        return item;
    }
}
