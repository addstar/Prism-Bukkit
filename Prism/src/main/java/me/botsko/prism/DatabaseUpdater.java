package me.botsko.prism;

import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.PrismDataSourceUpdater;
import me.botsko.prism.settings.Settings;

import java.util.ArrayList;

public class DatabaseUpdater {

    private final int currentDbSchemaVersion = 8;
    private final ArrayList<Runnable> updates = new ArrayList<>(currentDbSchemaVersion);

    /**
     * The plugin.
     */
    public DatabaseUpdater(PrismDataSource dataSource) {
        PrismDataSourceUpdater prismDataSourceUpdater = dataSource.getUpdater();
        updates.add(prismDataSourceUpdater::v1_to_v2);
        updates.add(prismDataSourceUpdater::v2_to_v3);
        updates.add(prismDataSourceUpdater::v3_to_v4);
        updates.add(prismDataSourceUpdater::v4_to_v5);
        updates.add(prismDataSourceUpdater::v5_to_v6);
        updates.add(prismDataSourceUpdater::v6_to_v7);
        updates.add(prismDataSourceUpdater::v7_to_v8);
    }

    private int getClientDbSchemaVersion() {
        final String schema_ver = Settings.getSetting("schema_ver");
        if (schema_ver != null) {
            return Integer.parseInt(schema_ver);
        }
        return currentDbSchemaVersion;
    }

    /**
     * Run any queries lower than current currentDbSchemaVersion.
     */
    public void applyUpdates(PrismDataSource dataSource) {

        int clientSchemaVer = getClientDbSchemaVersion();

        for (int i = clientSchemaVer; i < currentDbSchemaVersion; ++i) {
            Runnable update = updates.get(i - 1);

            if (update != null) {
                PrismLogHandler.log("Updating prism schema v" + i + " to v" + (i + 1) + ". This make take a while.");
                update.run();
            }
        }

        // Save current version
        dataSource.setDatabaseSchemaVersion(currentDbSchemaVersion);
        PrismLogHandler.log("Update check complete: Schema v" + currentDbSchemaVersion);
    }
}
