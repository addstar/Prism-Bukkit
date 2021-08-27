package me.botsko.prism.api.actions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Handler {

    /**
     * Gets the internal ID of the handler.
     *
     * @return long
     */
    long getId();

    void setId(long id);

    /**
     * Get the UnixEpoch time this action was recorded.
     * @return long
     */
    long getUnixEpoch();

    void setUnixEpoch(long epoch);

    /**
     * Get the Date as a human readable format.
     * @return String
     */
    String getDisplayDate();

    /**
     * Get the time as a human readable format.
     *
     * @return String
     */
    String getDisplayTime();

    /**
     * True of this action has extra info.
     *
     * @return boolean
     */
    boolean hasExtraData();

    /**
     * Time elapsed since this action.
     *
     * @return String
     */
    String getTimeSince();

    /**
     * The ActionType.
     * @return ActionType
     */
    ActionType getActionType();

    void setActionType(ActionType type);

    void setWorld(World world);

    Location getLoc();

    /**
     * A displayable name for the source of the action.
     *
     * @return String
     */
    @Nullable String getSourceName();

    void setSourceName(String name);

    void setX(double x);

    void setY(double y);

    void setZ(double z);

    /**
     * The material.
     *
     * @return Material
     */
    Material getMaterial();

    void setMaterial(Material material);

    /**
     * {@link BlockData} for the block.
     *
     * @return BlockData
     */
    BlockData getBlockData();

    void setBlockData(BlockData state);

    /**
     * The durability. used to be used as a datatype.
     * @return short
     */

    @Deprecated
    short getDurability();

    void setDurability(short durability);

    /**
     * Serialize the handler.
     * @return String
     */
    String serialize();

    /**
     * Deserialize from a String.
     * @param data String
     */
    void deserialize(String data);

    /**
     * The old material.
     *
     * @return Material
     */
    Material getOldMaterial();

    void setOldMaterial(Material material);

    /**
     * The old {@link BlockData}.
     *
     * @return BlockData
     */
    BlockData getOldBlockData();

    void setOldBlockData(BlockData state);

    short getOldDurability();

    void setOldDurability(short durability);

    int getAggregateCount();

    void setAggregateCount(int aggregateCount);

    String getNiceName();

    UUID getUuid();

    void setUuid(UUID uuid);

    boolean isCanceled();

    void setCanceled(boolean cancel);

    /**
     * Gets a custom description for a specific handler.
     *
     *
     * @return String
     */
    String getCustomDesc();

    boolean isRollbacked();

    void setRollbacked(boolean rollbacked);

}