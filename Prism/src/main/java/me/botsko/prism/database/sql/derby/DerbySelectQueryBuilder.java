package me.botsko.prism.database.sql.derby;

import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.sql.SqlSelectQueryBuilder;
import me.botsko.prism.utils.TypeUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 23/01/2021.
 */
public class DerbySelectQueryBuilder extends SqlSelectQueryBuilder {

    public DerbySelectQueryBuilder(PrismDataSource<?> dataSource) {
        super(dataSource);
    }

    @Override
    public String getQuery(@Nullable PrismParameters parameters, boolean shouldGroup) {
        return super.getQuery(parameters, shouldGroup);
    }

    @Override
    protected String select() {
        String query = "";
        query += "SELECT ";
        if (shouldGroup) {
            columns.add("MIN(id) AS id");
            columns.add("MIN(epoch) AS epoch");
            columns.add("MIN(action_id) AS action_id");
            columns.add("MIN(player) AS player");
            columns.add("MIN(world_id) AS world_id");
            columns.add("AVG(x) as x");
            columns.add("AVG(y) AS y");
            columns.add("AVG(z) AS z");
            columns.add("MIN(block_id) AS block_id");
            columns.add("MIN(block_subid) AS block_subid");
            columns.add("MIN(old_block_id) AS old_block_id");
            columns.add("MIN(old_block_subid) AS old_block_subid");
            columns.add("MIN(data) AS data");
            columns.add("MIN(player_uuid) AS uuid");
            columns.add("COUNT(*) AS counted");
            columns.add("MIN(DATE({fn TIMESTAMPADD(SQL_TSI_SECOND, epoch, TIMESTAMP('1970-01-01-00.00.00.000000')) }))"
                    + " AS epochdate");
        } else {
            columns.add("id");
            columns.add("epoch");
            columns.add("action_id");
            columns.add("player");
            columns.add("world_id");
            columns.add("x");
            columns.add("y");
            columns.add("z");
            columns.add("block_id");
            columns.add("block_subid");
            columns.add("old_block_id");
            columns.add("old_block_subid");
            columns.add("data");
            columns.add("player_uuid AS uuid");
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
    protected String group() {
        if (shouldGroup) {
            return " GROUP BY " + tableNameData + ".action_id, " + tableNameData + ".player_id, " + tableNameData
                    + ".block_id, ex.data, DATE({fn TIMESTAMPADD(SQL_TSI_SECOND, " + tableNameData
                    + ".epoch, TIMESTAMP('1970-01-01-00.00.00.000000')) })";
        }
        return "";
    }

    @Override
    protected String limit() {
        if (parameters == null) {
            return "";
        }
        if (parameters.getProcessType().equals(PrismProcessType.LOOKUP)) {
            final int limit = parameters.getLimit();
            if (limit > 0) {
                return " FETCH NEXT " + limit + " ROWS ONLY";
            }
        }
        return "";
    }

}
