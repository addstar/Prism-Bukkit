package me.botsko.prism.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.botsko.prism.ApiHandler;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.actionlibs.ActionRegistryImpl;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.config.ConfigHandler;
import me.botsko.prism.database.IdMapQuery;
import me.botsko.prism.database.PlayerIdentificationQuery;
import me.botsko.prism.database.PrismDataSourceUpdater;
import me.botsko.prism.database.SelectQuery;
import me.botsko.prism.database.SettingsQuery;
import me.botsko.prism.database.sql.HikariHelper;
import me.botsko.prism.database.sql.SqlPrismDataSource;
import me.botsko.prism.database.sql.SqlSelectQueryBuilder;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by Narimm on 5/04/2019.
 */
public class MySqlPrismDataSource extends SqlPrismDataSource<MySqlPrimConfig> {

    private static final File propFile = new File(Prism.getInstance().getDataFolder(),
            "hikari.properties");
    private static final HashMap<String, String> dbInfo = new HashMap<>();
    private static HikariConfig dbConfig;
    private final Boolean nonStandardSql;
    private SettingsQuery settingsQuery;
    private MySqlPrimConfig config;

    /**
     * Create a dataSource.
     *
     * @param node Config
     */
    public MySqlPrismDataSource(ConfigurationNode node) {
        super(node);
        setConfig();
        nonStandardSql = config.useNonStandardSql;
        setupDefaultProperties();

        name = "mysql";
    }

    private static void setupDefaultProperties() {
        if (propFile.exists()) {
            PrismLogHandler.log("Configuring Hikari from " + propFile.getName());
            PrismLogHandler.debug("This file will not save the jdbcURL, username or password - these are loaded"
                    + " by default from the standard prism configuration file.  If you set these "
                    + "explicitly in the properties file the settings in the standard config will be"
                    + "ignored.");
            dbConfig = new HikariConfig(propFile.getPath());
        } else {
            dbConfig = new HikariConfig();
            dbConfig.addDataSourceProperty("maximumPoolSize", 10);
            dbConfig.addDataSourceProperty("minimumIdle", 2);
            dbConfig.setMaximumPoolSize(10);
            dbConfig.setMinimumIdle(2);
            dbConfig.addDataSourceProperty("cachePrepStmts", true);
            dbConfig.addDataSourceProperty("prepStmtCacheSize", 250);
            dbConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            dbConfig.addDataSourceProperty("useServerPrepStmts", true);
            HikariHelper.saveHikariConfig(propFile, dbConfig, true);
        }
    }

    @Override
    public SettingsQuery createSettingsQuery() {
        if (settingsQuery == null) {
            settingsQuery = new MySqlSettingsQuery(this);
        }
        return settingsQuery;
    }

    @Override
    public PlayerIdentificationQuery getPlayerIdHelper() {
        if (playerIdHelper == null) {
            playerIdHelper = new MySqlPlayerIdentificationQuery();
        }
        return playerIdHelper;
    }

    @Override
    public IdMapQuery getIdMapQuery() {
        if (idMapQuery == null) {
            idMapQuery = new MySqlIdMapQuery(this);
        }
        return idMapQuery;
    }

    @Override
    public PrismDataSourceUpdater getUpdater() {
        return new MySqlPrismDataSourceUpdater(this);
    }

