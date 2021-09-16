package me.botsko.prism.actionlibs;

import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.api.actions.MatchRule;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.api.commands.Flag;
import me.botsko.prism.api.objects.MaterialState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Query Parameters allows you to add values with which Prism will build the
 * database queries.
 *
 * @author botskonet
 */
public class QueryParameters implements PrismParameters, Cloneable {

    private final List<String> defaultsUsed = new ArrayList<>();
    private final List<Location> specificBlockLocations = new ArrayList<>();
    private final EnumSet<Material> blockFilters = EnumSet.noneOf(Material.class);
    private final Set<MaterialState> materialStateFilters = new HashSet<>();
    private final Map<String, MatchRule> entityFilters = new HashMap<>();
    private final Map<String, MatchRule> playerNames = new HashMap<>();
    private final Set<Flag> flags = new HashSet<>();
    private final List<CommandSender> sharedPlayers = new ArrayList<>();

    private Set<String> foundArgs = new HashSet<>();
    private PrismProcessType processType = PrismProcessType.LOOKUP;
    private String originalCommand;
    private boolean allowNoRadius = false;
    private long id = 0;
    private long minId = 0;
    private long maxId = 0;
    private Vector maxLoc;
    private Vector minLoc;
    private long parentId = 0;
    private Location playerLocation;
    private int radius;
    private Long sinceTime;
    private Long beforeTime;
    private String world;
    private String keyword;
    private boolean ignoreTime;

    private HashMap<ActionType, MatchRule> actionTypeRules = new HashMap<>();

    private int perPage = 5;
    private int limit = 1000000;

    /**
     * Get Query Id.
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Set the Id.
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Get the Min Id.
     * @return long
     */
    public long getMinPrimaryKey() {
        return this.minId;
    }

    /**
     * Set the minimum primary key.
     * @param minId long
     */
    public void setMinPrimaryKey(long minId) {
        this.minId = minId;
    }

    /**
     * Get the Max Id.
     * @return long
     */
    public long getMaxPrimaryKey() {
        return this.maxId;
    }

    /**
     * Set the max primary key.
     * @param maxId long
     */
    public void setMaxPrimaryKey(long maxId) {
        this.maxId = maxId;
    }

    /**
     * Get the Entity Filter.
     * @return the entity Map
     */
    public Map<String, MatchRule> getEntities() {
        return entityFilters;
    }

    /**
     * Add an entity to filter as an include.
     * @param entity the entity  name to add
     */
    @SuppressWarnings("unused")
    public void addEntity(String entity) {
        addEntity(entity, MatchRule.INCLUDE);
    }

    /**
     * Add an entity to filter.
     * @param entity the entity to set
     */
    public void addEntity(String entity, MatchRule match) {
        this.entityFilters.put(entity, match);
    }

    /**
     * Get the Set of materials to filter.
     * @return the block
     */
    public Set<Material> getBlockFilters() {
        return blockFilters;
    }

    /**
     * Add  block data to the filter.
     *
     * @param partialData MaterialState the block to set
     */
    public void addBlockDataFilter(MaterialState partialData) {
        this.materialStateFilters.add(partialData);
    }

    /**
     * Get the block data filters.
     * @return the block
     */
    public Set<MaterialState> getBlockDataFilters() {
        return materialStateFilters;
    }

    /**
     * Add material to the material filter.
     *
     * @param mat the material of block to set
     */
    public void addBlockFilter(Material mat) {
        this.blockFilters.add(mat);
    }

    /**
     * Get a specific list of block locations.
     * @return the List.
     */
    public List<Location> getSpecificBlockLocations() {
        return specificBlockLocations;
    }

    /**
     * Set a location to the list - clearing all others.
     * @param loc the loc to set
     */
    public void setSpecificBlockLocation(Location loc) {
        this.specificBlockLocations.clear();
        addSpecificBlockLocation(loc);
    }

    /**
     * add a location to the list.
     * @param loc the loc to set
     */
    public void addSpecificBlockLocation(Location loc) {
        this.specificBlockLocations.add(loc);
    }

    /**
     * Get the player location.
     * @return the player_location
     */
    public Location getPlayerLocation() {
        return playerLocation;
    }

