package me.botsko.prism.actions;

import me.botsko.prism.api.ChangeResult;
import me.botsko.prism.api.ChangeResultType;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.api.commands.Flag;
import me.botsko.prism.appliers.ChangeResultImpl;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class PortalCreateAction extends BlockAction {

    /**
     * Set the location.
     * @param loc Location
     */
    public void setLocation(final Location loc) {
        if (loc != null) {
            setLoc(loc);
        }
    }

    /**
     * Set the new block state.
     * @param state BlockState
     */
    public void setNewBlock(final BlockState state) {
        if (state != null) {
            setMaterial(state.getType());
            setBlockData(state.getBlockData());
        }
    }

    /**
     * Set the old block state.
     * @param state BlockState
     */
    public void setOldBlock(final BlockState state) {
        if (state != null) {
            setOldMaterial(state.getType());
            setOldBlockData(state.getBlockData());
        }
    }

    @Override
    public ChangeResult applyRollback(Player player, PrismParameters parameters, boolean isPreview) {
        final Block block = getWorld().getBlockAt(getLoc());
        return placeBlock(player, parameters, isPreview, getActionType().getName(), getOldMaterial(), getOldBlockData(),
                getMaterial(), getBlockData(), block, false);
    }

    @Override
    public ChangeResult applyRestore(Player player, PrismParameters parameters, boolean isPreview) {
        final Block block = getWorld().getBlockAt(getLoc());
        return placeBlock(player, parameters, isPreview, getActionType().getName(), getOldMaterial(), getOldBlockData(),
                getMaterial(), getBlockData(), block, false);
    }

    @Override
    public ChangeResult applyUndo(Player player, PrismParameters parameters, boolean isPreview) {
        final Block block = getWorld().getBlockAt(getLoc());
        return placeBlock(player, parameters, isPreview, getActionType().getName(), getOldMaterial(), getOldBlockData(),
                getMaterial(), getBlockData(), block, false);
    }

    @Override
    public ChangeResult applyDeferred(Player player, PrismParameters parameters, boolean isPreview) {
        final Block block = getWorld().getBlockAt(getLoc());
        return placeBlock(player, parameters, isPreview, getActionType().getName(), getOldMaterial(), getOldBlockData(),
                getMaterial(), getBlockData(), block, true);
    }

    private ChangeResult placeBlock(Player player, PrismParameters parameters, boolean isPreview, String type,
                                    Material oldMat, BlockData oldData, Material newMat,
                                    BlockData newData, Block block, boolean isDeferred) {

        // TODO: For some reason, the portal is not being recreated on a RESTORE unless forced
        parameters.addFlag(Flag.OVERWRITE);

        final BlockAction b = new BlockAction();
        b.setActionType(type);
        b.setLoc(getLoc());
        if (parameters.getProcessType().equals(PrismProcessType.ROLLBACK)) {
            b.setMaterial(oldMat);
            b.setBlockData(oldData);
            return b.placeBlock(player, parameters, isPreview, block, isDeferred);
        }
        if (parameters.getProcessType().equals(PrismProcessType.RESTORE)) {
            b.setMaterial(newMat);
            b.setBlockData(newData);
            return b.placeBlock(player, parameters, isPreview, block, isDeferred);
        }
        if (parameters.getProcessType().equals(PrismProcessType.UNDO)) {
            b.setMaterial(oldMat);
            b.setBlockData(oldData);
            return b.placeBlock(player, parameters, isPreview, block, isDeferred);
        }
        return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
    }
}
