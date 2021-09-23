package me.botsko.prism.listeners;

import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.actions.BlockAction;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.utils.InventoryUtils;
import me.botsko.prism.utils.MaterialTag;
import me.botsko.prism.utils.MiscUtils;
import me.botsko.prism.wands.ProfileWand;
import me.botsko.prism.wands.Wand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class PrismPlayerEvents implements Listener {

    private final Prism plugin;
    private final List<String> illegalCommands;
    private final List<String> ignoreCommands;

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public PrismPlayerEvents(Prism plugin) {
        this.plugin = plugin;
        illegalCommands = plugin.config.alertConfig.illegalCommands.illegalCommands;
        ignoreCommands = plugin.config.doNotTrackCommands;
    }

    /**
     * Log command use.
     *
     * @param event PlayerCommandPreprocessEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {

        final Player player = event.getPlayer();
        final String cmd = event.getMessage().toLowerCase();

        final String[] cmdArgs = cmd.split(" ");
        final String primaryCmd = cmdArgs[0].substring(1);

        if (plugin.config.alertConfig.illegalCommands.enabled) {
            if (illegalCommands.contains(primaryCmd)) {
                final String msg = player.getName() + " attempted an illegal command: " + primaryCmd + ". Originally: "
                        + cmd;
                TextComponent send = Component.text(msg);
                Prism.messenger.sendMessage(player,
                        Prism.messenger.playerError("Sorry, this command is not available in-game."));
                plugin.alertPlayers(null, send, null);
                event.setCancelled(true);
                // Log to console
                if (plugin.config.alertConfig.illegalCommands.logToConsole) {
                    PrismLogHandler.log(msg);
                }

                // Log to commands
                List<String> commands = plugin.config.alertConfig.illegalCommands.logCommands;
                MiscUtils.dispatchAlert(msg, commands);
            }
        }

        if (!Prism.getIgnore().event(ActionType.PLAYER_COMMAND, player)) {
            return;
        }

        // Ignore some commands based on config
        if (ignoreCommands.contains(primaryCmd)) {
            return;
        }

        RecordingQueue.addToQueue(ActionFactory.createPlayer(ActionType.PLAYER_COMMAND, player, event.getMessage()));

    }

    /**
     * PlayerJoinEvent.
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {

        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        final String name = player.getName();
        final boolean trackIp = plugin.config.trackingConfig.playerIpOnJoin;
        final boolean doNotTrackJoin = !Prism.getIgnore().event(ActionType.PLAYER_JOIN, player);
        Bukkit.getScheduler().runTaskAsynchronously(Prism.getInstance(), () -> {
            // Lookup player for cache reasons
            Prism.getInstance().getPlayerIdentifier().cachePrismPlayer(uuid, name);
            Bukkit.getScheduler().runTask(Prism.getInstance(), () -> {
                if (doNotTrackJoin) {
                    return;
                }
                String ip = null;
                if (trackIp && player.getAddress() != null) { //player may have disconnected.
                    ip = player.getAddress().getAddress().getHostAddress();
                }
                RecordingQueue.addToQueue(ActionFactory.createPlayer(ActionType.PLAYER_JOIN, player, ip));
            });
        });

    }

    /**
     * PlayerQuitEvent.
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {

        // Remove from primary key cache
        plugin.getPlayerIdentifier().getPrismPlayers().remove(event.getPlayer().getUniqueId());

        // Track player quit
        if (!Prism.getIgnore().event(ActionType.PLAYER_QUIT, event.getPlayer())) {
            return;
        }

        RecordingQueue.addToQueue(ActionFactory.createPlayer(ActionType.PLAYER_QUIT, event.getPlayer(), null));

        // Remove any active wands for this player
        Prism.playersWithActiveTools.remove(event.getPlayer().getName());
        // Remove any active previews for this player, even though they would
        // expire
        // naturally.
        plugin.playerActivePreviews.remove(event.getPlayer().getName());
    }


    /**
     * PlayerDropItemEvent.
     *
     * @param event PlayerDropItemEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (!Prism.getIgnore().event(ActionType.ITEM_DROP, event.getPlayer())) {
            return;
        }
        if (event.getItemDrop().getItemStack().getType() == Material.AIR) {
            return;
        }
        RecordingQueue.addToQueue(ActionFactory.createItemStack(ActionType.ITEM_DROP,
                event.getItemDrop().getItemStack(),event.getItemDrop().getItemStack().getAmount(), -1,
                null, event.getPlayer().getLocation(),event.getPlayer()));
    }

    /**
     * Track players changing game modes.
     *
     * @param event PlayerGameModeChangeEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeSwitch(PlayerGameModeChangeEvent event) {
        if (!Prism.getIgnore().event(ActionType.PLAYER_GAMEMODECHANGE, event.getPlayer())) {
            return;
        }
        RecordingQueue.addToQueue(ActionFactory.createPlayer(ActionType.PLAYER_GAMEMODECHANGE,
                event.getPlayer(), event.getNewGameMode().toString()));
    }

    /**
     * EntityPickupItemEvent.
     *
     * @param event EntityPickupItemEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupItem(final EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player p) {
            if (!Prism.getIgnore().event(ActionType.ITEM_PICKUP, p)) {
                return;
            }
            RecordingQueue.addToQueue(ActionFactory.createItemStack(ActionType.ITEM_PICKUP,
                    event.getItem().getItemStack(), event.getItem().getItemStack().getAmount(), -1,
                    null, p.getLocation(), p));
        }
    }

    /**
     * PlayerExpChangeEvent.
     *
     * @param event PlayerExpChangeEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerExpChangeEvent(final PlayerExpChangeEvent event) {
        if (!Prism.getIgnore().event(ActionType.XP_PICKUP, event.getPlayer())) {
            return;
        }
        RecordingQueue.addToQueue(ActionFactory.createPlayer(ActionType.XP_PICKUP, event.getPlayer(), ""
                + event.getAmount()));
    }

    /**
     * PlayerBucketEmptyEvent.
     *
     * @param event PlayerBucketEmptyEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {

        final Player player = event.getPlayer();
        ActionType cause;
        Material newMat;
        Block spot = event.getBlockClicked().getRelative(event.getBlockFace());
        switch (event.getBucket()) {
            case LAVA_BUCKET -> {
                cause = ActionType.LAVA_BUCKET;
                newMat = Material.LAVA;
            }
            default -> {
                cause = ActionType.WATER_BUCKET;
                newMat = Material.WATER;
            }
        }

        if (!Prism.getIgnore().event(cause, player)) {
            return;
        }

        BlockData oldData = spot.getBlockData();
        BlockData newData = Bukkit.createBlockData(newMat);

        BlockData clickedData = event.getBlockClicked().getBlockData();

        // TODO If "Lavalogged" blocks become a thing, please revisit.
        if (clickedData instanceof Waterlogged wl && event.getBucket() != Material.LAVA) {

            if (!wl.isWaterlogged()) {
                spot = event.getBlockClicked();
                newMat = spot.getType();
                oldData = wl;

                newData = wl.clone();
                ((Waterlogged) newData).setWaterlogged(true);

                cause = ActionType.WATER_BUCKET;
            }
        }

        RecordingQueue.addToQueue(ActionFactory.createBlockChange(cause, spot.getLocation(),
                spot.getType(), oldData, newMat, newData, player));

        if (plugin.config.alertConfig.uses.lava && event.getBucket() == Material.LAVA_BUCKET
                && !player.hasPermission("prism.alerts.use.lavabucket.ignore")
                && !player.hasPermission("prism.alerts.ignore")) {
            plugin.useMonitor.alertOnItemUse(player, "poured lava", "prism.alerts.use.lavabucket");
        }
    }

    /**
     * PlayerBucketFillEvent.
     *
     * @param event PlayerBucketFillEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(final PlayerBucketFillEvent event) {

        final Player player = event.getPlayer();
        if (!Prism.getIgnore().event(ActionType.BUCKET_FILL, player)) {
            return;
        }
        final Block spot = event.getBlock();

        String liquidType = "milk";
        if (spot.getType() == Material.WATER) {
            liquidType = "water";
        } else if (spot.getBlockData() instanceof Waterlogged && ((Waterlogged) spot.getBlockData()).isWaterlogged()) {
            liquidType = "water";
        } else if (spot.getType() == Material.LAVA) {
            liquidType = "lava";
        }

        final Handler pa = ActionFactory.createPlayer(ActionType.BUCKET_FILL, player, liquidType);

        // Override the location with the area taken
        pa.setX(spot.getX());
        pa.setY(spot.getY());
        pa.setZ(spot.getZ());

        RecordingQueue.addToQueue(pa);

    }

    /**
     * PlayerTeleportEvent.
     *
     * @param event PlayerTeleportEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        if (!Prism.getIgnore().event(ActionType.PLAYER_TELEPORT, event.getPlayer())) {
            return;
        }
        final TeleportCause c = event.getCause();
        if (c.equals(TeleportCause.END_PORTAL) || c.equals(TeleportCause.NETHER_PORTAL)
                || c.equals(TeleportCause.ENDER_PEARL)) {
            RecordingQueue.addToQueue(ActionFactory.createEntityTravel(ActionType.PLAYER_TELEPORT, event.getPlayer(),
                    event.getFrom(), event.getTo(), event.getCause()));
        }
    }


    /**
     * PlayerInteractEvent.
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {

        final Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        ItemStack hand = player.getInventory().getItemInMainHand();

        final Wand wand = Prism.playersWithActiveTools.get(player.getName());
        // Are they using a wand (or do we always allow it)
        if (wand != null) {

            // The wand will tell us what to use.
            final Material item_mat = wand.getItem();

            // Does the player have such item?
            if (hand.getType() == item_mat) {

                // Left click is for current block
                if ((event.getAction() == Action.LEFT_CLICK_BLOCK) && (event.getHand() == EquipmentSlot.HAND)
                        && (block != null)) {
                    wand.playerLeftClick(player, block.getLocation());
                }
                // Right click is for relative block on blockface
                // except block placements - those will be handled by the
                // blockplace.
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND
                        && block != null) {
                    block = block.getRelative(event.getBlockFace());
                    wand.playerRightClick(player, block.getLocation());
                }

                if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                    PrismLogHandler.debug("Cancelling event for wand use.");
                    event.setCancelled(true);
                    InventoryUtils.updateInventory(player);
                    return;
                }
            }
        }

        if (event.useInteractedBlock() == Result.DENY) {
            return;
        }

        // Doors, buttons, containers, etc may only be opened with a right-click
        // as of 1.4
        if (block != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            String coordKey;
            if (MaterialTag.CONTAINERS.isTagged(block.getType())) {
                if (!Prism.getIgnore().event(ActionType.CONTAINER_ACCESS, player)) {
                    return;
                }
                RecordingQueue.addToQueue(ActionFactory.createBlock(ActionType.CONTAINER_ACCESS, block, player));
            } else if (MaterialTag.USABLE.isTagged(block.getType())) {
                if (!Prism.getIgnore().event(ActionType.BLOCK_USE, player)) {
                    return;
                }
                RecordingQueue.addToQueue(ActionFactory.createBlock(ActionType.BLOCK_USE, block, player));
            } else if (MaterialTag.GROWABLE.isTagged(block.getType())) {
                recordBoneMealEvent(block, hand, player);
            } else {
                switch (block.getType()) {
                    case JUKEBOX:
                        recordDiscInsert(block, player);
                        break;
                    case CAKE:
                        recordCakeEat(block, player);
                        break;
                    case JUNGLE_LOG:
                        recordCocoaPlantEvent(block, hand, event.getBlockFace(), player);
                        break;
                    case RAIL:
                    case DETECTOR_RAIL:
                    case POWERED_RAIL:
                    case ACTIVATOR_RAIL:
                        coordKey = block.getX() + ":" + block.getY() + ":" + block.getZ();
                        plugin.preplannedVehiclePlacement.put(coordKey, player.getUniqueId().toString());
                        break;
                    case TNT:
                        if (hand.getType().equals(Material.FLINT_AND_STEEL)) {
                            if (!Prism.getIgnore().event(ActionType.TNT_PRIME, player)) {
                                return;
                            }
                            RecordingQueue.addToQueue(ActionFactory.createUse(ActionType.TNT_PRIME,
                                    hand.getType(), block, player));
                        }
                        break;
                    default:
                        break;
                }
            }
            // if they're holding a spawner egg
            if (MaterialTag.SPAWN_EGGS.isTagged(hand.getType())) {
                recordMonsterEggUse(block, hand, player);
            }

            // if they're holding a rocket
            if (hand.getType() == Material.FIREWORK_ROCKET) {
                recordRocketLaunch(block, hand, player);
            }

            // if they're holding a boat (why they hell can you put boats on
            // anything...)
            if (MaterialTag.BOATS.isTagged(hand.getType())) {
                coordKey = block.getX() + ":" + (block.getY() + 1) + ":" + block.getZ();
                plugin.preplannedVehiclePlacement.put(coordKey, player.getUniqueId().toString());
            }
        }

        // Punching fire
        if (block != null && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            final Block above = block.getRelative(BlockFace.UP);
            if (above.getType().equals(Material.FIRE)) {
                RecordingQueue.addToQueue(ActionFactory.createBlock(ActionType.BLOCK_BREAK, above, player));
            }
        }

        if (!plugin.config.trackingConfig.trackers.get(ActionType.CROP_TRAMPLE)) {
            return;
        }

        if (block != null && event.getAction() == Action.PHYSICAL) {
            if (block.getType() == Material.FARMLAND) { // They are stepping on
                // soil
                if (!Prism.getIgnore().event(ActionType.CROP_TRAMPLE, player)) {
                    return;
                }
                RecordingQueue
                        .addToQueue(ActionFactory.createBlock(ActionType.CROP_TRAMPLE, block.getRelative(BlockFace.UP),
                                player));
            }
        }
    }

    /**
     * Record Cocoa Plant Event.
     *
     * @param block  Block
     * @param inhand ItemStack
     * @param player Player
     */
    private void recordCocoaPlantEvent(Block block, ItemStack inhand, BlockFace clickedFace, Player player) {
        if (!Prism.getIgnore().event(ActionType.BLOCK_PLACE, block)) {
            return;
        }
        if (block.getType() == Material.JUNGLE_LOG && inhand.getType() == Material.COCOA_BEANS) {
            final Location newLoc = block.getRelative(clickedFace).getLocation();
            final Block actualBlock = block.getWorld().getBlockAt(newLoc);
            // This is a lame way to do this
            final BlockAction action = new BlockAction();
            action.setActionType(ActionType.BLOCK_PLACE);
            action.setPlayer(player);
            action.setUuid(player.getUniqueId());
            action.setLoc(actualBlock.getLocation());
            action.setMaterial(Material.COCOA);
            action.setBlockData(block.getBlockData());
            RecordingQueue.addToQueue(action);
        }
    }

    /**
     * recordBoneMealEvent.
     *
     * @param block  Block
     * @param inhand ItemStack
     * @param player Player
     */
    private void recordBoneMealEvent(Block block, ItemStack inhand, Player player) {
        if (inhand.getType() == Material.BONE_MEAL) {
            if (!Prism.getIgnore().event(ActionType.BONEMEAL_USE, block)) {
                return;
            }
            RecordingQueue.addToQueue(ActionFactory.createUse(ActionType.BONEMEAL_USE, inhand.getType(), block,
                    player));
        }
    }

    /**
     * recordMonsterEggUse.
     *
     * @param block  Block
     * @param inhand ItemStack
     * @param player Player
     */
    private void recordMonsterEggUse(Block block, ItemStack inhand, Player player) {
        if (!Prism.getIgnore().event(ActionType.SPAWNEGG_USE, block)) {
            return;
        }
        RecordingQueue.addToQueue(ActionFactory.createUse(ActionType.SPAWNEGG_USE,
                inhand.getType(), block, player));
    }

    /**
     * recordRocketLaunch.
     *
     * @param block  Block
     * @param inhand ItemStack
     * @param player Player
     */
    private void recordRocketLaunch(Block block, ItemStack inhand, Player player) {
        if (!Prism.getIgnore().event(ActionType.FIREWORK_LAUNCH, block)) {
            return;
        }
        RecordingQueue.addToQueue(
                ActionFactory.createItemStack(ActionType.FIREWORK_LAUNCH, inhand, null, block.getLocation(), player));
    }

    /**
     * recordCakeEat.
     *
     * @param block  Block
     * @param player Player
     */
    private void recordCakeEat(Block block, Player player) {
        if (!Prism.getIgnore().event(ActionType.CAKE_EAT, block)) {
            return;
        }
        RecordingQueue.addToQueue(ActionFactory.createUse(ActionType.CAKE_EAT, Material.CAKE, block, player));
    }

    /**
     * recordDiscInsert.
     *
     * @param block  Block
     * @param player Player
     */
    private void recordDiscInsert(Block block, Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        // They have to be holding a record
        if (!hand.getType().isRecord()) {
            return;
        }

        final Jukebox jukebox = (Jukebox) block.getState();

        // Do we have a disc inside? This will pop it out
        if (!jukebox.getPlaying().equals(Material.AIR)) {

            // Record currently playing disc
            final ItemStack i = new ItemStack(jukebox.getPlaying(), 1);
            RecordingQueue.addToQueue(ActionFactory.createItemStack(ActionType.ITEM_REMOVE, i,
                    i.getAmount(), 0, null,block.getLocation(), player));

        } else {

            // Record the insert
            RecordingQueue.addToQueue(
                    ActionFactory.createItemStack(ActionType.ITEM_INSERT, hand, 1, 0, null,
                            block.getLocation(), player));

        }
    }

    /**
     * PlayerInteractEntityEvent.
     *
     * @param event PlayerInteractEntityEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerEntityInteract(final PlayerInteractEntityEvent event) {

        final Player player = event.getPlayer();
        final Entity entity = event.getRightClicked();

        // Are they using a wand?
        if (Prism.playersWithActiveTools.containsKey(player.getName())) {

            // Pull the wand in use
            final Wand wand = Prism.playersWithActiveTools.get(player.getName());
            if (wand instanceof ProfileWand) {

                wand.playerRightClick(player, entity);

                // Always cancel
                event.setCancelled(true);

            }
        }
    }
}
