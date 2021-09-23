package me.botsko.prism.api;

import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.api.actions.MatchRule;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.api.commands.Flag;
import me.botsko.prism.api.objects.MaterialState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 10/01/2021.
 */
public interface PrismParameters {

    /**
     * Get Query Id.
     *
     * @return the id
     */
    long getId();

    /**
     * Set the Id.
     *
     * @param id the id to set
     */
    void setId(long id);

    /**
     * Get the Min Id.
     *
     * @return long
     */
    long getMinPrimaryKey();

    /**
     * Set the minimum primary key.
     *
     * @param minId long
     */
    void setMinPrimaryKey(long minId);

    /**
     * Get the Max Id.
     *
     * @return long
     */
    long getMaxPrimaryKey();

    /**
     * Set the max primary key.
     *
     * @param maxId long
     */
    void setMaxPrimaryKey(long maxId);

    /**
     * Get the Entity Filter.
     *
     * @return the entity Map
     */
    Map<String, MatchRule> getEntities();

    /**
     * Add an entity to filter as an include.
     *
     * @param entity the entity  name to add
     */
    @SuppressWarnings("unused")
    void addEntity(String entity);

    /**
     * Add an entity to filter.
     *
     * @param entity the entity to set
     * @param match {@link MatchRule}
     */
    void addEntity(String entity, MatchRule match);

    /**
     * Add  block data to the filter.
     *
     * @param partialData MaterialState the block to set
     */
    void addBlockDataFilter(MaterialState partialData);

    /**
     * Get the block data filters.
     *
     * @return the block
     */
    Set<MaterialState> getBlockDataFilters();

    /**
     * Add material to the material filter.
     *
     * @param mat the material of block to set
     */
    void addBlockFilter(Material mat);

    /**
     * Get a specific list of block locations.
     *
     * @return the List.
     */
    List<Location> getSpecificBlockLocations();

    /**
     * Set a location to the list - clearing all others.
     *
     * @param loc the loc to set
     */
    void setSpecificBlockLocation(Location loc);

    /**
     * add a location to the list.
     *
     * @param loc the loc to set
     */
    void addSpecificBlockLocation(Location loc);

    /**
     * Get the player location.
     *
     * @return the player_location
     */
    Location getPlayerLocation();

    /**
     * Set Min Max Vectors from a location. Sets the player location ot the supplied variable. Uses the set radius.
     *
     * @param loc Location.
     */
    void setMinMaxVectorsFromPlayerLocation(Location loc);

    /**
     * Clear the vectors.
     */
    void resetMinMaxVectors();

    /**
     * Gets min location.
     *
     * @return Vector
     */
    Vector getMinLocation();

    /**
     * Set min loc.
     * @param minLoc {@link Vector}
     */
    void setMinLocation(Vector minLoc);

    /**
     * Get max location vector.
     *
     * @return Vector
     */
    Vector getMaxLocation();

    /**
     * Set the max location vector.
     * @param maxLoc {@link Vector}
     */
    void setMaxLocation(Vector maxLoc);

    /**
     * Get the radius.
     *
     * @return the radius
     */
    int getRadius();

    /**
     * Set the radius.
     *
     * @param radius the radius to set
     */
    void setRadius(int radius);

    /**
     * Get if no radius is allowed.
     *
     * @return the allow_no_radius
     */
    boolean allowsNoRadius();

    /**
     * Set Allow no radius.
     *
     * @param allowNoRadius the allow_no_radius to set
     */
    void setAllowNoRadius(boolean allowNoRadius);

    /**
     * get A set of Player names and match rules.
     *
     * @return the player
     */
    Map<String, MatchRule> getPlayerNames();

    /**
     * add a player name to the match set rules with a INCLUDE rule.
     *
     * @param player the player to set
     */
    void addPlayerName(String player);

    /**
     * add a player name to the match set rules.
     *
     * @param player the player to set
     * @param match  The rule to match
     */
    void addPlayerName(String player, MatchRule match);

    /**
     * Get the World name.
     *
     * @return the world
     */
    String getWorld();

    /**
     * Set the world name.
     *
     * @param world the world to set
     */
    void setWorld(String world);

