package me.botsko.prism.actions;

import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.api.ChangeResult;
import me.botsko.prism.api.ChangeResultType;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.appliers.ChangeResultImpl;
import me.botsko.prism.commands.Flags;
import me.botsko.prism.events.BlockStateChangeImpl;
import me.botsko.prism.serializers.SerializationHelper;
import me.botsko.prism.serializers.items.ItemStackSerializer;
import me.botsko.prism.utils.EntityUtils;
import me.botsko.prism.utils.MaterialTag;
import me.botsko.prism.utils.TypeUtils;
import me.botsko.prism.utils.block.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Nameable;
import org.bukkit.Tag;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Material.AIR;
import static org.bukkit.Material.CHEST;
import static org.bukkit.Material.COMMAND_BLOCK;
import static org.bukkit.Material.FARMLAND;
import static org.bukkit.Material.FIRE;
import static org.bukkit.Material.JUKEBOX;
import static org.bukkit.Material.NETHER_PORTAL;
import static org.bukkit.Material.OBSIDIAN;
import static org.bukkit.Material.PLAYER_HEAD;
import static org.bukkit.Material.PLAYER_WALL_HEAD;
import static org.bukkit.Material.SPAWNER;
import static org.bukkit.Material.TRAPPED_CHEST;
import static org.bukkit.Material.WATER;


public class BlockAction extends GenericAction {

    private BlockActionData actionData;

    /**
     * Set the Block.
     *
     * @param block Block
     */
    public void setBlock(Block block) {
        if (block != null) {
            setBlock(block.getState());
        }
    }

    /**
     * Set the Block State.
     *
     * @param state BlockState.
     */
    public void setBlock(BlockState state) {
        if (state != null) {
            setMaterial(state.getType());
            setBlockData(state.getBlockData());
            createActionData(state);
            setLoc(state.getLocation());
        }
    }

    private void createActionData(BlockState state) {
        switch (state.getType()) {
            case SPAWNER:
                final SpawnerActionData spawnerActionData = new SpawnerActionData();
                final CreatureSpawner spawner = (CreatureSpawner) state;
                spawnerActionData.entityType = spawner.getSpawnedType().name().toLowerCase();
                spawnerActionData.delay = spawner.getDelay();
                actionData = spawnerActionData;
                break;
            case PLAYER_WALL_HEAD:
            case PLAYER_HEAD:
                SkullActionData headActionData = new SkullActionData();
                if (state instanceof Skull skull) {
                    if (skull.getOwningPlayer() != null) {
                        headActionData.owner = skull.getOwningPlayer().getUniqueId().toString();
                    }
                }
                setBlockRotation(state, headActionData);
                actionData = headActionData;
                break;
            case SKELETON_SKULL:
            case SKELETON_WALL_SKULL:
            case WITHER_SKELETON_SKULL:
            case WITHER_SKELETON_WALL_SKULL:
                SkullActionData skullActionData = new SkullActionData();
                setBlockRotation(state, skullActionData);
                actionData = skullActionData;
                break;
            case COMMAND_BLOCK:
                final CommandBlock cmdBlock = (CommandBlock) state;
                final CommandActionData commandActionData = new CommandActionData();
                commandActionData.command = cmdBlock.getCommand();
                actionData = commandActionData;
                break;
            default:
                if (Tag.SIGNS.isTagged(state.getType())) {
                    final SignActionData signActionData = new SignActionData();
                    final Sign sign = (Sign) state;
                    signActionData.lines = sign.getLines();
                    actionData = signActionData;
                } else if (Tag.BANNERS.isTagged(state.getType())) {
                    final BannerActionData bannerActionData = new BannerActionData();
                    final Banner banner = (Banner) state;
                    bannerActionData.patterns = new HashMap<>();
                    setBlockRotation(state, bannerActionData);

                    banner.getPatterns().forEach(pattern ->
                            bannerActionData.patterns.put(pattern.getPattern().name(), pattern.getColor().name()));
                    actionData = bannerActionData;
                } else if (Tag.SHULKER_BOXES.isTagged(state.getType())) {
                    InventoryHolder box = (InventoryHolder) state;
                    ShulkerActionData shulkerActionData = new ShulkerActionData();
                    box.getInventory().forEach(itemStack -> {
                        if (itemStack != null && itemStack.getType() != AIR) {
                            ItemStackSerializer serializer = ItemStackSerializer.createItemStackSerialized(itemStack);
                            shulkerActionData.items.add(serializer);
                        }
                    });
                    actionData = shulkerActionData;
                }
                break;
        }
        if (state instanceof Nameable && ((Nameable) state).getCustomName() != null) {
            if (actionData == null) {
                actionData = new BlockActionData();
            }
            actionData.customName = ((Nameable) state).getCustomName();
        }
    }

