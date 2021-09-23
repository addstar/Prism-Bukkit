package me.botsko.prism.testhelpers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.database.sql.HikariHelper;
import me.botsko.prism.database.sql.derby.DerbySqlPrismDataSource;
import org.spongepowered.configurate.ConfigurationNode;
import java.util.Random;

/**
 * Created for the Prism-Bukkit Project.
 *
 * @author Narimm on
 */
public class TestPrismDataSource extends DerbySqlPrismDataSource {

    private static final Random random = new Random();

    /**
     * Constructor.
     *
     * @param section Config
     */
    public TestPrismDataSource(ConfigurationNode section) {
        super(section);
        setPrefix("prism_");
    }

    @Override
    protected void setUpHikariProperties() {
        dbConfig = new HikariConfig();
        dbConfig.setUsername("prism");
        dbConfig.setPassword("password");
        dbConfig.setJdbcUrl("jdbc:derby:memory:test" + random.nextInt(100) + ";create=true");
        if (Prism.getInstance() != null) {
            HikariHelper.createPropertiesFile(dbConfig,false);
        }
        reportJdbcDrivers();
    }

    @Override
    public TestPrismDataSource createDataSource() {
        try {
            database = new HikariDataSource(dbConfig);
            createSettingsQuery();
            return this;
        } catch (HikariPool.PoolInitializationException e) {
            PrismLogHandler.warn("Hikari Pool did not Initialize: " + e.getMessage());
            database = null;
        }
        return this;
    }
}


