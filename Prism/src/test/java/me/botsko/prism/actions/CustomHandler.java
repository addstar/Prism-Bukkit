package me.botsko.prism.actions;

import me.botsko.prism.api.actions.Action;
import me.botsko.prism.api.actions.Handler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * An example of a custom handler.
 * Created for Prism.
 *
 * @author Narimm on 18/08/2021
 * @since 2.1.8
 */
public class CustomHandler implements Handler {

    private long id;
    private long epoch;


    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void setId(long id) {

    }

    @Override
    public long getUnixEpoch() {
        return 0;
    }

    @Override
    public void setUnixEpoch(long epoch) {

    }

    @Override
    public String getDisplayDate() {
        return null;
    }

    @Override
    public String getDisplayTime() {
        return null;
    }

    @Override
    public boolean hasExtraData() {
        return false;
    }

    @Override
    public String getTimeSince() {
        return null;
    }

    @Override
    public Action getAction() {
        return null;
    }

    @Override
    public void setAction(Action type) {

    }

    @Override
    public void setWorld(World world) {

    }

    @Override
    public Location getLoc() {
        return null;
    }

    @Override
    public @Nullable String getSourceName() {
        return null;
    }

    @Override
    public void setSourceName(String name) {

    }

    @Override
    public void setX(double x) {

    }

    @Override
    public void setY(double y) {

    }

    @Override
    public void setZ(double z) {

    }

    @Override
    public Material getMaterial() {
        return null;
    }

    @Override
    public void setMaterial(Material material) {

    }

    @Override
    public BlockData getBlockData() {
        return null;
    }

    @Override
    public void setBlockData(BlockData state) {

    }

    @Override
    public short getDurability() {
        return 0;
    }

    @Override
    public void setDurability(short durability) {

    }

    @Override
    public String serialize() {
        return null;
    }

    @Override
    public void deserialize(String data) {

    }

    @Override
    public Material getOldMaterial() {
        return null;
    }

    @Override
    public void setOldMaterial(Material material) {

    }

    @Override
    public BlockData getOldBlockData() {
        return null;
    }

    @Override
    public void setOldBlockData(BlockData state) {

    }

    @Override
    public short getOldDurability() {
        return 0;
    }

    @Override
    public void setOldDurability(short durability) {

    }

    @Override
    public int getAggregateCount() {
        return 0;
    }

    @Override
    public void setAggregateCount(int aggregateCount) {

    }

    @Override
    public String getNiceName() {
        return null;
    }

    @Override
    public UUID getUuid() {
        return null;
    }

    @Override
    public void setUuid(UUID uuid) {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void setCanceled(boolean cancel) {

    }

    @Override
    public String getCustomDesc() {
        return null;
    }
}
