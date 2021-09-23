package me.botsko.prism.database.sql.derby;

import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.sql.SqlSelectIdQueryBuilder;

/**
 * Created for the Prism-Bukkit Project.
 *
 * @author Narimm on 12/02/2021
 */
public class DerbySelectIdQueryBuilder extends SqlSelectIdQueryBuilder {

    public DerbySelectIdQueryBuilder(PrismDataSource<?> dataSource) {
        super(dataSource);
    }

    @Override
    protected String limit() {
        if (parameters == null) {
            return "";
        }
        if (parameters.getProcessType().equals(PrismProcessType.LOOKUP)) {
            final int limit = parameters.getLimit();
            if (limit == 1) {
                return "FETCH FIRST ROW ONLY";
            }
            if (limit > 1) {
                return " FETCH NEXT " + limit + " ROWS ONLY";
            }
        }
        return "";
    }


}
