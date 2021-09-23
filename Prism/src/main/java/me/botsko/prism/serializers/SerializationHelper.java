package me.botsko.prism.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.botsko.prism.serializers.items.ItemStackSerializer;
import me.botsko.prism.serializers.items.StorageItemStackSerializer;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 30/10/2020.
 */
public class SerializationHelper {
    private static final Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
        RuntimeTypeAdapterFactory<ItemStackSerializer> factory = RuntimeTypeAdapterFactory
                .of(ItemStackSerializer.class,"objectType");
        factory.registerSubtype(ItemStackSerializer.class);
        factory.registerSubtype(StorageItemStackSerializer.class);
        builder.registerTypeAdapterFactory(factory);
        gson = builder.create();
    }

    /**
     * Gson.
     * @return gson.
     */
    public static Gson gson() {
        return gson;
    }
}
