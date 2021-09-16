package me.botsko.prism.actions;

import org.bukkit.block.BlockState;

public class PrismRollbackAction extends BlockChangeAction {

    private long parentId;

    /**
     * Set the block change.
     * @param oldBlock block
     * @param newBlock block
     * @param parentId long
     */
    public void setBlockChange(BlockState oldBlock, BlockState newBlock, long parentId) {
        // TODO: Why string? Why?
        this.parentId = parentId;
        if (oldBlock != null) {
            setOldMaterial(oldBlock.getType());
            setOldBlockData(oldBlock.getBlockData());

            // TODO: This is using oldBlock when it looks like it should use newBlock,
            // but this is how it was when I found it. Test later.
            setMaterial(oldBlock.getType());
            setBlockData(oldBlock.getBlockData());
        }
    }

    @Override
    public boolean hasExtraData() {
        return true;
    }

    @Override
    public String serialize() {
        return String.valueOf(parentId);
    }

    @Override
    public void deserialize(String data) {
        parentId = Long.parseLong(data);
    }

    /**
     * Get the parent rollback id.
     *
     * @return the parentId
     */
    public long getParentId() {
        return parentId;
    }
}