    /**
     * Set Min Max Vectors from a location. Sets the player location ot the supplied variable. Uses the set radius.
     * @param loc Location.
     */
    public void setMinMaxVectorsFromPlayerLocation(Location loc) {
        this.playerLocation = loc;
        if (radius > 0) {
            minLoc = new Vector(loc.getX() - radius, loc.getY() - radius, loc.getZ() - radius);
            maxLoc = new Vector(loc.getX() + radius, loc.getY() + radius, loc.getZ() + radius);
        }
    }

    /**
     * Clear the vectors.
     */
    public void resetMinMaxVectors() {
        minLoc = null;
        maxLoc = null;
    }

    /**
     * Gets min location.
     * @return Vector
     */
    public Vector getMinLocation() {
        return minLoc;
    }

    /**
     * Set min loc.
     */
    public void setMinLocation(Vector minLoc) {
        this.minLoc = minLoc;
    }

    /**
     * Get max location vector.
     * @return Vector
     */
    public Vector getMaxLocation() {
        return maxLoc;
    }

    /**
     * Set the max location vector.
     */
    public void setMaxLocation(Vector maxLoc) {
        this.maxLoc = maxLoc;
    }

    /**
     * Get the radius.
     * @return the radius
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Set the radius.
     * @param radius the radius to set
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * Get if no radius is allowed.
     * @return the allow_no_radius
     */
    public boolean allowsNoRadius() {
        return allowNoRadius;
    }

    /**
     * Set Allow no radius.
     * @param allowNoRadius the allow_no_radius to set
     */
    public void setAllowNoRadius(boolean allowNoRadius) {
        this.allowNoRadius = allowNoRadius;
    }

    /**
     * get A set of Player names and match rules.
     * @return the player
     */
    public Map<String, MatchRule> getPlayerNames() {
        return playerNames;
    }

    /**
     * add a player name to the match set rules with a INCLUDE rule.
     * @param player the player to set
     */
    public void addPlayerName(String player) {
        addPlayerName(player, MatchRule.INCLUDE);
    }

    /**
     *  add a player name to the match set rules.
     * @param player the player to set
     * @param match  The rule to match
     */
    public void addPlayerName(String player, MatchRule match) {
        this.playerNames.put(player, match);
    }

    /**
     * Get the World name.
     * @return the world
     */
    public String getWorld() {
        return world;
    }

    /**
     * Set the world name.
     * @param world the world to set
     */
    public void setWorld(String world) {
        this.world = world;
    }

    /**
     * Get the keyword.
     * @return the keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Set the keyword.
     * @param keyword the world to set
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Get a set of actions to match with a match rules.
     * @return the Action Type
     */
    public HashMap<ActionType, MatchRule> getActionTypes() {
        return actionTypeRules;
    }

    /**
     * Get a set of actions to match with a match rules.
     * @return the Action Type
     * @deprecated use getActionTypes()
     */
    @Deprecated
    public HashMap<ActionType, MatchRule> getActionTypeNames() {
        return getActionTypes();
    }

    /**
     * Add action type to the filter with include rule.
     * @param actionType the action_type to set
     */
    public void addActionType(ActionType actionType) {
        addActionType(actionType, MatchRule.INCLUDE);
    }

    /**
     * Add Action type to match with the rule.
     * @param actionType the action_type to set
     * @param match the rule
     */
    public void addActionType(ActionType actionType, MatchRule match) {
        this.actionTypeRules.put(actionType, match);
    }

    /**
     * Remove an action Type.
     */
    @SuppressWarnings("unused")
    public void removeActionType(ActionType a) {
        actionTypeRules.remove(a.name);
    }

    /**
     * Clear all action matches.
     */
    @SuppressWarnings("unused")
    public void resetActionTypes() {
        actionTypeRules.clear();
    }

    /**
     * Get before time.
     * @return the time
     */
    public Long getBeforeTime() {
        return beforeTime;
    }

    /**
     * Set the before time.
     * @param epoch the time to set
     */
    public void setBeforeTime(Long epoch) {
        this.beforeTime = epoch;
    }

