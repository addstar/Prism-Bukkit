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
import me.botsko.prism.database.*;
import me.botsko.prism.database.sql.HikariHelper;
import me.botsko.prism.database.sql.SqlPrismDataSource;
import me.botsko.prism.database.sql.SqlSelectQueryBuilder;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.sql.*;
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
            st.executeUpdate(getFormattedSql("mysql_actions"));
            st.executeUpdate(getFormattedSql("mysql_data"));

            // extra prism data table (check if it exists first, so we can avoid
            // re-adding foreign key stuff)
            final DatabaseMetaData metadata = conn.getMetaData();
            ResultSet resultSet;
            resultSet = metadata.getTables(null, null, "" + prefix + "_data_extra", null);
            if (!resultSet.next()) {
                // extra data
                st.executeUpdate(getFormattedSql("mysql_data_extra"));
                // add extra data delete cascade
                st.executeUpdate(getFormattedSql("mysql_alter_data_extra"));
            }
            // meta
            st.executeUpdate(getFormattedSql("mysql_create_meta"));
            // players
            st.executeUpdate(getFormattedSql("mysql_create_players"));
            // worlds
            st.executeUpdate(getFormattedSql("mysql_create_worlds"));
            // id map
            st.executeUpdate(getFormattedSql("mysql_id_map"));
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