    private void setBlockRotation(BlockState block, RotatableActionData rotatableActionData) {
        if (block.getBlockData() instanceof final Rotatable r) {
            rotatableActionData.rotation = r.getRotation().toString();
        } else {
            final Directional d = (Directional) block.getBlockData();
            rotatableActionData.rotation = d.getFacing().name().toLowerCase();
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
            if (Tag.BANNERS.isTagged(getMaterial())) {
                actionData = SerializationHelper.gson().fromJson(data, BannerActionData.class);
            } else if (getMaterial() == PLAYER_HEAD || getMaterial() == PLAYER_WALL_HEAD) {
                actionData = SerializationHelper.gson().fromJson(data, SkullActionData.class);
            } else if (getMaterial() == SPAWNER) {
                actionData = SerializationHelper.gson().fromJson(data, SpawnerActionData.class);
            } else if (Tag.SIGNS.isTagged(getMaterial())) {
                actionData = SerializationHelper.gson().fromJson(data, SignActionData.class);
            } else if (getMaterial() == COMMAND_BLOCK) {
                actionData = SerializationHelper.gson().fromJson(data,CommandActionData.class);
            } else if (Tag.SHULKER_BOXES.isTagged(getMaterial())) {
                actionData = SerializationHelper.gson().fromJson(data,ShulkerActionData.class);
            } else {
                actionData = SerializationHelper.gson().fromJson(data, BlockActionData.class);
            }
        }
    }

    private BlockActionData getActionData() {
        return actionData;
    }


    @Override
    public String getNiceName() {
        String name = "";
        BlockActionData blockActionData = getActionData();
        if (blockActionData != null) {
            if (blockActionData instanceof final SkullActionData ad) {
                name += ad.owner + " ";
            } else if (blockActionData instanceof final SpawnerActionData ad) {
                name += ad.entityType + " ";
            }
        }
        name += Prism.getItems().getAlias(getMaterial(), getBlockData());
        if (blockActionData == null) {
            return name;
        }
        if (blockActionData instanceof final SignActionData ad) {
            if (ad.lines != null && ad.lines.length > 0) {
                name += " (" + TypeUtils.join(ad.lines, ", ") + ")";
            }
        } else if (blockActionData instanceof final CommandActionData ad) {
            name += " (" + ad.command + ")";
        }
        if (blockActionData.customName != null) {
            name += ChatColor.RESET + " (" + blockActionData.customName + ChatColor.RESET + ") ";
        }
        if (getAction().getName().equals("crop-trample") && getMaterial() == AIR) {
            return "empty soil";
        }
        return name;
    }

    @Override
    public String getCustomDesc() {
        if (getAction().getName().equals("water-bucket") && getBlockData() instanceof Waterlogged) {
            return "waterlogged";
        }

        return null;
    }

    @Override
    public ChangeResult applyRollback(Player player, PrismParameters parameters, boolean isPreview) {
        final Block block = getWorld().getBlockAt(getLoc());
        if (getAction().doesCreateBlock()) {
            return removeBlock(player, parameters, isPreview, block);
        } else {
            return placeBlock(player, parameters, isPreview, block, false);
        }
    }

    @Override
    public ChangeResult applyRestore(Player player, PrismParameters parameters, boolean isPreview) {
        final Block block = getWorld().getBlockAt(getLoc());
        if (getAction().doesCreateBlock()) {
            return placeBlock(player, parameters, isPreview, block, false);
        } else {
            return removeBlock(player, parameters, isPreview, block);
        }
    }

    @Override
    public ChangeResult applyUndo(Player player, PrismParameters parameters, boolean isPreview) {

        final Block block = getWorld().getBlockAt(getLoc());

        // Undo a drain/ext event (which always remove blocks)
        // @todo if we ever track rollback/restore for undo, we'll
        // need logic to do the opposite
        return placeBlock(player, parameters, isPreview, block, false);

    }

    @Override
    public ChangeResult applyDeferred(Player player, PrismParameters parameters, boolean isPreview) {
        final Block block = getWorld().getBlockAt(getLoc());
        return placeBlock(player, parameters, isPreview, block, true);
    }

