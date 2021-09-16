package me.botsko.prism.database.sql;

import me.botsko.prism.database.PrismSqlConfig;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Created for Prism.
 *
 * @author Narimm on 2/03/2021
 * @since 2.1.8
 */
@ConfigSerializable
public class PrismSqlConfigImpl implements PrismSqlConfig {

    public PrismSqlConfigImpl() {
    }

    public String getPrefix() {
        return prefix;
    }

    public String prefix = "prism_";

}
