package me.botsko.prism.database;

import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.database.mysql.MySqlPrismDataSource;
import me.botsko.prism.database.sql.derby.DerbySqlPrismDataSource;
import me.botsko.prism.settings.Settings;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 4/04/2019.
 */
public class PrismDatabaseFactory {

    /**
     * Create a config.
     *
     * @param dataSourceSection ConfigurationSection
     */
    public static void createDefaultConfig(final ConfigurationNode dataSourceSection) throws SerializationException {
        String dataType = dataSourceSection.node("type").getString("derby");
        dataSourceSection.node("type").set(dataType);
    }

    /**
     * Construct Data source.
     *
     * @param dataSourceSection ConfigurationSection
     * @return PrismDataSource
     */
    @SuppressWarnings("CheckStyle")
    public static PrismDataSource createDataSource(ConfigurationNode dataSourceSection) {
        PrismDataSource database;
        String dataSource = dataSourceSection.node("type").getString();
        ConfigurationNode dataSourceProperties = dataSourceSection.node("properties");
        if (dataSource == null) {
            dataSource = "null";
        }
        switch (dataSource) {
            case "mysql":
                PrismLogHandler.log("Attempting to configure datasource as mysql");
                database = new MySqlPrismDataSource(dataSourceProperties);
                break;
            case "sqlite":
                PrismLogHandler.warn("ERROR: This version of Prism no longer supports SQLite.");
            case "null":
                database = new NullDataSource();
                break;
            case "hikari":
            case "derby":
                database = new DerbySqlPrismDataSource(dataSourceProperties);
                PrismLogHandler.log("Attempting to configure datasource as " + dataSource);
                PrismLogHandler.log("HIKARI: prism will configure itself using the hikari parameters");
                break;
            default:
                PrismLogHandler.warn("ERROR: This version of Prism no longer supports " + dataSource);
                PrismLogHandler.log("Attempting to configure datasource as hikari using derby");
                database = new DerbySqlPrismDataSource(dataSourceProperties);
                PrismLogHandler.log("HIKARI: prism will configure itself using the hikari parameters");
                break;
        }
        database.createDataSource();
        Settings.setDataSource(database);
        return database;
    }
}
