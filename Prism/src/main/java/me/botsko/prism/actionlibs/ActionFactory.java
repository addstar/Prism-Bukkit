package me.botsko.prism.actionlibs;

import me.botsko.prism.actions.BlockAction;
import me.botsko.prism.actions.BlockChangeAction;
import me.botsko.prism.actions.BlockShiftAction;
import me.botsko.prism.actions.EntityAction;
import me.botsko.prism.actions.EntityTravelAction;
import me.botsko.prism.actions.GrowAction;
import me.botsko.prism.actions.HangingItemAction;
import me.botsko.prism.actions.ItemStackAction;
import me.botsko.prism.actions.PlayerAction;
import me.botsko.prism.actions.PlayerDeathAction;
import me.botsko.prism.actions.PrismProcessAction;
import me.botsko.prism.actions.PrismRollbackAction;
import me.botsko.prism.actions.SignAction;
import me.botsko.prism.actions.UseAction;
import me.botsko.prism.actions.VehicleAction;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.api.actions.PrismProcessType;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Map;

public class ActionFactory {

    /**
     * BlockAction.
     *
     * @param actionType the action
     * @param block      the block
     * @param player     Offline Player
     */
    public static Handler createBlock(ActionType actionType, Block block, AnimalTamer player) {
        final BlockAction a = new BlockAction();
        a.setActionType(actionType);
        a.setBlock(block);
        a.setPlayer(player);
        return a;
    }

    /**
     * Create Handler.
     *
     * @param actionType type
     * @param block      block
     * @param nonPlayer  not a player
     * @return Handler
     */
    public static Handler createBlock(ActionType actionType, Block block, String nonPlayer) {
        final Handler a = createBlock(actionType, block, (OfflinePlayer) null);
        a.setSourceName(nonPlayer);
        return a;
    }

    /**
     * BlockAction.
     *
     * @param actionType the action
     * @param state      the block state
     * @param player     Offline Player
     */
    @SuppressWarnings("WeakerAccess")
    public static Handler createBlock(ActionType actionType, BlockState state, AnimalTamer player) {
        final BlockAction a = new BlockAction();
        a.setActionType(actionType);
        a.setBlock(state);
        a.setPlayer(player);
        return a;
    }

    /**
     * Create Handler.
     *
     * @param actionType type
     * @param block      block
     * @param nonPlayer  not a player
     * @return Handler
     */
    public static Handler createBlock(ActionType actionType, BlockState block, String nonPlayer) {
        final Handler a = createBlock(actionType, block, (OfflinePlayer) null);
        a.setSourceName(nonPlayer);
        return a;
    }

    /**
     * BlockChangeAction | WorldEditAction.
     *
     * @param actionType the action
     * @param player     Offline Player
     */
    public static Handler createBlockChange(ActionType actionType, Location loc, Material oldMat, BlockData oldData,
                                            Material newMat, BlockData newData, AnimalTamer player) {
        final BlockChangeAction a = new BlockChangeAction();
        a.setActionType(actionType);
        a.setMaterial(newMat);
        a.setBlockData(newData);
        a.setOldMaterial(oldMat);
        a.setOldBlockData(oldData);
        a.setPlayer(player);
        a.setLoc(loc);
        return a;
    }

    /**
     * Handles Spread, Fade and Form events.
     *
     * @param actionType type
     * @param loc        Location
     * @param oldMat     old
     * @param oldData    old data
     * @param newMat     new
     * @param newData    new data
     * @param nonPlayer  non player.
     * @return Handler.
     */
    public static Handler createBlockChange(ActionType actionType, Location loc, Material oldMat, BlockData oldData,
                                            Material newMat, BlockData newData, String nonPlayer) {
        final Handler a = createBlockChange(actionType, loc, oldMat, oldData, newMat, newData, (OfflinePlayer) null);
        a.setSourceName(nonPlayer);
        return a;
    }

    /**
     * BlockShiftAction.
     *
     * @param actionType the action
     */
    public static Handler createBlockShift(ActionType actionType, Block from, Location to, String nonPlayer) {
        final BlockShiftAction a = new BlockShiftAction();
        a.setActionType(actionType);
        a.setBlock(from);
        a.setSourceName(nonPlayer);
        a.setLoc(to);
        return a;
    }

    /**
     * EntityAction.
     *
     * @param actionType the action
     * @param player     the acting player
     */
    public static Handler createEntity(ActionType actionType, Entity entity, OfflinePlayer player) {
        return ActionFactory.createEntity(actionType, entity, player, null);
    }

    /**
     * Entity Handler.
     *
     * @param actionType type
     * @param entity     entity
     * @param nonPlayer  not a player
     * @return Handler
     */
    public static Handler createEntity(ActionType actionType, Entity entity, String nonPlayer) {
        return ActionFactory.createEntity(actionType, entity, nonPlayer, null);
    }

    /**
     * Entity Handler.
     *
     * @param actionType type
     * @param entity     entity
     * @param player     player
     * @param dyeUsed    string
     * @return Handler
     */
    public static Handler createEntity(ActionType actionType, Entity entity, AnimalTamer player, DyeColor dyeUsed) {
        final EntityAction a = new EntityAction();
        a.setActionType(actionType);
        a.setPlayer(player);
        a.setEntity(entity, dyeUsed);
        return a;
    }

