package me.botsko.prism.database.sql;

import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.actionlibs.ActionRegistryImpl;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.database.InsertQuery;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.QueryBuilder;
import me.botsko.prism.players.PrismPlayer;
import me.botsko.prism.utils.IntPair;
import me.botsko.prism.utils.block.Utilities;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by Narimm on 1/06/2019.
 */
public class SqlInsertBuilder extends QueryBuilder implements InsertQuery {
    private final ArrayList<Handler> extraDataQueue = new ArrayList<>();
    private PreparedStatement batchStatement;
    private Connection batchConnection;

    /**
     * Create an insert builder.
     *
     * @param dataSource Data source
     */
    public SqlInsertBuilder(PrismDataSource<?> dataSource) {
        super(dataSource);
    }

    private int getWorldId(Handler a) {
        String worldName = a.getLoc().getWorld().getName();
        if (Prism.prismWorlds.containsKey(worldName)) {
            return Prism.prismWorlds.get(worldName);
        }
        return 0;
    }

    private int getActionId(Handler a) {
        if (ActionRegistryImpl.prismActions.containsKey(a.getAction().getActionType())) {
            return ActionRegistryImpl.prismActions.get(a.getAction().getActionType());
        }
        return 0;
    }

    private int getPlayerId(Handler a) {
        PrismPlayer prismPlayer = Prism.getInstance().getPlayerIdentifier()
                .getPrismPlayerByNameFromCache(a.getSourceName());
        return prismPlayer.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insertActionIntoDatabase(Handler a) {
        int worldId = getWorldId(a);
        int actionId = getActionId(a);
        int playerId = getPlayerId(a);
        if (worldId == 0 || actionId == 0 || playerId == 0) {
            PrismLogHandler.debug("Sql data error: Handler:" + a);
        }
        IntPair newIds = Prism.getItems().materialToIds(a.getMaterial(),
                Utilities.dataString(a.getBlockData()));
        IntPair oldIds = Prism.getItems().materialToIds(a.getOldMaterial(),
                Utilities.dataString(a.getOldBlockData()));

        Location l = a.getLoc();
        long id = 0;
        try (
                Connection con = dataSource.getConnection()
        ) {
            try (
                    PreparedStatement s = con.prepareStatement(getQuery(), Statement.RETURN_GENERATED_KEYS)
            ) {
                applyToInsert(s, a, actionId, playerId, worldId, newIds, oldIds, l);
                s.executeUpdate();
                ResultSet generatedKeys = s.getGeneratedKeys();
                if (generatedKeys.next()) {
                    id = generatedKeys.getLong(1);
                }
                if (a.hasExtraData()) {
                    String serialData = a.serialize();
                    if (serialData != null && !serialData.isEmpty()) {
                        if (serialData.length() > 32000) {
                            PrismLogHandler.debug("Large SerialData Details: Loc: "
                                    + a.getLoc().toString()
                                    + " Action:" + a.getAction().getActionType().name
                                    + " Block:" + a.getMaterial().name());
                            PrismLogHandler.debug("    SerialData: " + serialData);
                            throw new SQLDataException("Extra Data value to large for database (max 32000) "
                                    + serialData.length());
                        }
                        try (
                                PreparedStatement s2 = con.prepareStatement(getExtraDataQuery(),
                                        Statement.RETURN_GENERATED_KEYS)
                        ) {
                            s2.setLong(1, id);
                            s2.setString(2, serialData);
                            s2.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                PrismLogHandler.warn(e.getMessage(), e);
            }
        } catch (SQLException e) {
            dataSource.handleDataSourceException(e);
        }
        return id;
    }

    @Override
    public void createBatch() throws SQLException {
        batchConnection = dataSource.getConnection();
        if (batchConnection == null) {
            throw new SQLException("No Connection to database");
        }
        batchConnection.setAutoCommit(false);
        batchStatement = batchConnection.prepareStatement(getQuery(), Statement.RETURN_GENERATED_KEYS);
    }

    @Override
    public boolean addInsertionToBatch(Handler a) throws SQLException {
        if (batchStatement == null) {
            return false;
        }
        int worldId = getWorldId(a);
        int actionId = getActionId(a);
        int playerId = getPlayerId(a);
        if (worldId == 0 || actionId == 0 || playerId == 0) {
            PrismLogHandler.debug("Sql data error: Handler:" + a);
        }
        IntPair newIds = Prism.getItems().materialToIds(a.getMaterial(),
                Utilities.dataString(a.getBlockData()));

        IntPair oldIds = Prism.getItems().materialToIds(a.getOldMaterial(),
                Utilities.dataString(a.getOldBlockData()));
        Location l = a.getLoc();
        applyToInsert(batchStatement, a, actionId, playerId, worldId, newIds, oldIds, l);
        batchStatement.addBatch();
        extraDataQueue.add(a);
        return true;
    }

    /**
     * Process the batch.
     *
     * @throws SQLException on sql errors
     */
    public void processBatch() throws SQLException {
        if (batchStatement == null) {
            PrismLogHandler.debug("Batch insert was null");
            throw new SQLException("no batch statement configured");
        }
        long currentTime = System.currentTimeMillis();
        int[] results = batchStatement.executeBatch();
        batchConnection.commit();
        debugBatch("primary", results, currentTime);
        processExtraData(batchStatement.getGeneratedKeys());
        batchConnection.close();
    }

    private void debugBatch(String name, int[] results, long startTime) {
        if (Prism.isDebug()) {
            long time = System.currentTimeMillis();
            int inserts = results.length;
            int actual = 0;
            int line = 0;
            for (int i : results) {
                switch (i) {
                    case Statement.EXECUTE_FAILED -> PrismLogHandler.log(name + "Item " + line + " / " + inserts + " failed to execute");
                    case Statement.SUCCESS_NO_INFO -> PrismLogHandler.log(name + "Item " + line + " / "
                            + inserts + " was successful but no info was returned.");
                    default -> actual = actual + i;
                }
                line++;
            }
            PrismLogHandler.log(name + " Batch commit was complete @ " + time
                    + " Taking: " + (time - startTime) + "ms");
            PrismLogHandler.log(name + " Batch insert contained " + actual + " actual v " + inserts + " actions");
        }
    }

    /**
     * Process any extra data associated with the ResultSet.
     *
     * @param keys ResultSet
     */
    private void processExtraData(ResultSet keys) {
        if (extraDataQueue.isEmpty()) {
            return;
        }
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement s = conn.prepareStatement(getExtraDataQuery(), Statement.RETURN_GENERATED_KEYS)
        ) {
            conn.setAutoCommit(false);
            int i = 0;
            while (keys.next()) {
                // todo should not happen
                if (i >= extraDataQueue.size()) {
                    PrismLogHandler.log("Skipping extra data for " + prefix + "data.id " + keys.getLong(1)
                            + " because the queue doesn't have data for it.");
                    continue;
                }

                final Handler a = extraDataQueue.get(i);
                if (a.hasExtraData()) {
                    String serialData = a.serialize();

                    if (serialData != null && !serialData.isEmpty()) {
                        s.setLong(1, keys.getLong(1));
                        s.setString(2, serialData);
                        s.addBatch();
                    }
                } else {
                    PrismLogHandler.debug("Skipping extra data for " + prefix + "data.id " + keys.getLong(1)
                            + " because the queue doesn't have data for it.");
                }

                i++;
            }

            // The main delay is here
            long startTime = System.currentTimeMillis();
            int[] results = s.executeBatch();
            if (conn.isClosed()) {
                PrismLogHandler.log("Prism database error. We have to bail in the middle of building extra "
                        + "data bulk insert query.");
            } else {
                conn.commit();
            }
            debugBatch("Extra", results, startTime);

        } catch (final SQLException e) {
            e.printStackTrace();
            Prism.getInstance().getPrismDataSource().handleDataSourceException(e);
        }
    }

    private void applyToInsert(PreparedStatement s, Handler a, int actionId, int playerId, int worldId,
                               IntPair newIds, IntPair oldIds, Location l) throws SQLException {
        s.setLong(1, a.getUnixEpoch());
        s.setInt(2, actionId);
        s.setInt(3, playerId);
        s.setInt(4, worldId);
        s.setInt(5, newIds.first);
        s.setInt(6, newIds.second);
        s.setInt(7, oldIds.first);
        s.setInt(8, oldIds.second);
        s.setInt(9, l.getBlockX());
        s.setInt(10, l.getBlockY());
        s.setInt(11, l.getBlockZ());
    }

    private String getQuery() {

        return "INSERT INTO " + prefix
                + "data (epoch,action_id,player_id,world_id,block_id,block_subid,old_block_id,old_block_subid,"
                + "x,y,z) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    }

    private String getExtraDataQuery() {
        return "INSERT INTO " + prefix + "data_extra (data_id,data) VALUES (?,?)";
    }

}
