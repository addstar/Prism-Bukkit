package me.botsko.prism.database.mysql;

import me.botsko.prism.database.sql.SqlPrismDataSource;
import me.botsko.prism.database.sql.SqlSettingsQuery;

/**
 * Created for the Prism-Bukkit Project.
 *
 * @author Narimm on
 * @since 22/01/2021
 */
public class MySqlSettingsQuery extends SqlSettingsQuery {

    public MySqlSettingsQuery(SqlPrismDataSource dataSource) {
        super(dataSource);
    }

    protected String getSelectQuery() {
        return "SELECT v FROM " + prefix + "meta WHERE k = ? LIMIT 0,1;";
    }

    protected String getInsertQuery() {
        return "INSERT INTO " + prefix + "meta (k , v) VALUES (?,?)";
    }
}
