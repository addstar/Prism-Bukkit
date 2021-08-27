package me.botsko.prism.database.mysql;

import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.sql.SqlSelectQueryBuilder;
import me.botsko.prism.utils.TypeUtils;

public class MySqlSelectQueryBuilder extends SqlSelectQueryBuilder {

    public MySqlSelectQueryBuilder(PrismDataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String select() {

        String query = "";

        query += "SELECT ";
        if (shouldGroup) {
            columns.add("any_value(id) id");
            columns.add("any_value(epoch) epoch");
            columns.add("any_value(action_id) action_id");
            columns.add("any_value(player) player");
            columns.add("any_value(world_id) world_id");
        } else {
            columns.add("id");
            columns.add("epoch");
            columns.add("action_id");
            columns.add("player");
            columns.add("world_id");
        }

        if (shouldGroup) {
            columns.add("AVG(x)");
            columns.add("AVG(y)");
            columns.add("AVG(z)");
        } else {
            columns.add("x");
            columns.add("y");
            columns.add("z");
        }

        columns.add("any_value(block_id) block_id");
        columns.add("any_value(block_subid) block_subid");
        columns.add("any_value(old_block_id) old_block_id");
        columns.add("any_value(old_block_subid) old_block_subid");
        columns.add("MAX(rollbacked) rollbacked");
        columns.add("any_value(data) data");
        columns.add("any_value(HEX(player_uuid)) AS uuid");

        if (shouldGroup) {
            columns.add("COUNT(*) counted");
        }

        // Append all columns
        if (columns.size() > 0) {
            query += TypeUtils.join(columns, ", ");
        }

        // From
        query += " FROM " + tableNameData + " ";

        // Joins
        query += "INNER JOIN " + prefix + "players p ON p.player_id = " + tableNameData + ".player_id ";
        query += "LEFT JOIN " + tableNameDataExtra + " ex ON ex.data_id = " + tableNameData + ".id ";

        return query;

    }

    @Override
    protected String order() {
        if (parameters == null) {
            return " ";
        }
        final String sort_dir = parameters.getSortDirection();

        if (shouldGroup) {
            return " ORDER BY MAX(" + tableNameData + ".epoch) " + sort_dir
                    + ", AVG(x) ASC, AVG(z) ASC, AVG(y) ASC, any_value(id) " + sort_dir;
        }

        return " ORDER BY " + tableNameData + ".epoch " + sort_dir + ", x ASC, z ASC, y ASC, id " + sort_dir;
    }
}