    /**
     * Setub Db. to schema 8
     *
     * @param actionRegistry ActionReg.
     */
    public void setupDatabase(ActionRegistryImpl actionRegistry) {
        try (
                Connection conn = getConnection();
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
                    + "`old_block_subid` mediumint(5) DEFAULT NULL," + "PRIMARY KEY (`id`),"
                    + "INDEX `epoch` (`epoch`),"
                    + "INDEX  `location` (`world_id`, `x`, `z`, `y`, `action_id`),"
                    + "INDEX  `player` (`player_id`)"
                    + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";
            st.executeUpdate(query);

            // extra prism data table (check if it exists first, so we can avoid
            // re-adding foreign key stuff)
            final DatabaseMetaData metadata = conn.getMetaData();
            ResultSet resultSet;
            resultSet = metadata.getTables(null, null, "" + prefix + "data_extra", null);
            if (!resultSet.next()) {

                // extra data
                query = "CREATE TABLE IF NOT EXISTS `" + prefix + "data_extra` ("
                        + "`extra_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,"
                        + "`data_id` bigint(20) unsigned NOT NULL,"
                        + "`data` text NULL,"
                        + "`te_data` text NULL,"
                        + "PRIMARY KEY (`extra_id`),"
                        + "INDEX `data_id` (`data_id`)"
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

            // id map
            query = "CREATE TABLE IF NOT EXISTS `" + prefix + "id_map` (" + "`material` varchar(63) NOT NULL,"
                    + "`state` varchar(255) NOT NULL," + "`block_id` mediumint(5) NOT NULL AUTO_INCREMENT,"
                    + "`block_subid` mediumint(5) NOT NULL DEFAULT 0," + "PRIMARY KEY (`material`, `state`),"
                    + "UNIQUE KEY (`block_id`, `block_subid`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
            st.executeUpdate(query);
            //finally check if this is a true setup and we are up to date.
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet set = meta.getIndexInfo(null, null, prefix + "data", true, false);
            while (set.next()) {
                String columnName = set.getString("COLUMN_NAME");
                if ("player_id".equals(columnName)) {
                    String indexName = set.getString("INDEX_NAME");
                    if ("player".equals(indexName)) {
                        setDatabaseSchemaVersion(8);
                    }
                }
            }
            // actions
            cacheActionPrimaryKeys(); // Pre-cache, so we know if we need to
            // populate db
            final ActionType[] actions = ActionType.values();
            for (final ActionType a : actions) {
                addActionName(a);
            }
        } catch (final SQLException e) {
            handleDataSourceException(e);
            PrismLogHandler.log("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public MySqlPrimConfig getConfig() {
        return config;
    }

    @Override
    public Class<MySqlPrimConfig> getConfigurationClass() {
        return MySqlPrimConfig.class;
    }

    @Override
    protected void setConfig() {
        this.config = ConfigHandler.getDataSourceConfig(MySqlPrimConfig.class, dataSourceConfig);
        prefix = config.getPrefix();
    }

    @Override
    public MySqlPrismDataSource createDataSource() {
        if (dbConfig.getJdbcUrl() == null) {
            final String dns = "jdbc:mysql://" + this.config.hostName + ":"
                    + this.config.port + "/" + this.config.database
                    + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
            dbConfig.setJdbcUrl(dns);
            dbConfig.setUsername(this.config.username);
            dbConfig.setPassword(this.config.password);
        }
        dbConfig.addHealthCheckProperty("connectivityCheckTimeoutMs", "1000");
        dbConfig.addHealthCheckProperty("expected99thPercentileMs", "10");
        if (Prism.getInstance().monitoring) {
            dbConfig.setMetricRegistry(ApiHandler.monitor.getRegistry());
            dbConfig.setHealthCheckRegistry(ApiHandler.monitor.getHealthRegistry());
            PrismLogHandler.log("Hikari is configured with Metric Reporting.");
        } else {
            PrismLogHandler.log("No metric recorder found to hook into Hikari.");
        }
        try {
            database = new HikariDataSource(dbConfig);
            detectNonStandardSql();
            createSettingsQuery();
            return this;
        } catch (HikariPool.PoolInitializationException e) {
            PrismLogHandler.warn("Hikari Pool did not Initialize: " + e.getMessage());
            database = null;
        }
        return this;
    }

    @Override
    public SelectQuery createSelectQuery() {
        if (nonStandardSql) {
            return new MySqlSelectQueryBuilder(this);
        } else {
            return new SqlSelectQueryBuilder(this);
        }
    }

    private void detectNonStandardSql() {
        try (
                Connection conn = getConnection();
                PreparedStatement st = (conn != null) ? conn.prepareStatement("SHOW VARIABLES") : null;
                PreparedStatement st1 = (conn != null) ? conn.prepareStatement("SELECT ANY_VALUE(1)") : null;
                ResultSet rs = (st != null) ? st.executeQuery() : null;
                ResultSet rs1 = (st1 != null) ? st1.executeQuery() : null

        ) {
            if (rs == null || rs1 == null) {
                throw new SQLNonTransientConnectionException("Database did not configure correctly.");
            }
            while (rs.next()) {
                dbInfo.put(rs.getString(1).toLowerCase(), rs.getString(2));
            }
            rs1.next();
            String version = dbInfo.get("version");
            String versionComment = dbInfo.get("version_comment");
            PrismLogHandler.log("Prism detected you database is version:" + version + " / " + versionComment);
            PrismLogHandler.log("You have set nonStandardSql to " + nonStandardSql);
            PrismLogHandler.log("You are able to use non standard SQL");
            if (!nonStandardSql) {
                PrismLogHandler.log("Prism will use standard sql queries");
            }
        } catch (SQLNonTransientConnectionException e) {
            PrismLogHandler.warn(e.getMessage());
        } catch (SQLException e) {
            PrismLogHandler.log("You are not able to use non standard Sql -"
                    + "extended SQL available is MySQL based servers");
            if (nonStandardSql) {
                PrismLogHandler.log("This sounds like a configuration error.  If you have database access"
                        + "errors please set nonStandardSql to false");
            }
        }
    }
}
