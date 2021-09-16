package me.botsko.prism.database;

import me.botsko.prism.actionlibs.ActionRegistryImpl;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.database.sql.PrismSqlConfigImpl;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created for the Prism-Bukkit Project.
 *
 * @author Narimm on 12/02/2021.
 * @since 2.1.8
 */
public class NullDataSource implements PrismDataSource<PrismSqlConfigImpl> {
    @Override
    public boolean isPaused() {
        return true;
    }

    @Override
    public PrismSqlConfigImpl getConfig() {
        return null;
    }

    @Override
    public Class<PrismSqlConfigImpl> getConfigurationClass() {
        return PrismSqlConfigImpl.class;
    }

    @Override
    public void setPaused(boolean paused) {

    }

    @Override
    public String getName() {
        return "Null";
    }

    @Override
    public NullDataSource createDataSource() {
        return this;
    }

    @Override
    public void setupDatabase(ActionRegistryImpl actionRegistry) {

    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public void rebuildDataSource() {

    }

    @Override
    public DataSource getDataSource() {
        return null;
    }

    @Override
    public void handleDataSourceException(SQLException e) {
        e.printStackTrace();
    }

    @Override
    public void cacheWorldPrimaryKeys(Map<String, Integer> prismWorlds) {

    }

    @Override
    public void addWorldName(String worldName) {

    }

    @Override
    public void addActionName(ActionType actionName) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public SelectQuery createSelectQuery() {
        return null;
    }

    @Override
    public SelectIdQuery createSelectIdQuery() {
        return null;
    }

    @Override
    public DeleteQuery createDeleteQuery() {
        return null;
    }

    @Override
    public BlockReportQuery createBlockReportQuery() {
        return null;
    }

    @Override
    public ActionReportQuery createActionReportQuery() {
        return null;
    }

    @Override
    public SettingsQuery createSettingsQuery() {
        return null;
    }

    @Override
    public void setDatabaseSchemaVersion(Integer ver) {

    }

    @Override
    public SelectProcessActionQuery createProcessQuery() {
        return null;
    }

    @Override
    public InsertQuery getDataInsertionQuery() {
        return null;
    }

    @Override
    public PlayerIdentificationQuery getPlayerIdHelper() {
        return null;
    }

    @Override
    public IdMapQuery getIdMapQuery() {
        return null;
    }

    @Override
    public boolean reportDataSource(StringBuilder builder, boolean toHandle) {
        return false;
    }

    @Override
    public PrismDataSourceUpdater getUpdater() {
        return new NullDataSourceUpdater();
    }

    public static class NullDataSourceUpdater implements PrismDataSourceUpdater {

        private NullDataSourceUpdater() {
        }

        @Override
        public void v1_to_v2() {

        }

        @Override
        public void v2_to_v3() {

        }

        @Override
        public void v3_to_v4() {

        }

        @Override
        public void v4_to_v5() {

        }

        @Override
        public void v5_to_v6() {

        }

        @Override
        public void v6_to_v7() {

        }

        @Override
        public void v7_to_v8() {

        }
    }
}
