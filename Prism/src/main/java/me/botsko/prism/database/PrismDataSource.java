package me.botsko.prism.database;

import me.botsko.prism.actionlibs.ActionRegistryImpl;
import me.botsko.prism.api.actions.ActionType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 5/04/2019.
 */
public interface PrismDataSource<T extends PrismSqlConfig> {

    boolean isPaused();

    T getConfig();

    default Class<T> getConfigurationClass() {
        return (Class<T>) getConfig().getClass();
    }

    void setPaused(boolean paused);

    String getName();

    default String getPrefix() {
        return "prism_";
    }

    PrismDataSource<?> createDataSource();

    void setupDatabase(ActionRegistryImpl actionRegistry);

    Connection getConnection();

    void rebuildDataSource();


    DataSource getDataSource();

    void handleDataSourceException(SQLException e);

    void cacheWorldPrimaryKeys(Map<String, Integer> prismWorlds);

    void addWorldName(String worldName);

    void addActionName(ActionType actionName);

    void dispose();

    SelectQuery createSelectQuery();

    SelectIdQuery createSelectIdQuery();

    DeleteQuery createDeleteQuery();

    BlockReportQuery createBlockReportQuery();

    ActionReportQuery createActionReportQuery();

    SettingsQuery createSettingsQuery();

    void setDatabaseSchemaVersion(Integer ver);

    SelectProcessActionQuery createProcessQuery();

    InsertQuery getDataInsertionQuery();

    PlayerIdentificationQuery getPlayerIdHelper();

    IdMapQuery getIdMapQuery();

    default boolean reportDataSource(StringBuilder builder) {
        return reportDataSource(builder,false);
    }

    boolean reportDataSource(StringBuilder builder, boolean toHandle);

    PrismDataSourceUpdater getUpdater();
}