    /**
     * Get the time since.
     * @return the time
     */
    public Long getSinceTime() {
        return sinceTime;
    }

    /**
     * Set the time since.
     * @param epoch the time to set
     */
    public void setSinceTime(Long epoch) {
        this.sinceTime = epoch;
    }

    /**
     * Get the limit.
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Set the limit.
     * @param limit the limit to set
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Get the lookup type.
     * @return the ProcessType
     */
    public PrismProcessType getProcessType() {
        return processType;
    }

    /**
     * Set the Lookup Type.
     * @param lookupType the lookupType to set
     */
    public void setProcessType(PrismProcessType lookupType) {
        this.processType = lookupType;
    }

    /**
     * Get the Set of Found arguments.
     * @return the foundArgs
     */
    public Set<String> getFoundArgs() {
        return foundArgs;
    }

    /**
     * Set the Set of arguments.
     * @param foundArgs the foundArgs to set
     */
    public void setFoundArgs(Set<String> foundArgs) {
        this.foundArgs = foundArgs;
    }

    /**
     * Get the parent id.
     * @return long
     */
    @SuppressWarnings("unused")
    public long getParentId() {
        return parentId;
    }

    /**
     * Set the parent id.
     * @param id long.
     */
    public void setParentId(long id) {
        this.parentId = id;
    }

    /**
     * LOOKUP = Most recent actions first. ROLLBACK = Newest->Oldest so we can
     * "rewind" the events RESTORE = Oldest->Newest so we can "replay" the events.
     *
     * @return String
     */
    public String getSortDirection() {
        if (!this.processType.equals(PrismProcessType.RESTORE)) {
            return "DESC";
        }
        return "ASC";
    }

    /**
     * Add a flag to query.
     */
    public void addFlag(Flag flag) {
        if (!hasFlag(flag)) {
            this.flags.add(flag);
        }
    }

    /**
     * Check for a flag.
     * @param flag Flag
     * @return bool
     */
    public boolean hasFlag(Flag flag) {
        return flags.contains(flag);
    }

    /**
     * Get results per page.
     * @return int.
     */
    @SuppressWarnings("WeakerAccess")
    public int getPerPage() {
        return perPage;
    }

    /**
     * Set results per page.
     * @param perPage  int
     */
    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }

    /**
     * Add default.
     * @param d default.
     */
    public void addDefaultUsed(String d) {
        defaultsUsed.add(d);
    }

    /**
     * Get Defaults.
     * @return List
     */
    public List<String> getDefaultsUsed() {
        return defaultsUsed;
    }

    /**
     * Set from raw Args.
     * @param args String[];
     * @param start position to start at.
     */
    public void setStringFromRawArgs(String[] args, int start) {
        StringBuilder params = new StringBuilder();
        if (args.length > 0) {
            for (int i = start; i < args.length; i++) {
                params.append(" ").append(args[i]);
            }
        }
        originalCommand = params.toString();
    }

    /**
     * Get the original command.
     * @return String.
     */
    public String getOriginalCommand() {
        return originalCommand;
    }

    /**
     * Get the players that you're sharing your lookup with.
     *
     * @return List
     */
    public List<CommandSender> getSharedPlayers() {
        return sharedPlayers;
    }

    @Override
    public String toString() {
        return "Original Command: " + originalCommand;
    }

    /**
     * Set the players you're sharing the lookup with.
     *
     * @param sender Command Sender.
     */
    public void addSharedPlayer(CommandSender sender) {
        this.sharedPlayers.add(sender);
    }

    @Override
    public QueryParameters clone() throws CloneNotSupportedException {
        final QueryParameters cloned = (QueryParameters) super.clone();
        cloned.actionTypeRules = new HashMap<>(actionTypeRules);
        return cloned;
    }

    /**
     * Check if we are ignoring the time.
     *
     * @return bool.
     */
    @SuppressWarnings("unused")
    public boolean getIgnoreTime() {
        return ignoreTime;
    }

    /**
     * Ignore the time.
     *
     * @param ignore bool
     */
    public void setIgnoreTime(boolean ignore) {
        this.ignoreTime = ignore;
    }
}
