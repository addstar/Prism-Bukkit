package me.botsko.prism.database.sql.derby;

import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.sql.SqlIdMapQuery;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 23/01/2021.
 */
public class DerbySqlIdMapQuery extends SqlIdMapQuery {

    /**
     * Constructor.
     *
     * @param dataSource PrismDataSource
     */
    public DerbySqlIdMapQuery(PrismDataSource<?> dataSource) {
        super(dataSource);
    }

    protected  String getToIds() {
        return "SELECT block_id, block_subid FROM "
              + prefix + "id_map WHERE material=? AND state=? FETCH FIRST ROW ONLY";
    }

    protected  String getToAllIds() {
        return "SELECT block_id, block_subid FROM " + prefix + "id_map WHERE material=?";
    }

    protected  String getPartialToAllIds() {
        return "SELECT block_id, block_subid FROM " + prefix + "id_map WHERE material=? AND state LIKE ?";
    }

    protected  String getToMat() {
        return "SELECT material, state FROM "
              + prefix + "id_map WHERE block_id=? AND block_subid=? FETCH FIRST ROW ONLY";
    }

    protected  String getMap() {
        return "INSERT INTO " + prefix + "id_map(material, state, block_id, block_subid) "
                + "VALUES (?, ?, ?, ?)";
    }

    protected  String getAutomap() {
        return "INSERT INTO " + prefix + "id_map(material, state) VALUES (?, ?)";
    }

    protected  String getRepair() {
        return "UPDATE " + prefix + "id_map SET block_id=?, block_subid=? WHERE block_id=?";
    }

    protected String getUnauto() {
        return "ALTER TABLE " + prefix + "id_map AUTO_INCREMENT=?";
    }
}
