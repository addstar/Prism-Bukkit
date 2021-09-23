package me.botsko.prism.database.sql.derby;

import me.botsko.prism.database.sql.PrismSqlConfigImpl;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Created for Prism.
 *
 * @author Narimm on 12/03/2021
 * @since 2.1.8
 */

@ConfigSerializable
public class DerbySqlConfig extends PrismSqlConfigImpl {

    public String databaseName = "derby";

    public String userName = "PRISM";

    public String password = "PRISM";
}
