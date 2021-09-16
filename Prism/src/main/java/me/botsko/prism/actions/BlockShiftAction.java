package me.botsko.prism.actions;

import me.botsko.prism.Prism;
import me.botsko.prism.serializers.SerializationHelper;
import org.bukkit.block.Block;

public class BlockShiftAction extends GenericAction {

    private BlockShiftActionData actionData;

    /**
     * Set the Block.
     *
     * @param from from what Block.
     */
    public void setBlock(Block from) {

        // Build an object for the specific details of this action
        actionData = new BlockShiftActionData();

        // Store information for the action
        if (from != null) {

            setMaterial(from.getType());
            setBlockData(from.getBlockData());
            actionData.x = from.getX();
            actionData.y = from.getY();
            actionData.z = from.getZ();

        }
    }

    @Override
    public boolean hasExtraData() {
        return actionData != null;
    }

    @Override
    public String serialize() {
        return SerializationHelper.gson().toJson(actionData);
    }

    @Override
    public void deserialize(String data) {
        if (data != null && data.startsWith("{")) {
            actionData = SerializationHelper.gson().fromJson(data, BlockShiftActionData.class);
        }
    }

    @Override
    public String getNiceName() {
        String location = "unknown";
        if (actionData != null) {
            location = actionData.x + " " + actionData.y + " " + actionData.z;
        }

        return Prism.getItems().getAlias(getMaterial(), getBlockData()) + " from " + location;
    }

    public static class BlockShiftActionData {
        int x;
        int y;
        int z;
    }
}