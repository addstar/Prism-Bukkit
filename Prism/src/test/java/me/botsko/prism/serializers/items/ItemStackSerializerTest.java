package me.botsko.prism.serializers.items;

import me.botsko.prism.serializers.SerializationHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 2/11/2020.
 */
class ItemStackSerializerTest {

    @Test
    protected void testDeserialize() {
        ItemStackSerializer serializer = new ItemStackSerializer();
        serializer.amt = 1;
        serializer.material = "DIRT";
        serializer.name = "SOME_DIRT";
        String data = SerializationHelper.gson().toJson(serializer);
        String expected = "{\"objectType\":\"ItemStackSerializer\",\"amt\":1,\"name\":\"SOME_DIRT\",\"color\""
                + ":0,\"slot\":\"-1\",\"hasFlicker\":false,\"hasTrail\":false,\"durability\":0,\"material\":\"DIRT\""
                + ",\"potionExtended\":false,\"potionUpgraded\":false}";
        assertEquals(expected,data);
        ItemStackSerializer out = SerializationHelper.gson().fromJson(expected,ItemStackSerializer.class);
        assertEquals(serializer.amt,out.amt);
        assertEquals(serializer.material,out.material);
        StorageItemStackSerializer storage = new StorageItemStackSerializer();
        storage.amt = 1;
        storage.material = "SHULKER";
        storage.inventoryContents.add(serializer);
        String storageData = SerializationHelper.gson().toJson(storage,ItemStackSerializer.class);
        Object storageNew = SerializationHelper.gson().fromJson(storageData,ItemStackSerializer.class);
        assertTrue(storageNew instanceof StorageItemStackSerializer);
        assertEquals(1,((StorageItemStackSerializer) storageNew).inventoryContents.size());
        assertEquals("DIRT",((StorageItemStackSerializer) storageNew).inventoryContents.get(0).material);
        String oldItem = "{\"amt\":1,\"name\":\"SOME_DIRT\",\"color\":0,\"slot\":\"-1\",\"hasFlicker\""
                + ":false,\"hasTrail\":false,\"durability\":0,\"material\":\"SHULKER\"}";
        ItemStackSerializer oldOut = SerializationHelper.gson().fromJson(oldItem,ItemStackSerializer.class);
        assertEquals(storage.material,oldOut.material);
        TestClass test = new TestClass();
        String testString = SerializationHelper.gson().toJson(test);
        TestClass obj = SerializationHelper.gson().fromJson(testString,TestClass.class);
        assertEquals(obj.storage.inventoryContents.size(),test.storage.inventoryContents.size());


    }
}