    /**
     * Entity Handler.
     *
     * @param actionType type
     * @param entity     entity
     * @param nonPlayer  nonPlayer
     * @param dyeUsed    string
     * @return Handler
     */
    @SuppressWarnings("WeakerAccess")
    public static Handler createEntity(ActionType actionType, Entity entity, String nonPlayer, DyeColor dyeUsed) {
        final Handler a = createEntity(actionType, entity, (OfflinePlayer) null, dyeUsed);
        a.setSourceName(nonPlayer);
        return a;
    }

    /**
     * EntityTravel.
     *
     * @param actionType type
     * @param entity     entity
     * @param from       location
     * @param to         location
     * @param cause      Teleport cause
     * @return Handler
     */
    public static Handler createEntityTravel(ActionType actionType, Entity entity, Location from, Location to,
                                             TeleportCause cause) {
        final EntityTravelAction a = new EntityTravelAction();
        a.setEntity(entity);
        a.setActionType(actionType);
        a.setLoc(from);
        a.setToLocation(to);
        a.setCause(cause);
        return a;
    }

    /**
     * GrowAction.
     *
     * @param actionType the action
     * @param player     the player
     */
    public static Handler createGrow(ActionType actionType, BlockState blockstate, AnimalTamer player) {
        final GrowAction a = new GrowAction();
        a.setActionType(actionType);
        a.setBlock(blockstate);
        a.setPlayer(player);
        return a;
    }

    /**
     * GrowHandler.
     *
     * @param actionType type
     * @param blockState BlockState
     * @param nonPlayer  non player
     * @return Handler
     */
    public static Handler createGrow(ActionType actionType, BlockState blockState, String nonPlayer) {
        final Handler a = createGrow(actionType, blockState, (OfflinePlayer) null);
        a.setSourceName(nonPlayer);
        return a;
    }

    /**
     * HangingItemAction.
     *
     * @param actionType type
     * @param hanging    hanging
     * @param player     player
     * @return Handler
     */
    public static Handler createHangingItem(ActionType actionType, Hanging hanging, AnimalTamer player) {
        final HangingItemAction a = new HangingItemAction();
        a.setActionType(actionType);
        a.setHanging(hanging);
        a.setPlayer(player);
        return a;
    }

    /**
     * HangingItemAction.
     *
     * @param actionType type
     * @param hanging    hanging
     * @param nonPlayer  nonPlayer
     * @return Handler
     */
    public static Handler createHangingItem(ActionType actionType, Hanging hanging, String nonPlayer) {
        final Handler a = createHangingItem(actionType, hanging, (OfflinePlayer) null);
        a.setSourceName(nonPlayer);
        return a;
    }

    /**
     * ItemStack Handler for item enchanting.
     *
     * @param actionType   String
     * @param item         Item
     * @param enchantments map
     * @param loc          Location
     * @param player       OfflinePlayer
     * @return handler
     */
    public static Handler createItemStack(ActionType actionType, ItemStack item, Map<Enchantment, Integer> enchantments,
                                          Location loc, OfflinePlayer player) {
        return ActionFactory.createItemStack(actionType, item, 1, -1, enchantments, loc, player);
    }

    /**
     * ItemStack Handler.
     *
     * @param actionType   String
     * @param item         Item
     * @param quantity     int
     * @param slot         int
     * @param enchantments map
     * @param loc          Location
     * @param player       Player
     * @return handler
     */
    public static Handler createItemStack(ActionType actionType, ItemStack item, int quantity, int slot,
                                          Map<Enchantment, Integer> enchantments, Location loc, OfflinePlayer player) {
        final ItemStackAction a = createItemStack(actionType, item, quantity, enchantments, loc, player);
        a.setSlot(String.valueOf(slot));

        return a;
    }

    /**
     * Handles a block being dropped or dispensed.
     *
     * @param actionType   type
     * @param item         item
     * @param quantity     qty
     * @param slot         slot it was in
     * @param enchantments Map of enchants
     * @param loc          Location
     * @param sourceName   Source of the item.
     * @return Handler
     */
    public static Handler createItemStack(ActionType actionType, ItemStack item, int quantity, int slot,
                                          Map<Enchantment, Integer> enchantments,
                                          Location loc, String sourceName) {
        final ItemStackAction a = new ItemStackAction();
        a.setActionType(actionType);
        a.setLoc(loc);
        a.setSourceName(sourceName);
        a.setItem(item, quantity, enchantments);
        a.setSlot(String.valueOf(slot));
        return a;
    }

    /**
     * Handles Item being removed or inserted.
     *
     * @param actionType   type
     * @param item         item
     * @param quantity     qty
     * @param slot         slot of inventory
     * @param enchantments map
     * @param loc          Location
     * @param player       Player
     * @return handler
     */
    public static Handler createItemStack(ActionType actionType, ItemStack item, int quantity, EquipmentSlot slot,
                                          Map<Enchantment, Integer> enchantments, Location loc, OfflinePlayer player) {
        final ItemStackAction a = createItemStack(actionType, item, quantity, enchantments, loc, player);
        a.setSlot(slot.name().toLowerCase(Locale.ENGLISH));

        return a;
    }