    /**
     * Get the keyword.
     *
     * @return the keyword
     */
    String getKeyword();

    /**
     * Set the keyword.
     *
     * @param keyword the world to set
     */
    void setKeyword(String keyword);

    /**
     * Get a set of actions to match with a match rules.
     *
     * @return the Action Type
     */
    Map<ActionType, MatchRule> getActionTypes();

    /**
     * Get a set of actions to match with a match rules.
     *
     * @return the Action Type
     * @deprecated use getActionTypes()
     */
    @Deprecated
    Map<ActionType, MatchRule> getActionTypeNames();

    /**
     * Add action type to the filter with include rule.
     *
     * @param actionType the action_type to set
     */
    void addActionType(ActionType actionType);

    /**
     * Remove an action Type.
     * @param actionType {@link ActionType}
     */
    @SuppressWarnings("unused")
    void removeActionType(ActionType actionType);

    /**
     * Clear all action matches.
     */
    @SuppressWarnings("unused")
    void resetActionTypes();

    /**
     * Get before time.
     *
     * @return the time
     */
    Long getBeforeTime();

    /**
     * Set the before time.
     *
     * @param epoch the time to set
     */
    void setBeforeTime(Long epoch);

    /**
     * Get the time since.
     *
     * @return the time
     */
    Long getSinceTime();

    /**
     * Set the time since.
     *
     * @param epoch the time to set
     */
    void setSinceTime(Long epoch);

    /**
     * Get the limit.
     *
     * @return the limit
     */
    int getLimit();

    /**
     * Set the limit.
     *
     * @param limit the limit to set
     */
    void setLimit(int limit);

    /**
     * Get the lookup type.
     *
     * @return the ProcessType
     */
    PrismProcessType getProcessType();

    /**
     * Set the Lookup Type.
     *
     * @param lookupType the lookupType to set
     */
    void setProcessType(PrismProcessType lookupType);

    /**
     * Get the Set of Found arguments.
     *
     * @return the foundArgs
     */
    Set<String> getFoundArgs();

    /**
     * Set the Set of arguments.
     *
     * @param foundArgs the foundArgs to set
     */
    void setFoundArgs(Set<String> foundArgs);

    /**
     * Get the parent id.
     *
     * @return long
     */
    @SuppressWarnings("unused")
    long getParentId();

    /**
     * Set the parent id.
     *
     * @param id long.
     */
    void setParentId(long id);

    /**
     * LOOKUP = Most recent actions first. ROLLBACK = Newest to Oldest so we can
     * "rewind" the events RESTORE = Oldest to Newest so we can "replay" the events.
     *
     * @return String
     */
    String getSortDirection();

    /**
     * Add a flag to query.
     * @param flag {@link Flag}
     */
    void addFlag(Flag flag);

    /**
     * Check for a flag.
     *
     * @param flag Flag
     * @return bool
     */
    boolean hasFlag(Flag flag);

    /**
     * Get results per page.
     *
     * @return int.
     */
    int getPerPage();

    /**
     * Set results per page.
     *
     * @param perPage int
     */
    void setPerPage(int perPage);

    /**
     * Add default.
     *
     * @param d default.
     */
    void addDefaultUsed(String d);

    /**
     * Get Defaults.
     *
     * @return List
     */
    List<String> getDefaultsUsed();

    /**
     * Set from raw Args.
     *
     * @param args  String[];
     * @param start position to start at.
     */
    void setStringFromRawArgs(String[] args, int start);

    /**
     * Get the original command.
     *
     * @return String.
     */
    String getOriginalCommand();

    /**
     * Get the players that you're sharing your lookup with.
     *
     * @return List
     */
    List<CommandSender> getSharedPlayers();

    /**
     * Set the players you're sharing the lookup with.
     *
     * @param sender Command Sender.
     */
    void addSharedPlayer(CommandSender sender);

    /**
     * Check if we are ignoring the time.
     *
     * @return bool.
     */
    @SuppressWarnings("unused")
    boolean getIgnoreTime();

    /**
     * Ignore the time.
     *
     * @param ignore bool
     */
    void setIgnoreTime(boolean ignore);

    /**
     * Get the Set of materials to filter.
     * @return the block
     */
    Set<Material> getBlockFilters();

}

