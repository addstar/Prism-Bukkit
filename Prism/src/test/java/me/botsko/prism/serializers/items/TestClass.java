package me.botsko.prism.serializers.items;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 2/11/2020.
 */
public class TestClass {

    protected StorageItemStackSerializer storage;

    protected TestClass() {
        this.storage = new StorageItemStackSerializer();
        storage.amt = 2;
        storage.material = "CHEST";
        ItemStackSerializer item = new ItemStackSerializer();
        item.amt = 1;
        item.material = "DIRT";
        storage.inventoryContents.add(item);
    }
}
