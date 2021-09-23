package me.botsko.prism.actions;

import me.botsko.prism.api.ChangeResult;
import me.botsko.prism.api.ChangeResultType;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.appliers.ChangeResultImpl;
import me.botsko.prism.serializers.SerializationHelper;
import me.botsko.prism.utils.TypeUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

import java.util.Objects;

public class SignAction extends GenericAction {

    protected SignChangeActionData actionData;

    /**
     * Set the block.
     *
     * @param block Block
     * @param lines String[]
     */
    public void setBlock(Block block, String[] lines) {

        // Build an object for the specific details of this action
        actionData = new SignChangeActionData();

        if (block != null) {
            actionData.signType = block.getType().name();

            if (block.getBlockData() instanceof Directional) {
                actionData.facing = ((Directional) block.getBlockData()).getFacing();
            }

            setMaterial(block.getType());
            setLoc(block.getLocation());
        }
        if (lines != null) {
            actionData.lines = lines;
        }
    }

    @Override
    public void deserialize(String data) {
        if (data != null && !data.isEmpty()) {
            actionData = SerializationHelper.gson().fromJson(data, SignChangeActionData.class);
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

    /**
     * Get Lines.
     * @return String[]
     */
    public String[] getLines() {
        return actionData.lines;
    }

    /**
     * Get the sign type.
     * @return Material
     */
    public Material getSignType() {
        if (actionData.signType != null) {
            final Material m = Material.matchMaterial(actionData.signType);
            if (m != null) {
                return m;
            }
        }

        // Could be legacy (x - 1.13) wall sign
        if (Objects.equals(actionData.signType, "WALL_SIGN")) {
            return Material.OAK_WALL_SIGN;
        }

        // Either was legacy standing sign or unknown/invalid. Default standing sign.
        return Material.OAK_SIGN;
    }

    /**
     * Get the blockface.
     * @return BlockFace.
     */
    public BlockFace getFacing() {
        return actionData.facing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNiceName() {
        String name = "sign (";
        if (actionData.lines != null && actionData.lines.length > 0) {
            name += TypeUtils.join(actionData.lines, ", ");
        } else {
            name += "no text";
        }
        name += ")";
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeResult applyRestore(Player player, PrismParameters parameters, boolean isPreview) {

        final Block block = getWorld().getBlockAt(getLoc());

        // Ensure a sign exists there (and no other block)
        if (block.getType().equals(Material.AIR) || Tag.SIGNS.isTagged(block.getType())) {

            if (block.getType().equals(Material.AIR)) {
                block.setType(getSignType());
            }

            // Set the facing direction
            if (block.getBlockData() instanceof Directional) {
                ((Directional) block.getBlockData()).setFacing(getFacing());
            }

            // Set the content
            if (block.getState() instanceof final org.bukkit.block.Sign sign) {

                // Set sign data
                final String[] lines = getLines();
                int i = 0;
                if (lines != null && lines.length > 0) {
                    for (final String line : lines) {
                        sign.setLine(i, line);
                        i++;
                    }
                }
                sign.update(true, false);
                return new ChangeResultImpl(ChangeResultType.APPLIED, null);
            }
        }
        return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
    }

    public static class SignChangeActionData {
        public String[] lines;
        public String signType;
        public BlockFace facing;
    }
}