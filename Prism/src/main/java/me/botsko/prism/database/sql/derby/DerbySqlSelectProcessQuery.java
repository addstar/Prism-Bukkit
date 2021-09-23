package me.botsko.prism.database.sql.derby;

import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.sql.SqlSelectProcessQuery;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 23/01/2021.
 */
public class DerbySqlSelectProcessQuery extends SqlSelectProcessQuery {
    /**
     * Constructor.
     *
     * @param dataSource PrismDataSource
     */
    public DerbySqlSelectProcessQuery(PrismDataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String limit() {
        if (getLastID) {
            return " FETCH FIRST ROW ONLY ";
        }
        return " ";
    }
}