    ChangeResult placeBlock(Player player, PrismParameters parameters, boolean isPreview, Block block,
                                boolean isDeferred) {
        BlockStateChangeImpl stateChange;

        // Ensure block action is allowed to place a block here.
        // (essentially liquid/air).

        final boolean cancelIfBadPlace = !getAction().requiresHandler(BlockChangeAction.class)
                && !getAction().requiresHandler(PrismRollbackAction.class) && !parameters.hasFlag(Flags.OVERWRITE);

        if (cancelIfBadPlace && !Utilities.isAcceptableForBlockPlace(block.getType())) {
            PrismLogHandler.debug("Block skipped due to being unacceptable for block place: " + block.getType().name());
            return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
        }

        // On the blacklist (except an undo)
        if ((Prism.getIllegalBlocks().contains(getMaterial())
                && !parameters.getProcessType().equals(PrismProcessType.UNDO))
                && !parameters.hasFlag(Flags.OVERWRITE)) {
            PrismLogHandler.debug("Block skipped because it's not allowed to be placed unless its an UNDO: "
                    + getMaterial().toString());
            return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
        }
        // If we're not in a preview, actually apply this block
        // Capture the block before we change it
        final BlockState originalBlock = block.getState();
        if (!isPreview) {
            return handleApply(block, originalBlock, parameters, cancelIfBadPlace);
        } else {

            // Otherwise, save the state so we can cancel if needed
            // Note: we save the original state as both old/new so we can re-use
            // blockStateChanges
            stateChange = new BlockStateChangeImpl(originalBlock, originalBlock);

            // Preview it
            EntityUtils.sendBlockChange(player, block.getLocation(), getBlockData());

            // Send preview to shared players
            for (final CommandSender sharedPlayer : parameters.getSharedPlayers()) {
                if (sharedPlayer instanceof Player) {
                    EntityUtils.sendBlockChange((Player) sharedPlayer, block.getLocation(), getBlockData());
                }
            }
            return new ChangeResultImpl(ChangeResultType.APPLIED, stateChange);
        }
    }