    private static ItemStackAction createItemStack(ActionType actionType, ItemStack item, int quantity,
                                                   Map<Enchantment, Integer> enchantments,
                                                   Location loc, AnimalTamer player) {
        final ItemStackAction a = new ItemStackAction();
        a.setActionType(actionType);
        a.setLoc(loc);
        a.setPlayer(player);
        a.setItem(item, quantity, enchantments);
        return a;
    }

    /**
     * ItemFrame Handler.
     *
     * @param actionType   String
     * @param item         Item
     * @param quantity     int
     * @param attachedFace BlockFace
     * @param enchantments map
     * @param loc          Location
     * @param player       OfflinePlayer
     * @return handler
     */
    public static Handler createItemFrame(ActionType actionType, ItemStack item, int quantity, BlockFace attachedFace,
                                          Map<Enchantment, Integer> enchantments, Location loc, OfflinePlayer player) {
        final ItemStackAction a = createItemStack(actionType, item, quantity, enchantments, loc, player);
        a.setSlot(attachedFace.name().toLowerCase(Locale.ENGLISH));
        return a;
    }

    /**
     * PlayerAction.
     *
     * @param actionType     the action
     * @param player         the acting player
     * @param additionalInfo more info
     **/
    public static Handler createPlayer(ActionType actionType, Player player, String additionalInfo) {
        final PlayerAction a = new PlayerAction();
        a.setActionType(actionType);
        a.setPlayer(player);
        a.setLoc(player.getLocation());
        a.deserialize(additionalInfo);
        return a;
    }

    /**
     * PlayerDeathAction.
     *
     * @param actionType the action
     * @param player     the acting player
     * @param cause      Cause of death
     * @param attacker   attacker name
     */
    public static Handler createPlayerDeath(ActionType actionType, Player player, String cause, String attacker) {
        final PlayerDeathAction a = new PlayerDeathAction();
        a.setActionType(actionType);
        a.setPlayer(player);
        a.setLoc(player.getLocation());
        a.setCause(cause);
        a.setAttacker(attacker);
        return a;
    }

    /**
     * PrismProcessActionData.
     *
     * @param actionType  type
     * @param processType process
     * @param player      player
     * @param parameters  parameters
     * @return Handler
     */
    public static Handler createPrismProcess(ActionType actionType, PrismProcessType processType, Player player,
                                             String parameters) {
        final PrismProcessAction a = new PrismProcessAction();
        a.setActionType(actionType);
        a.setPlayer(player);
        a.setLoc(player.getLocation());
        a.setProcessData(processType, parameters);
        return a;
    }

    /**
     * PrismRollbackAction.
     *
     * @param actionType type
     * @param oldBlock   old
     * @param newBlock   new
     * @param player     player
     * @param parentId   id
     * @return Handler
     */
    public static Handler createPrismRollback(ActionType actionType, BlockState oldBlock, BlockState newBlock,
                                              AnimalTamer player, long parentId) {
        final PrismRollbackAction a = new PrismRollbackAction();
        a.setActionType(actionType);
        a.setPlayer(player);
        a.setLoc(oldBlock.getLocation());
        a.setBlockChange(oldBlock, newBlock, parentId);
        return a;
    }

    /**
     * SignAction.
     *
     * @param actionType the action
     * @param block      the block acted on
     * @param player     the acting player
     */
    public static Handler createSign(ActionType actionType, Block block, String[] lines, AnimalTamer player) {
        final SignAction a = new SignAction();
        a.setActionType(actionType);
        a.setPlayer(player);
        a.setBlock(block, lines);
        return a;
    }

    /**
     * UseAction.
     *
     * @param actionType the action
     * @param block      the block acted on
     * @param player     the acting player
     */
    public static Handler createUse(ActionType actionType, Material item, Block block, AnimalTamer player) {
        final UseAction a = new UseAction();
        a.setActionType(actionType);
        a.setPlayer(player);
        a.setLoc(block.getLocation());
        a.setMaterial(item);
        return a;
    }

    /**
     * VehicleAction.
     *
     * @param actionType the action
     * @param player     the acting player
     */
    public static Handler createVehicle(ActionType actionType, Entity vehicle, AnimalTamer player) {
        final VehicleAction a = new VehicleAction();
        a.setActionType(actionType);
        a.setPlayer(player);
        a.setLoc(vehicle.getLocation());
        a.setVehicle(vehicle);
        return a;
    }

    /**
     * Create a vehicle.
     *
     * @param actionType type
     * @param vehicle    vehicle
     * @param nonPlayer  Non Player
     * @return Handler
     */
    public static Handler createVehicle(ActionType actionType, Vehicle vehicle, String nonPlayer) {
        final Handler a = createVehicle(actionType, vehicle, (OfflinePlayer) null);
        a.setSourceName(nonPlayer);
        return a;
    }
}