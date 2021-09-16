package me.botsko.prism.serializers.entity;

import me.botsko.prism.serializers.items.ItemStackSerializer;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 20/10/2020.
 */
public class AbstractVillagerSerializer<T extends AbstractVillager> extends EntitySerializer<T> {

    protected final Collection<ItemStackSerializer> inventory = new ArrayList<>();


    @Override
    public void serialize(T entity) {
        super.serialize(entity);
        entity.getInventory().forEach(itemStack -> {
            if (itemStack != null) {
                inventory.add(ItemStackSerializer.createItemStackSerialized(itemStack));
            }
        });
    }

    @Override
    public void deserialize(T entity) {
        super.deserialize(entity);
        List<ItemStack> items = new ArrayList<>();
        inventory.forEach(prismItemStack -> items.add(prismItemStack.toBukkit()));
        entity.getInventory().setContents(items.toArray(new ItemStack[0]));
    }

}
