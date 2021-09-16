package me.botsko.prism.database;

import me.botsko.prism.utils.IntPair;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 5/04/2019.
 */
public interface IdMapQuery {

    void findMaterial(int blockId, int blockSubid, BiConsumer<String, String> success);

    void findMaterial(int blockId, int blockSubid, BiConsumer<String, String> success, Runnable failure);

    void findIds(String material, String state, BiConsumer<Integer, Integer> success);

    void findIds(String material, String state, BiConsumer<Integer, Integer> success, Runnable failure);

    void findAllIds(String material, Consumer<List<IntPair>> success);

    void findAllIds(String material, Consumer<List<IntPair>> success, Runnable failure);

    void findAllIdsPartial(String material, String stateLike, Consumer<List<IntPair>> success);

    void map(String material, String state, int blockId, int blockSubid);

    int mapAutoId(String material, String state);
}
