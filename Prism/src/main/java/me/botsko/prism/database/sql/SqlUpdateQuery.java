package me.botsko.prism.database.sql;

import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.UpdateQuery;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SqlUpdateQuery implements UpdateQuery {
    protected final String tableNameData;
    protected String prefix;
    protected PrismDataSource dataSource;

    /**
     * Build a query.
     *
     * @param dataSource
     */
    public SqlUpdateQuery(PrismDataSource dataSource) {
        this.dataSource = dataSource;
        prefix = this.dataSource.getPrefix();
        tableNameData = prefix + "data";
    }

    @Override
    public void updateRollbacked(Handler... handlers) {
        ArrayList<Handler> rollbacked = new ArrayList<>();
        ArrayList<Handler> notRollbacked = new ArrayList<>();
        for (Handler handler : handlers) {
            if (handler.isRollbacked()) {
                rollbacked.add(handler);
            } else {
                notRollbacked.add(handler);
            }
        }
        try {
            Connection conn = dataSource.getConnection();
            Statement s = conn.createStatement();
            String sql = buildRollbackedSql(rollbacked, true);
            if (sql != null) {
                s.executeUpdate(sql);
            }
            sql = buildRollbackedSql(notRollbacked, false);
            if (sql != null) {
                s.executeUpdate(sql);
            }
            s.close();
            conn.close();
        } catch (SQLException e) {
            dataSource.handleDataSourceException(e);
        }
    }

    /**
     * Build a sql for rollbacked update.
     *
     * @return The sql statement. Null if the size of array is 0.
     */
    @Nullable
    protected String buildRollbackedSql(ArrayList<Handler> handlers, boolean rollbacked) {
        int size = handlers.size();
        if (size > 0) {
            StringBuilder sqlBuilder = new StringBuilder("UPDATE " + tableNameData + " SET rollbacked = " + (rollbacked ? "1" : "0") + " WHERE id in (");
            sqlBuilder.append(handlers.get(0).getId());
            for (int i = 1; i < size; i++) {
                sqlBuilder.append(", ").append(handlers.get(i).getId());
            }
            return sqlBuilder.append(")").toString();
        }
        return null;
    }
}
