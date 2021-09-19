package me.botsko.prism.database.sql;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.actions.PrismProcessAction;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.SelectProcessActionQuery;
import me.botsko.prism.measurement.TimeTaken;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/04/2019.
 */
public class SqlSelectProcessQuery extends SqlSelectQueryBuilder implements SelectProcessActionQuery {
    private boolean getLastID;

    /**
     * Constructor.
     * @param dataSource PrismDataSource
     */
    public SqlSelectProcessQuery(PrismDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void setShouldGroup(boolean shouldGroup) {
        this.shouldGroup = false;
    }

    public void isLastProcessID() {
        getLastID = true;
    }

    protected String select() {
        if (getLastID) {
            return "SELECT id FROM " + prefix + "data JOIN " + prefix + "players p ON p.player_id = " + prefix
                    + "data.player_id ";
        }
        String sql = "SELECT id, action, epoch, world, player, HEX(player_uuid), x, y, z, data FROM " + prefix
                + "data d";
        sql += " INNER JOIN " + prefix + "players p ON p.player_id = d.player_id";
        sql += " INNER JOIN " + prefix + "actions a ON a.action_id = d.action_id";
        sql += " INNER JOIN " + prefix + "worlds w ON w.world_id = d.world_id";
        sql += " LEFT JOIN " + prefix + "data_extra ex ON ex.data_id = d.id ";
        return sql;
    }

    protected String where() {
        if (getLastID) {
            //bit hacky here we are using the id parameter which should generally refer to a player.
            final int action_id = Prism.prismActions.get("prism-process");
            String playerName = parameters.getKeyword();
            return "WHERE action_id = " + action_id + " AND p.player = '" + playerName + "' ";
        }
        //bit hacky here we are using the id parameter which should generally refer to a player.
        final long id = parameters.getId();
        return "WHERE d.id = " + id + " ";
    }

    @Override
    protected String group() {
        return "";
    }

    @Override
    protected String order() {
        if (getLastID) {
            return "ORDER BY id DESC ";
        }
        return "";
    }

    @Override
    protected String limit() {
        if (getLastID) {
            return "LIMIT 1";
        }
        return "";
    }

    @Override
    public QueryResult executeSelect(TimeTaken eventTimer) {
        return super.executeSelect(eventTimer);
    }

    @Override
    public PrismProcessAction executeProcessQuery() {
        if (getLastID) {
            return null;
        }
        final String query = getQuery(parameters, false);
        PrismProcessAction process = null;
        try (
                Connection conn = dataSource.getDataSource().getConnection();
                PreparedStatement s = conn.prepareStatement(query);
                ResultSet rs = s.executeQuery()
        ) {
            if (rs.next()) {
                process = new PrismProcessAction();
                // Set all shared values
                process.setId(rs.getLong("id"));
                process.setActionType(rs.getString("action"));
                process.setUnixEpoch(rs.getLong("epoch"));
                process.setWorld(Bukkit.getWorld(rs.getString("world")));
                process.setSourceName(rs.getString("player"));
                process.setUuid(SqlPlayerIdentificationHelper.uuidFromDbString(rs.getString("HEX(player_uuid)")));
                process.setX(rs.getInt("x"));
                process.setY(rs.getInt("y"));
                process.setZ(rs.getInt("z"));
                process.deserialize(rs.getString("data"));
            }
        } catch (SQLException e) {
            dataSource.handleDataSourceException(e);
        }
        return process;
    }

    @Override
    public long getLastProcessIdQuery() {
        if (getLastID) {
            long id = 0;
            final String query = getQuery(parameters, false);
            try (
                    Connection conn = dataSource.getDataSource().getConnection();
                    PreparedStatement s = conn.prepareStatement(query);
                    ResultSet rs = s.executeQuery()
            ) {
                if (rs.next()) {
                    id = rs.getLong("id");
                }
            } catch (SQLException e) {
                dataSource.handleDataSourceException(e);

            }
            return id;
        }
        return 0;
    }
}
