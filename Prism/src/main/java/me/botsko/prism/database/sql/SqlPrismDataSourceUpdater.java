package me.botsko.prism.database.sql;

import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.PrismDataSourceUpdater;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 5/04/2019.
 */
public class SqlPrismDataSourceUpdater implements PrismDataSourceUpdater {
    private final PrismDataSource dataSource;
    private static String  prefix = "prism_";

    public SqlPrismDataSourceUpdater(PrismDataSource dataSource) {
        this.dataSource = dataSource;
        prefix = this.dataSource.getPrefix();
    }

    private static void v7_batch_material(PreparedStatement st, String before, String after) throws SQLException {
        // this "backwards" insert matches the order in the prepared statement
        st.setString(1, after);
        st.setString(2, before);
        st.addBatch();
    }

    public void v1_to_v2() {
    }

    public void v2_to_v3() {
    }

    public void v3_to_v4() {
    }

    public void v4_to_v5() {
    }

    /**
     * Update 5 to 6.
     */
    public void v5_to_v6() {

        String query;
        try (
                Connection conn = dataSource.getConnection();
                Statement st = conn.createStatement()
        ) {

            // Key must be dropped before we can edit colum types
            query = "ALTER TABLE `" + prefix + "data_extra` DROP FOREIGN KEY `" + prefix + "data_extra_ibfk_1`;";
            st.executeUpdate(query);

            query = "ALTER TABLE " + prefix + "data MODIFY id bigint(20) unsigned NOT NULL AUTO_INCREMENT";
            st.executeUpdate(query);

            query = "ALTER TABLE " + prefix + "data_extra MODIFY extra_id bigint(20) unsigned NOT NULL AUTO_INCREMENT,"
                    + " MODIFY data_id bigint(20) unsigned NOT NULL";
            st.executeUpdate(query);

            // return foreign key
            /// BEGIN COPY PASTE Prism.setupDatabase()
            query = "ALTER TABLE `" + prefix + "data_extra` ADD CONSTRAINT `" + prefix
                    + "data_extra_ibfk_1` FOREIGN KEY (`data_id`) REFERENCES `" + prefix
                    + "data` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;";
            st.executeUpdate(query);
            /// END COPY PASTE
        } catch (SQLException e) {
            dataSource.handleDataSourceException(e);
        }
    }

    @Override
    public void v6_to_v7() {
        String query = "UPDATE `" + prefix + "id_map` SET material = ? WHERE material = ?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement st = conn.prepareStatement(query)
        ) {
            v7_batch_material(st, "CACTUS_GREEN", "GREEN_DYE");
            v7_batch_material(st, "DANDELION_YELLOW", "YELLOW_DYE");
            v7_batch_material(st, "ROSE_RED", "RED_DYE");
            v7_batch_material(st, "SIGN", "OAK_SIGN");
            v7_batch_material(st, "WALL_SIGN", "OAK_WALL_SIGN");
            st.executeBatch();
        } catch (SQLException e) {
            dataSource.handleDataSourceException(e);
        }
    }

    @Override
    public void v7_to_v8() {
        // Prepare query to be used
        String query = "ALTER TABLE `" + prefix + "data` ADD INDEX `player` (`player_id`)";

        // Prepare database
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement st = conn.prepareStatement(query)
        ) {
            // Add player index to speed up player lookups.
            st.executeUpdate(query);
        } catch (SQLException e) {
            dataSource.handleDataSourceException(e);
        }
    }

    @Override
    public void v8_to_v9() {
        // Prepare query to be used
        String query = "ALTER TABLE `" + prefix + "data` ADD COLUMN `rollbacked` boolean NOT NULL DEFAULT 0";

        // Prepare database
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement st = conn.prepareStatement(query)
        ) {
            // Add player index to speed up player lookups.
            st.executeUpdate(query);
        } catch (SQLException e) {
            dataSource.handleDataSourceException(e);
        }
    }
}