    /**
     * The BlockState object is modified by this method.
     *
     * @param block            Block
     * @param originalBlock    BlockState
     * @param parameters       QueryParameters
     * @param cancelIfBadPlace cancelIfBadPlace
     * @return ChangeResult.
     */
    private @NotNull ChangeResult handleApply(final Block block, final BlockState originalBlock,
                                                  final PrismParameters parameters, final boolean cancelIfBadPlace) {
        BlockState state = block.getState();
        // If lily pad, check that block below is water. Be sure
        // it's set to stationary water so the lily pad will sit
        switch (getMaterial()) {
            case LILY_PAD -> {
                final Block below = block.getRelative(BlockFace.DOWN);
                if (below.getType().equals(WATER) || below.getType().equals(AIR)) {
                    below.setType(WATER);
                } else {
                    // Prism.debug("Lilypad skipped because no water exists below.");
                    return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
                }
            }
            case NETHER_PORTAL -> { // Only way is to set the portal on fire.
                final Block obsidian = Utilities.getFirstBlockOfMaterialBelow(OBSIDIAN, block.getLocation());
                if (obsidian != null) {
                    final Block above = obsidian.getRelative(BlockFace.UP);
                    if (!(above.getType() == NETHER_PORTAL)) {
                        above.setType(FIRE);
                        return new ChangeResultImpl(ChangeResultType.APPLIED, null);
                    }
                }
            }
            case JUKEBOX -> setBlockData(Bukkit.createBlockData(JUKEBOX));
            default -> {
            }
        }
        state.setType(getMaterial());
        state.setBlockData(getBlockData());
        state.update(true);
        BlockState newState = block.getState();
        BlockActionData blockActionData = getActionData();
        if (blockActionData != null) {
            if ((getMaterial() == PLAYER_HEAD || getMaterial() == PLAYER_WALL_HEAD)
                    && blockActionData instanceof SkullActionData) {
                return handleSkulls(block, blockActionData, originalBlock);
            }
            if (Tag.SHULKER_BOXES.isTagged(getMaterial()) && blockActionData instanceof ShulkerActionData) {
                return handleShulkers(block, (ShulkerActionData) blockActionData,originalBlock);
            }
            if (Tag.BANNERS.isTagged(getMaterial()) && blockActionData instanceof BannerActionData) {
                return handleBanners(block, blockActionData, originalBlock);
            }
            if (getMaterial() == SPAWNER && blockActionData instanceof final SpawnerActionData s) {

                // Set spawner data
                ((CreatureSpawner) newState).setDelay(s.getDelay());
                ((CreatureSpawner) newState).setSpawnedType(s.getEntityType());

            }

            if (getMaterial() == COMMAND_BLOCK
                    && blockActionData instanceof final CommandActionData c) {
                ((CommandBlock) newState).setCommand(c.command);
            }
            if (newState instanceof Nameable && blockActionData.customName != null
                    && !blockActionData.customName.equals("")) {
                ((Nameable) newState).setCustomName(blockActionData.customName);
            }
            if (parameters.getProcessType() == PrismProcessType.ROLLBACK
                    && Tag.SIGNS.isTagged(getMaterial())
                    && blockActionData instanceof final SignActionData s) {

                // Verify block is sign. Rarely, if the block somehow pops off
                // or fails
                // to set it causes ClassCastException:
                // org.bukkit.craftbukkit.v1_4_R1.block.CraftBlockState
                // cannot be cast to org.bukkit.block.Sign
                // https://snowy-evening.com/botsko/prism/455/
                if (newState instanceof Sign) {
                    if (s.lines != null) {
                        for (int i = 0; i < s.lines.length; ++i) {
                            ((Sign) newState).setLine(i, s.lines[i]);
                        }
                    }
                }
            }
        } else {
            PrismLogHandler.debug("BlockAction Data was null with " + parameters.toString());
        }
        // -----------------------------
        // Sibling logic marker

        // If the material is a crop that needs soil, we must restore the soil
        // This may need to go before setting the block, but I prefer the BlockUtil logic to use materials.
        BlockState sibling = null;

        if (Utilities.materialRequiresSoil(getMaterial())) {
            sibling = block.getRelative(BlockFace.DOWN).getState();

            if (cancelIfBadPlace && !MaterialTag.SOIL_CANDIDATES.isTagged(sibling.getType())) {
                PrismLogHandler.debug(parameters.getProcessType().name() + " skipped due to lack of soil for "
                        + getMaterial().name());
                return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
            }
            sibling.setType(FARMLAND);
        }

        // Chest sides can be broken independently, ignore them
        if (newState.getType() != CHEST && newState.getType() != TRAPPED_CHEST) {
            final Block s = Utilities.getSiblingForDoubleLengthBlock(state);

            if (s != null) {
                sibling = s.getState();

                if (cancelIfBadPlace && !Utilities.isAcceptableForBlockPlace(sibling.getType())) {
                    PrismLogHandler.debug(parameters.getProcessType().name()
                            + " skipped due to lack of wrong sibling type for "
                            + getMaterial().name());
                    return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
                }

                sibling.setType(block.getType());

                BlockData siblingData = getBlockData().clone();

                if (siblingData instanceof Bed) {
                    // We always log the foot
                    ((Bed) siblingData).setPart(Part.HEAD);
                } else if (siblingData instanceof Bisected) {
                    // We always log the bottom
                    ((Bisected) siblingData).setHalf(Half.TOP);
                }

                sibling.setBlockData(siblingData);
            }
        }

        boolean physics = !parameters.hasFlag(Flags.NO_PHYS);

        newState.update(true, physics);

        if (sibling != null) {
            sibling.update(true, physics);
        }
        return new ChangeResultImpl(ChangeResultType.APPLIED, new BlockStateChangeImpl(originalBlock, state));
    }

    private ChangeResultImpl handleShulkers(Block block, ShulkerActionData blockActionData, BlockState originalBlock) {
        block.setType(getMaterial());
        ShulkerBox state = (ShulkerBox) block.getState();
        if (blockActionData.items.size() > 0) {
            blockActionData.items.forEach(itemStackSerializer -> {
                if (itemStackSerializer != null) {
                    state.getInventory().addItem(itemStackSerializer.toBukkit());
                }
            });
        }
        state.update();
        return new ChangeResultImpl(ChangeResultType.APPLIED,new BlockStateChangeImpl(originalBlock,state));

    }

    private ChangeResultImpl handleBanners(Block block, BlockActionData blockActionData, BlockState originalBlock) {
        block.setType(getMaterial());
        BlockState state = block.getState();
        final BannerActionData actionData = (BannerActionData) blockActionData;
        setBlockRotatable(state, actionData);
        state = block.getState();
        if (!actionData.patterns.isEmpty()) {
            final Banner banner = (Banner) state;
            List<Pattern> patternsList = new ArrayList<>();
            actionData.patterns.forEach((s, s2) -> {
                PatternType type = PatternType.valueOf(s);
                DyeColor color = DyeColor.valueOf(s2.toUpperCase());
                Pattern p = new Pattern(color, type);
                patternsList.add(p);
            });
            banner.setPatterns(patternsList);
            banner.update();
        }
        BlockStateChangeImpl stateChange = new BlockStateChangeImpl(originalBlock, state);
        return new ChangeResultImpl(ChangeResultType.APPLIED, stateChange);
    }

