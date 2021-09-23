package me.botsko.prism.database.sql;

import com.google.gson.JsonSyntaxException;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.actionlibs.ActionRegistryImpl;
import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.actionlibs.RecordingManager;
import me.botsko.prism.api.actions.Action;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.api.actions.MatchRule;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.api.objects.MaterialState;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.QueryBuilder;
import me.botsko.prism.database.SelectQuery;
import me.botsko.prism.measurement.TimeTaken;
import me.botsko.prism.utils.IntPair;
import me.botsko.prism.utils.ItemUtils;
import me.botsko.prism.utils.TypeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SqlSelectQueryBuilder extends QueryBuilder implements SelectQuery {

    public SqlSelectQueryBuilder(PrismDataSource<?> dataSource) {
        super(dataSource);
    }

    @Override
    protected String select() {
        String query = "";
        query += "SELECT ";
        if (shouldGroup) {
            columns.add("MIN(id) id");
            columns.add("MIN(epoch) epoch");
            columns.add("MIN(action_id) action_id");
            columns.add("MIN(player) player");
            columns.add("MIN(world_id) world_id");
        } else {
            columns.add("id");
            columns.add("epoch");
            columns.add("action_id");
            columns.add("player");
            columns.add("world_id");
        }

        if (shouldGroup) {
            columns.add("AVG(x)");
            columns.add("AVG(y)");
            columns.add("AVG(z)");
        } else {
            columns.add("x");
            columns.add("y");
            columns.add("z");
        }

        if (shouldGroup) {
            columns.add("MIN(block_id) block_id");
            columns.add("MIN(block_subid) block_subid");
            columns.add("MIN(old_block_id) old_block_id");
            columns.add("MIN(old_block_subid) old_block_subid");
            columns.add("MIN(data) data");
            columns.add("MIN(HEX(player_uuid)) AS uuid");
        } else {
            columns.add("block_id");
            columns.add("block_subid");
            columns.add("old_block_id");
            columns.add("old_block_subid");
            columns.add("data");
            columns.add("HEX(player_uuid) AS uuid");
        }

        if (shouldGroup) {
            columns.add("COUNT(*) counted");
        }

        // Append all columns
        if (columns.size() > 0) {
            query += TypeUtils.join(columns, ", ");
        }

        // From
        query += " FROM " + tableNameData + " ";

        // Joins
        query += "INNER JOIN " + prefix + "players p ON p.player_id = " + tableNameData + ".player_id ";
        query += "LEFT JOIN " + tableNameDataExtra + " ex ON ex.data_id = " + tableNameData + ".id ";

        return query;

    }

    @Override
    protected String where() {
        if (parameters == null) {
            return " ";
        }
        // ID Condition overrides anything else
        final long id = parameters.getId();
        if (id > 0) {
            return "WHERE " + tableNameData + ".id = " + id;
        }

        // id range conditions
        final long minId = parameters.getMinPrimaryKey();
        final long maxId = parameters.getMaxPrimaryKey();
        if (minId > 0 && maxId > 0 && minId != maxId) {
            addCondition(tableNameData + ".id >= " + minId);
            addCondition(tableNameData + ".id < " + maxId);
        }

        worldCondition();
        actionCondition();
        playerCondition();
        radiusCondition();
        blockCondition();
        entityCondition();
        timeCondition();
        keywordCondition();
        coordinateCondition();

        return buildWhereConditions();

    }

    private void worldCondition() {
        if (parameters.getWorld() != null) {
            Integer worldId = Prism.prismWorlds.get(parameters.getWorld());
            if (worldId != null && worldId != 0) {
                addCondition("world_id = " + worldId);
            } else {
                addCondition(
                        String.format("world_id = ( SELECT w.world_id FROM " + prefix
                                        + "worlds w WHERE w.world = '%s')",
                                parameters.getWorld()));
            }
        }
    }

    private void actionCondition() {
        // Action type
        final Map<ActionType, MatchRule> action_types = parameters.getActionTypes();
        boolean containsPrismProcessType = false;

        // Build IDs for prism process actions
        final Collection<String> prismActionIds = new ArrayList<>();
        for (final Entry<ActionType, Integer> entry : ActionRegistryImpl.prismActions.entrySet()) {
            if (entry.getKey().name.contains("prism")) {
                containsPrismProcessType = true;
                prismActionIds.add("" + ActionRegistryImpl.prismActions.get(entry.getKey()));
            }
        }

        // scan whitelist of given actions
        if (action_types.size() > 0) {

            final Collection<String> includeIds = new ArrayList<>();
            final Collection<String> excludeIds = new ArrayList<>();
            for (final Entry<ActionType, MatchRule> entry : action_types.entrySet()) {
                Integer id = ActionRegistryImpl.prismActions.get(entry.getKey());
                if (id == null) {
                    // null id means the action isn't registered.
                    PrismLogHandler.debug("Ignoring" + entry.getKey() + " as Action not registered.");
                    continue;
                }
                if (entry.getValue().equals(MatchRule.INCLUDE)) {
                    includeIds.add("" + id);
                }
                if (entry.getValue().equals(MatchRule.EXCLUDE)) {
                    excludeIds.add("" + id);
                }
            }
            // Include IDs
            if (includeIds.size() > 0) {
                addCondition("action_id IN (" + TypeUtils.join(includeIds, ",") + ")");
            }
            // Exclude IDs
            if (excludeIds.size() > 0) {
                addCondition("action_id NOT IN (" + TypeUtils.join(excludeIds, ",") + ")");
            }
        } else {
            // exclude internal stuff
            if (!containsPrismProcessType && !parameters.getProcessType().equals(PrismProcessType.DELETE)) {
                if (prismActionIds.size() > 0) {
                    addCondition("action_id NOT IN (" + TypeUtils.join(prismActionIds, ",") + ")");
                }
            }
        }
    }

    private void playerCondition() {
        final Map<String, MatchRule> playerNames = parameters.getPlayerNames();
        if (playerNames.size() > 0) {

            // Match the first rule, this needs to change, we can't include and
            // exclude at the same time
            MatchRule playerMatch = MatchRule.INCLUDE;
            for (final MatchRule match : playerNames.values()) {
                playerMatch = match;
                break;
            }
            final String matchQuery = (playerMatch.equals(MatchRule.INCLUDE) ? "IN" : "NOT IN");
            // @todo Temporary band-aid. The player list should not actually exclude anyone
            // because
            // we're doing it here. This is going to be rewritten soon anyway.
            for (Entry<String, MatchRule> entry : playerNames.entrySet()) {
                entry.setValue(MatchRule.INCLUDE);
            }
            // Add conditions
            addCondition(tableNameData + ".player_id " + matchQuery + " ( SELECT p.player_id FROM " + prefix
                    + "players p WHERE " + buildMultipleConditions(playerNames, "p.player", null) + ")");
        }
    }

    private void radiusCondition() {
        buildRadiusCondition(parameters.getMinLocation(), parameters.getMaxLocation());
    }

    private void blockCondition() {
        // Blocks
        final Set<Material> blockFilters = parameters.getBlockFilters();
        if (!blockFilters.isEmpty()) {
            final String[] blockArr = new String[blockFilters.size()];
            int i = 0;
            for (Material m : blockFilters) {

                Set<IntPair> allIds = Prism.getItems().materialToAllIds(m);

                StringBuilder blockIds = new StringBuilder("(");
                for (IntPair pair : allIds) {
                    blockIds.append(pair.first).append(',');
                }

                String in = blockIds.append(')').toString().replace(",)", ")");

                blockArr[i++] = tableNameData + ".block_id IN " + in;
            }
            addCondition(buildGroupConditions(null, blockArr, "%s%s", "OR", null));
        }

        Set<MaterialState> blockDataFilters = parameters.getBlockDataFilters();

        if (!blockDataFilters.isEmpty()) {
            final ArrayList<String> blockArr = new ArrayList<>();

            for (MaterialState data : blockDataFilters) {
                Set<IntPair> pairs = Prism.getItems().partialBlockDataIds(data.material, data.state);

                for (IntPair pair : pairs) {
                    blockArr.add(tableNameData + ".block_id = " + pair.first + " AND " + tableNameData
                            + ".block_subid = " + pair.second);
                }
            }
            addCondition(buildGroupConditions(null, blockArr.toArray(new String[0]), "%s%s", "OR", null));
        }
    }

    private void entityCondition() {
        // Entity
        final Map<String, MatchRule> entityNames = parameters.getEntities();
        if (entityNames.size() > 0) {
            addCondition(buildMultipleConditions(entityNames, "ex.data", "entityName\":\"%s"));
        }
    }

    private void timeCondition() {
        // Timeframe
        Long time = parameters.getBeforeTime();
        if (time != null && time != 0) {
            addCondition(buildTimeCondition(time, "<="));
        }
        time = parameters.getSinceTime();
        if (time != null && time != 0) {
            addCondition(buildTimeCondition(time, null));
        }
    }

    private void keywordCondition() {
        // Keyword(s)
        final String keyword = parameters.getKeyword();
        if (keyword != null) {
            addCondition("ex.data LIKE '%" + keyword + "%'");
        }
    }

    private void coordinateCondition() {
        // Specific coords
        final List<Location> locations = parameters.getSpecificBlockLocations();
        if (locations.size() > 0) {
            StringBuilder coordCond = new StringBuilder("(");
            int l = 0;
            for (final Location loc : locations) {
                coordCond.append(l > 0 ? " OR" : "").append(" (").append(tableNameData).append(".x = ")
                        .append(loc.getBlockX()).append(" AND ").append(tableNameData).append(".y = ")
                        .append(loc.getBlockY()).append(" AND ").append(tableNameData).append(".z = ")
                        .append(loc.getBlockZ()).append(")");
                l++;
            }
            coordCond.append(")");
            addCondition(coordCond.toString());
        }
    }

    private String buildWhereConditions() {

        // Parent process
        // if(parameters.getParentId() > 0){
        // addCondition( String.format("ex.data = %d", parameters.getParentId())
        // );
        // }

        // Build final condition string
        int condCount = 1;
        StringBuilder query = new StringBuilder();
        if (conditions.size() > 0) {
            for (final String cond : conditions) {
                if (condCount == 1) {
                    query.append(" WHERE ");
                } else {
                    query.append(" AND ");
                }
                query.append(cond);
                condCount++;
            }
        }

        return query.toString();

    }

    @Override
    protected String group() {
        if (shouldGroup) {
            return " GROUP BY " + tableNameData + ".action_id, " + tableNameData + ".player_id, " + tableNameData
                    + ".block_id, ex.data, DATE(FROM_UNIXTIME(" + tableNameData + ".epoch))";
        }
        return "";
    }

    @Override
    protected String order() {
        if (parameters == null) {
            return " ";
        }
        final String sort_dir = parameters.getSortDirection();

        if (shouldGroup) {
            return " ORDER BY MAX(" + tableNameData + ".epoch) " + sort_dir
                    + ", AVG(x) ASC, AVG(z) ASC, AVG(y) ASC, MIN(id) " + sort_dir;
        }

        return " ORDER BY " + tableNameData + ".epoch " + sort_dir + ", x ASC, z ASC, y ASC, id " + sort_dir;
    }

    @Override
    protected String limit() {
        if (parameters == null) {
            return "";
        }
        if (parameters.getProcessType().equals(PrismProcessType.LOOKUP)) {
            final int limit = parameters.getLimit();
            if (limit > 0) {
                return " LIMIT " + limit;
            }
        }
        return "";
    }

    private String buildMultipleConditions(Map<String, MatchRule> origValues, String fieldName, String format) {
        String query = "";
        if (!origValues.isEmpty()) {

            final ArrayList<String> whereIs = new ArrayList<>();
            final ArrayList<String> whereNot = new ArrayList<>();
            final ArrayList<String> whereIsLike = new ArrayList<>();
            for (final Entry<String, MatchRule> entry : origValues.entrySet()) {
                if (entry.getValue().equals(MatchRule.EXCLUDE)) {
                    whereNot.add(entry.getKey());
                } else if (entry.getValue().equals(MatchRule.PARTIAL)) {
                    whereIsLike.add(entry.getKey());
                } else {
                    whereIs.add(entry.getKey());
                }
            }
            // To match
            if (!whereIs.isEmpty()) {
                String[] whereValues = new String[whereIs.size()];
                whereValues = whereIs.toArray(whereValues);
                if (format == null) {
                    query += buildGroupConditions(fieldName, whereValues, "%s = '%s'", "OR", null);
                } else {
                    query += buildGroupConditions(fieldName, whereValues, "%s LIKE '%%%s%%'", "OR", format);
                }
            }
            // To match partial
            if (!whereIsLike.isEmpty()) {
                String[] whereValues = new String[whereIsLike.size()];
                whereValues = whereIsLike.toArray(whereValues);
                query += buildGroupConditions(fieldName, whereValues, "%s LIKE '%%%s%%'", "OR", format);
            }
            // Not match
            if (!whereNot.isEmpty()) {
                String[] whereNotValues = new String[whereNot.size()];
                whereNotValues = whereNot.toArray(whereNotValues);

                if (format == null) {
                    query += buildGroupConditions(fieldName, whereNotValues, "%s != '%s'", null, null);
                } else {
                    query += buildGroupConditions(fieldName, whereNotValues, "%s NOT LIKE '%%%s%%'", null, format);
                }
            }
        }
        return query;
    }

    private String buildGroupConditions(final String fieldName, String[] argValues, final String matchFormat,
                                        final String matchType, final String dataFormat) {

        StringBuilder where = new StringBuilder();
        String format = (matchFormat == null ? "%s = %s" : matchFormat);
        String match = (matchType == null ? "AND" : matchType);
        String dataForm = (dataFormat == null ? "%s" : dataFormat);

        if (argValues.length > 0 && !format.isEmpty()) {
            where.append("(");
            int c = 1;
            for (final String val : argValues) {
                if (c > 1 && c <= argValues.length) {
                    where.append(" ").append(match).append(" ");
                }
                String field = (fieldName == null ? "" : fieldName);
                where.append(String.format(format, field, String.format(dataForm, val)));
                c++;
            }
            where.append(")");
        }
        return where.toString();
    }

    private void buildRadiusCondition(Vector minLoc, Vector maxLoc) {
        if (minLoc != null && maxLoc != null) {
            addCondition("(" + tableNameData + ".x BETWEEN " + minLoc.getBlockX() + " AND " + maxLoc.getBlockX() + ")");
            addCondition("(" + tableNameData + ".y BETWEEN " + minLoc.getBlockY() + " AND " + maxLoc.getBlockY() + ")");
            addCondition("(" + tableNameData + ".z BETWEEN " + minLoc.getBlockZ() + " AND " + maxLoc.getBlockZ() + ")");
        }
    }

    private String buildTimeCondition(Long dateFrom, String equation) {
        final String where = "";
        if (dateFrom != null) {
            if (equation == null) {
                addCondition(tableNameData + ".epoch >= " + (dateFrom / 1000) + "");
            } else {
                addCondition(tableNameData + ".epoch " + equation + " " + (dateFrom / 1000));
            }
        }
        return where;
    }

    @Override
    public QueryResult executeSelect(TimeTaken eventTimer) {

        final List<Handler> actions = new ArrayList<>();
        // Build conditions based off final args
        final String query = getQuery(parameters, shouldGroup);
        eventTimer.recordTimedEvent("query started");
        try (
                Connection conn = Prism.getInstance().getPrismDataSource().getDataSource().getConnection();
                PreparedStatement s = conn.prepareStatement(query);
                ResultSet rs = s.executeQuery()
        ) {
            RecordingManager.failedDbConnectionCount = 0;
            eventTimer.recordTimedEvent("query returned, building results");
            Map<Integer, String> worldsInverse = new HashMap<>();
            for (final Entry<String, Integer> entry : Prism.prismWorlds.entrySet()) {
                worldsInverse.put(entry.getValue(), entry.getKey());
            }
            while (rs.next()) {

                if (rs.getString(3) == null) {
                    continue;
                }

                // Convert action ID to name
                // Performance-wise this is a lot faster than table joins
                // and the cache data should always be available
                int actionId = rs.getInt(3);

                ActionType actionName = null;
                for (final Entry<ActionType, Integer> entry : ActionRegistryImpl.prismActions.entrySet()) {
                    if (entry.getValue() == actionId) {
                        actionName = entry.getKey();
                    }
                }
                if (actionName == null) {
                    PrismLogHandler.warn("Record contains action ID that doesn't exist in cache: " + actionId
                            + ", cacheSize=" + ActionRegistryImpl.prismActions.size());
                    continue;
                }

                // Get the action handler
                final Action action = Prism.getActionRegistryImpl().getAction(actionName);

                if (action == null) {
                    continue;
                }

                long rowId = 0;

                try {

                    final Handler baseHandler = Prism.getHandlerRegistry().create(action.getHandler());

                    // Convert world ID to name
                    // Performance-wise this is typically a lot faster than
                    // table joins
                    rowId = rs.getLong(1);
                    // Set all shared values
                    baseHandler.setAction(action);
                    baseHandler.setId(rowId);
                    baseHandler.setUnixEpoch(rs.getLong(2));

                    String worldName = worldsInverse.getOrDefault(rs.getInt(5), "");
                    baseHandler.setWorld(Bukkit.getWorld(worldName));//todo Async BukkitAPI?
                    baseHandler.setX(rs.getInt(6));
                    baseHandler.setY(rs.getInt(7));
                    baseHandler.setZ(rs.getInt(8));
                    int blockId = rs.getInt(9);
                    int blockSubId = rs.getInt(10);

                    int oldBlockId = rs.getInt(11);
                    int oldBlockSubId = rs.getInt(12);

                    String extraData = rs.getString(13);

                    boolean validBlockId = false;
                    boolean validOldBlockId = false;

                    MaterialState current = Prism.getItems().idsToMaterial(blockId, blockSubId, false);

                    if (current != null) {
                        ItemStack item = current.asItem();
                        BlockData block = current.asBlockData();

                        if (block != null) {
                            validBlockId = true;
                            baseHandler.setMaterial(block.getMaterial());
                            baseHandler.setBlockData(block);
                            baseHandler.setDurability((short) 0);

                        } else if (item != null) {
                            validBlockId = true;
                            baseHandler.setMaterial(item.getType());

                            BlockData newData;

                            try {
                                item.getType().createBlockData();
                                newData = Bukkit.createBlockData(item.getType());
                            } catch (IllegalArgumentException e) {
                                // This exception occurs, for example, with "ItemStack{DIAMOND_LEGGINGS x 1}"
                                PrismLogHandler.debug("IllegalArgumentException for record #" + rowId
                                        + " calling createBlockData for " + item);
                                newData = null;
                            }

                            baseHandler.setBlockData(newData);
                            baseHandler.setDurability((short) ItemUtils.getItemDamage(item));
                        }
                    }

                    MaterialState old = Prism.getItems().idsToMaterial(oldBlockId, oldBlockSubId, false);

                    if (old != null) {
                        ItemStack oldItem = old.asItem();
                        BlockData oldBlock = old.asBlockData();
                        validOldBlockId = true;

                        if (oldBlock != null) {
                            baseHandler.setOldMaterial(oldBlock.getMaterial());
                            baseHandler.setOldBlockData(oldBlock);
                            baseHandler.setOldDurability((short) 0);
                        } else {
                            baseHandler.setOldMaterial(oldItem.getType());
                            baseHandler.setOldBlockData(Bukkit.createBlockData(oldItem.getType()));
                            baseHandler.setOldDurability((short) ItemUtils.getItemDamage(oldItem));
                        }
                    }

                    if (!validBlockId && !validOldBlockId) {
                        // Entry could not be converted to a block or an item

                        boolean logWarning;
                        // The current item is likely a spawn or death event for an entity, for example, a cow or horse
                        logWarning = blockId != 0 || oldBlockId != 0 || extraData == null
                                || !extraData.contains("entity_name");

                        if (logWarning) {
                            String itemMetadataDesc;

                            if (extraData == null) {
                                itemMetadataDesc = "";
                            } else {
                                itemMetadataDesc = ", metadata=" + extraData;
                            }

                            if (blockId > 0) {
                                PrismLogHandler.warn("Unable to convert record #" + rowId + " to material: "
                                        + "block_id=" + blockId + ", block_subid=" + blockSubId + itemMetadataDesc);
                            } else if (oldBlockId > 0) {
                                PrismLogHandler.warn("Unable to convert record #" + rowId + " to material: "
                                        + "old_block_id=" + oldBlockId + ", old_block_subid="
                                        + oldBlockSubId + itemMetadataDesc);
                            } else {
                                PrismLogHandler.warn("Unable to convert record #" + rowId + " to material: "
                                        + "block_id=0, old_block_id=0" + itemMetadataDesc);
                            }
                        }
                    }

                    // data
                    try {
                        baseHandler.deserialize(extraData);
                    } catch (JsonSyntaxException e) {
                        if (Prism.isDebug()) {
                            PrismLogHandler.warn("Deserialization Error: " + e.getLocalizedMessage(), e);
                        }
                    } catch (NullPointerException e) {
                        //this should not happen log it out
                        PrismLogHandler.warn("Deserialization Error  (NPE): Row" + rowId + " " + e.getLocalizedMessage(), e);
                    }
                    // player
                    baseHandler.setSourceName(rs.getString(4));

                    // player_uuid
                    try {
                        // Calls UUID.fromString, must handle potential exceptions
                        OfflinePlayer offline = Bukkit.getOfflinePlayer(
                                SqlPlayerIdentificationQuery.uuidFromDbString(rs.getString(14)));

                        // Fake player
                        if (offline.hasPlayedBefore()) {
                            baseHandler.setUuid(offline.getUniqueId());
                        }
                    } catch (IllegalArgumentException | NullPointerException e) {
                        // Not a valid uuid
                    }

                    // Set aggregate counts if a lookup
                    int aggregated = 0;
                    if (shouldGroup) {
                        aggregated = rs.getInt(15);
                    }
                    baseHandler.setAggregateCount(aggregated);

                    actions.add(baseHandler);

                } catch (final SQLException e) {
                    PrismLogHandler.warn("Ignoring data from record #" + rowId + " because it caused an error:", e);
                }
            }
        } catch (NullPointerException e) {
            if (RecordingManager.failedDbConnectionCount == 0) {
                PrismLogHandler.log("Prism database error. Connection missing. Leaving actions to log in queue.");
                PrismLogHandler.debug(e.getMessage(),e);
            }
            RecordingManager.failedDbConnectionCount++;

            return new QueryResult(actions, parameters);

        } catch (SQLException e) {
            Prism.getInstance().getPrismDataSource().handleDataSourceException(e);
        }
        return new QueryResult(actions, parameters);
    }
}
