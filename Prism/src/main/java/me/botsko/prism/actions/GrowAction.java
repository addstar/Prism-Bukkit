package me.botsko.prism.actions;

import org.bukkit.block.BlockState;

public class GrowAction extends BlockAction {

    /**
     * Set the block state.
     * @param state BlockState
     */
    public void setBlock(BlockState state) {
        if (state != null) {
            setMaterial(state.getType());
            setBlockData(state.getBlockData());
            setLoc(state.getLocation());
        }
    }
}