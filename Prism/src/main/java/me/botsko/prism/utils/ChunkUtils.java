package me.botsko.prism.utils;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class ChunkUtils {

    /**
     * Resets the preview border blocks.
     *
     * @param player Player
     * @param blocks Iterable Block list,
     */
    public static void resetPreviewBoundaryBlocks(Player player, Iterable<Block> blocks) {
        for (Block block : blocks) {
            EntityUtils.sendBlockChange(player, block.getLocation(), block.getBlockData());
        }
    }

    /**
     * Sets a block in preview mode around the border of a chunk.
     *
     * @param player Player
     * @param blocks Iterable Block list,
     */
    public static void setPreviewBoundaryBlocks(Player player, Iterable<Block> blocks, Material m) {
        BlockData blockData = m.createBlockData();
        for (Block block : blocks) {
            EntityUtils.sendBlockChange(player, block.getLocation(), blockData);
        }
    }

    /**
     * Returns the minimum vector for the chunk.
     *
     * @param chunk Chunk
     * @return Vector
     */
    public static Vector getChunkMinVector(Chunk chunk) {
        int blockMinX = chunk.getX() * 16;
        int blockMinZ = chunk.getZ() * 16;
        return new Vector(blockMinX, 0, blockMinZ);
    }

    /**
     * Returns the maximum vector for the chunk.
     *
     * @param chunk Chunk
     * @return Vector
     */
    public static Vector getChunkMaxVector(Chunk chunk) {
        int blockMinX = chunk.getX() * 16;
        int blockMinZ = chunk.getZ() * 16;
        int blockMaxX = blockMinX + 15;
        int blockMaxZ = blockMinZ + 15;
        return new Vector(blockMaxX, chunk.getWorld().getMaxHeight(), blockMaxZ);
    }

    /**
     * Returns an array of boundary blocks at a single Y for the current chunk.
     *
     * @param chunk chunk
     * @param y height
     * @return List of Blocks
     */
    public static ArrayList<Block> getBoundingBlocksAtY(Chunk chunk, int y) {

        int blockMinX = chunk.getX() * 16;
        int blockMinZ = chunk.getZ() * 16;
        int blockMaxX = blockMinX + 15;
        int blockMaxZ = blockMinZ + 15;

        ArrayList<Block> blocks = new ArrayList<>();

        // MinX -> MinZ
        for (int x = blockMinX; x < blockMaxX; x++) {
            blocks.add(chunk.getWorld().getBlockAt(x, y, blockMinZ));
        }
        // MinX -> MaxZ
        for (int x = blockMinX; x < blockMaxX; x++) {
            blocks.add(chunk.getWorld().getBlockAt(x, y, blockMaxZ));
        }
        // MinZ -> MinX
        for (int z = blockMinZ; z < blockMaxZ; z++) {
            blocks.add(chunk.getWorld().getBlockAt(blockMinX, y, z));
        }
        // MinZ -> MaxX
        for (int z = blockMinZ; z < blockMaxZ; z++) {
            blocks.add(chunk.getWorld().getBlockAt(blockMaxX, y, z));
        }

        return blocks;

    }
}