    private void setBlockRotatable(BlockState state, RotatableActionData actionData) {
        if (state.getBlockData() instanceof final Rotatable r) {
            r.setRotation(actionData.getRotation());
            state.setBlockData(r);
        } else {
            final Directional d = (Directional) state.getBlockData();
            d.setFacing(actionData.getRotation());
            state.setBlockData(d);
        }
        state.update();
    }

    private @NotNull ChangeResultImpl handleSkulls(final Block block, BlockActionData blockActionData,
                                                   final BlockState originalBlock) {
        block.setType(getMaterial());
        BlockState state = block.getState();
        final SkullActionData s = (SkullActionData) blockActionData;
        setBlockRotatable(state, s);
        state = block.getState();

        if (!s.owner.isEmpty()) {
            final Skull skull = (Skull) state;
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(EntityUtils.uuidOf((s.owner))));
        }
        BlockStateChangeImpl stateChange = new BlockStateChangeImpl(originalBlock, state);
        return new ChangeResultImpl(ChangeResultType.APPLIED, stateChange);
    }

    private ChangeResult removeBlock(Player player, PrismParameters parameters, boolean isPreview, Block block) {

        BlockStateChangeImpl stateChange;

        if (!block.getType().equals(AIR)) {

            // Ensure it's acceptable to remove the current block
            if (!Utilities.isAcceptableForBlockPlace(block.getType())
                    && !Utilities.areBlockIdsSameCoreItem(block.getType(), getMaterial())
                    && !parameters.hasFlag(Flags.OVERWRITE)) {
                return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
            }
            // Capture the block before we change it

            final BlockState originalBlock = block.getState();
            if (!isPreview) {
                // Set
                block.setType(AIR);
                // Capture the new state
                final BlockState newBlock = block.getState();
                // Store the state change
                stateChange = new BlockStateChangeImpl(originalBlock, newBlock);
            } else {
                // Otherwise, save the state so we can cancel if needed
                // Note: we save the original state as both old/new so we can
                // re-use blockStateChanges
                stateChange = new BlockStateChangeImpl(originalBlock, originalBlock);

                // Preview it
                EntityUtils.sendBlockChange(player, block.getLocation(), Bukkit.createBlockData(AIR));

                // Send preview to shared players
                for (final CommandSender sharedPlayer : parameters.getSharedPlayers()) {
                    if (sharedPlayer instanceof Player) {
                        EntityUtils.sendBlockChange((Player) sharedPlayer, block.getLocation(),
                                Bukkit.createBlockData(AIR));
                    }
                }
            }
            return new ChangeResultImpl(ChangeResultType.APPLIED, stateChange);
        }
        return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
    }

    /**
     * BlockActionData.
     *
     * @author botskonet
     */
    static class BlockActionData {
        String customName = "";
    }

    public static class CommandActionData extends BlockActionData {
        String command;
    }

    /**
     * Spawner ActionData.
     *
     * @author botskonet
     */
    public static class SpawnerActionData extends BlockActionData {

        String entityType;
        int delay;

        EntityType getEntityType() {
            return EntityType.valueOf(entityType.toUpperCase());
        }

        int getDelay() {
            return delay;
        }
    }

    public static class RotatableActionData extends BlockActionData {
        String rotation;

        BlockFace getRotation() {
            if (rotation != null) {
                return BlockFace.valueOf(rotation.toUpperCase());
            }
            return null;
        }

    }

    /**
     * SkullActionData.
     *
     * @author botskonet
     */
    public static class SkullActionData extends RotatableActionData {

        String owner;
        String skullType;

    }

    /**
     * Not to be confused with SignChangeActionData, which records additional data
     * we don't need here.
     *
     * @author botskonet
     */
    public static class SignActionData extends BlockActionData {
        String[] lines;
    }

    public static class BannerActionData extends RotatableActionData {
        Map<String, String> patterns;
    }

    public static class ShulkerActionData extends BlockActionData {
        final Collection<ItemStackSerializer> items = new ArrayList<>();
    }

}