package me.botsko.prism.database.sql;

import com.zaxxer.hikari.HikariDataSource;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionRegistry;
import me.botsko.prism.database.ActionReportQuery;
import me.botsko.prism.database.BlockReportQuery;
import me.botsko.prism.database.DeleteQuery;
import me.botsko.prism.database.InsertQuery;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.SelectIdQuery;
import me.botsko.prism.database.SelectProcessActionQuery;
import me.botsko.prism.database.SelectQuery;
import me.botsko.prism.database.SettingsQuery;
import me.botsko.prism.database.UpdateQuery;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 8/04/2019.
 */
public abstract class SqlPrismDataSource implements PrismDataSource {

    protected static HikariDataSource database = null;
    protected String name = "unconfigured";
    protected ConfigurationSection section;
    private boolean paused; //when set the datasource will not allow insertions;
    private SettingsQuery settingsQuery = null;
    private String prefix = "prism_";

    /**
     * Constructor.
     * @param section Config
     */
    public SqlPrismDataSource(ConfigurationSection section) {
        this.section = section;
        if (section == null) {
            setPrefix("");
        } else {
            setPrefix(section.getString("prefix"));
        }
        setFile();
        createDataSource();
    }

    public static void updateDefaultConfig(ConfigurationSection section) {
        section.addDefault("useNonStandardSql", false);
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix for the data source.
     * @param prefix String.
     */
    public void setPrefix(String prefix) {
        if (prefix == null) {
            this.prefix = "";
        }
        this.prefix = prefix;
    }

    @Override
    public Connection getConnection() {
        try {
            if (database != null) {
                return database.getConnection();
            }
        } catch (SQLException e) {
            Prism.log("Could not retrieve a connection - with exception");
            return null;
        }
        Prism.log("Could not retrieve a connection");
        return null;
    }

    @Override
    public void rebuildDataSource() {
        // Close pool connections when plugin disables
        if (database != null) {
            try {
                database.getConnection().close();
            } catch (SQLException e) {
                handleDataSourceException(e);
            }
            database = null;
        }
        createDataSource();
    }

    protected boolean attemptToRescueConnection(SQLException e) throws SQLException {
        if (e.getMessage().contains("connection closed")) {
            rebuildDataSource();
            if (database != null) {
                final Connection conn = createDataSource().getConnection();
                return conn != null && !conn.isClosed();
            }
        }
        return false;
    }

    @Override
    public DataSource getDataSource() {
        return database;
    }

    @Override
    public void handleDataSourceException(SQLException e) {
        // Attempt to rescue
        try {
            if (attemptToRescueConnection(e)) {
                return;
            }
        } catch (final SQLException ignored) {
            Prism.warn("Database rescue was unsuccessful.");
        }
        Prism.warn("Database connection error: " + e.getMessage());
        if (e.getMessage().contains("marked as crashed")) {
            final String[] msg = new String[2];
            msg[0] = "If MySQL crashes during write it may corrupt it's indexes.";
            msg[1] = "Try running `CHECK TABLE " + getPrefix() + "data` and then `REPAIR TABLE "
                    + getPrefix() + "data`.";
            Prism.logSection(msg);
        }
        e.printStackTrace();
    }

    /**
     * Setub Db.
     * @param actionRegistry ActionReg.
     */
    public void setupDatabase(ActionRegistry actionRegistry) {
        try (
                Connection  conn = getConnection();
                Statement st = conn.createStatement()
                ) {
            String query = "CREATE TABLE IF NOT EXISTS `" + prefix + "actions` ("
                    + "`action_id` int(10) unsigned NOT NULL AUTO_INCREMENT," + "`action` varchar(25) NOT NULL,"
                    + "PRIMARY KEY (`action_id`)," + "UNIQUE KEY `action` (`action`)"
                    + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";
            st.executeUpdate(query);

            // data
            query = "CREATE TABLE IF NOT EXISTS `" + prefix + "data` ("
                    + "`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT," + "`epoch` int(10) unsigned NOT NULL,"
                    + "`action_id` int(10) unsigned NOT NULL," + "`player_id` int(10) unsigned NOT NULL,"
                    + "`world_id` int(10) unsigned NOT NULL," + "`x` int(11) NOT NULL," + "`y` int(11) NOT NULL,"
                    + "`z` int(11) NOT NULL," + "`block_id` mediumint(5) DEFAULT NULL,"
                    + "`block_subid` mediumint(5) DEFAULT NULL," + "`old_block_id` mediumint(5) DEFAULT NULL,"
                    + "`old_block_subid` mediumint(5) DEFAULT NULL," + "`rollbacked` boolean NOT NULL DEFAULT 0," + "PRIMARY KEY (`id`)," + "KEY `epoch` (`epoch`),"
                    + "KEY  `location` (`world_id`, `x`, `z`, `y`, `action_id`),"
                    + "KEY  `player` (`player_id`)"
                    + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";
            st.executeUpdate(query);

            // extra prism data table (check if it exists first, so we can avoid
            // re-adding foreign key stuff)
            final DatabaseMetaData metadata = conn.getMetaData();
            ResultSet resultSet = metadata.getTables(
                    conn.getCatalog(),
                    conn.getSchema(),
                    prefix + "data_extra",
                    new String[]{"TABLE"}
            );
            if (!resultSet.next()) {

                // extra data
                query = "CREATE TABLE IF NOT EXISTS `" + prefix + "data_extra` ("
                        + "`extra_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,"
                        + "`data_id` bigint(20) unsigned NOT NULL," + "`data` text NULL," + "`te_data` text NULL,"
                        + "PRIMARY KEY (`extra_id`)," + "KEY `data_id` (`data_id`)"
                        + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";
                st.executeUpdate(query);

                // add extra data delete cascade
                query = "ALTER TABLE `" + prefix + "data_extra` ADD CONSTRAINT `" + prefix
                        + "data_extra_ibfk_1` FOREIGN KEY (`data_id`) REFERENCES `" + prefix
                        + "data` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;";
                st.executeUpdate(query);
            }

            // meta
            query = "CREATE TABLE IF NOT EXISTS `" + prefix + "meta` ("
                    + "`id` int(10) unsigned NOT NULL AUTO_INCREMENT," + "`k` varchar(25) NOT NULL,"
                    + "`v` varchar(255) NOT NULL," + "PRIMARY KEY (`id`)" + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";
            st.executeUpdate(query);

            // players
            query = "CREATE TABLE IF NOT EXISTS `" + prefix + "players` ("
                    + "`player_id` int(10) unsigned NOT NULL AUTO_INCREMENT," + "`player` varchar(255) NOT NULL,"
                    + "`player_uuid` binary(16) NOT NULL," + "PRIMARY KEY (`player_id`),"
                    + "UNIQUE KEY `player` (`player`)," + "UNIQUE KEY `player_uuid` (`player_uuid`)"
                    + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";
            st.executeUpdate(query);

            // worlds
            query = "CREATE TABLE IF NOT EXISTS `" + prefix + "worlds` ("
                    + "`world_id` int(10) unsigned NOT NULL AUTO_INCREMENT," + "`world` varchar(255) NOT NULL,"
                    + "PRIMARY KEY (`world_id`)," + "UNIQUE KEY `world` (`world`)"
                    + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";
            st.executeUpdate(query);

            // actions
            cacheActionPrimaryKeys(); // Pre-cache, so we know if we need to
            // populate db
            final String[] actions = actionRegistry.listAll();
            for (final String a : actions) {
                addActionName(a);
            }

            // id map
            query = "CREATE TABLE IF NOT EXISTS `" + prefix + "id_map` (" + "`material` varchar(63) NOT NULL,"
                    + "`state` varchar(255) NOT NULL," + "`block_id` mediumint(5) NOT NULL AUTO_INCREMENT,"
                    + "`block_subid` mediumint(5) NOT NULL DEFAULT 0," + "PRIMARY KEY (`material`, `state`),"
                    + "UNIQUE KEY (`block_id`, `block_subid`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
            st.executeUpdate(query);
        } catch (final SQLException e) {
            handleDataSourceException(e);

            Prism.log("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Add action to db.
     * @param actionName String
     */
    public void addActionName(String actionName) {

        if (Prism.prismActions.containsKey(actionName)) {
            return;
        }
        try (
                Connection conn = database.getConnection();
                PreparedStatement s = conn.prepareStatement("INSERT INTO " + prefix + "actions (action) VALUES (?)",
                        Statement.RETURN_GENERATED_KEYS)
        ) {
            s.setString(1, actionName);
            s.executeUpdate();
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                Prism.log("Registering new action type to the database/cache: " + actionName + " " + rs.getInt(1));
                Prism.prismActions.put(actionName, rs.getInt(1));
            } else {
                throw new SQLException("Insert statement failed - no generated key obtained.");
            }
            rs.close();
        } catch (final SQLException e) {
            handleDataSourceException(e);

        }
    }

    protected void cacheActionPrimaryKeys() {

        try (
                Connection conn = getConnection();
                PreparedStatement s = conn.prepareStatement(
                        "SELECT action_id, action FROM " + prefix + "actions");
                ResultSet rs = s.executeQuery()
                ) {
            while (rs.next()) {
                Prism.debug("Loaded " + rs.getString(2) + ", id:" + rs.getInt(1));
                Prism.prismActions.put(rs.getString(2), rs.getInt(1));
            }

            Prism.debug("Loaded " + Prism.prismActions.size() + " actions into the cache.");

        } catch (final SQLException e) {
            handleDataSourceException(e);
        }
    }


    /**
     * Cache the world keys.
     * @param prismWorlds Map
     */
    @Override
    public void cacheWorldPrimaryKeys(Map<String, Integer> prismWorlds) {

        try (
                Connection conn = getConnection();
                PreparedStatement s = conn.prepareStatement(
                        "SELECT world_id, world FROM " + prefix + "worlds");
                ResultSet rs = s.executeQuery()
        ) {
            while (rs.next()) {
                prismWorlds.put(rs.getString(2), rs.getInt(1));
            }
            Prism.debug("Loaded " + prismWorlds.size() + " worlds into the cache.");
        } catch (final SQLException e) {
            handleDataSourceException(e);
        }
    }

    /**
     * Saves a world name to the database, and adds the id to the cache hashmap.
     */
    public void addWorldName(String worldName) {

        if (Prism.prismWorlds.containsKey(worldName)) {
            return;
        }
        String query = "INSERT INTO `" + prefix + "worlds` (world) VALUES (?)";
        try (
                Connection conn = database.getConnection();
                PreparedStatement s = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            s.setString(1, worldName);
            s.executeUpdate();
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                Prism.log("Registering new world to the database/cache: " + worldName + " " + rs.getInt(1));
                Prism.prismWorlds.put(worldName, rs.getInt(1));
            } else {
                throw new SQLException("Insert statement failed - no generated key obtained.");
            }
            rs.close();
        } catch (final SQLException e) {
            handleDataSourceException(e);
        }
    }

    @Override
    public void dispose() {
        if (database != null) {
            database.close();
        }
        database = null;
    }

    @Override
    public SelectQuery createSelectQuery() {
        return new SqlSelectQueryBuilder(this);
    }

    @Override
    public SelectIdQuery createSelectIdQuery() {
        return new SqlSelectIdQueryBuilder(this);
    }

    @Override
    public DeleteQuery createDeleteQuery() {
        return new SqlDeleteQueryBuilder(this);
    }

    @Override
    public BlockReportQuery createBlockReportQuery() {
        return new SqlBlockReportQueryBuilder(this);
    }

    @Override
    public ActionReportQuery createActionReportQuery() {
        return new SqlActionReportQueryBuilder(this);
    }

    @Override
    public SettingsQuery createSettingsQuery() {
        if (settingsQuery == null) {
            settingsQuery = new SqlSettingsQuery(this);
        }
        return settingsQuery;
    }

    @Override
    public SelectProcessActionQuery createProcessQuery() {
        return new SqlSelectProcessQuery(this);
    }

    @Override
    public UpdateQuery createUpdateQuery() {
        return new SqlUpdateQuery(this);
    }

    public InsertQuery getDataInsertionQuery() {
        return new SqlInsertBuilder(this);
    }
}
