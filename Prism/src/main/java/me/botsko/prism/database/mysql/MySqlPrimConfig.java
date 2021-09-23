package me.botsko.prism.database.mysql;

import me.botsko.prism.database.sql.PrismSqlConfigImpl;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

/**
 * Created for Prism.
 *
 * @author Narimm on 2/03/2021
 * @since 2.1.8
 */
@ConfigSerializable
public class MySqlPrimConfig extends PrismSqlConfigImpl {

    @Setting("hostname")
    public String hostName = "127.0.0.1";

    public String username = "username";

    public String password = "password";

    public String database = "prism";

    public Integer port = 3306;

    @Setting("useNonStandardSql")
    public boolean useNonStandardSql = false;

}
