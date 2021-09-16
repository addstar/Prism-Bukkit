package me.botsko.prism.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.database.PrismSqlConfig;
import org.spongepowered.configurate.ConfigurationNode;
import java.io.File;
import java.sql.Driver;
import java.util.Enumeration;
import static java.sql.DriverManager.getDrivers;


/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 1/01/2021.
 */
public abstract class PrismHikariDataSource<T extends PrismSqlConfig> extends SqlPrismDataSource<T> {


    protected File propFile;
    protected HikariConfig dbConfig;


    /**
     * Create a dataSource.
     *
     * @param node Config
     */
    public PrismHikariDataSource(ConfigurationNode node) {
        super(node);
        name = "hikari";
        setUpHikariProperties();
    }

    protected void setUpHikariProperties() {
        if (propFile == null) {
            propFile = new File(Prism.getInstance().getDataFolder(), "hikari.properties");
        }
        if (propFile.exists()) {
            PrismLogHandler.log("Configuring Hikari from " + propFile.getName());
            dbConfig = new HikariConfig(propFile.getPath());
        } else {
            PrismLogHandler.log("You may need to adjust these settings for your setup.");
            String jdbcUrl = "jdbc:mysql://localhost:3306/prism?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
            PrismLogHandler.log("Default jdbcUrl: " + jdbcUrl);
            PrismLogHandler.log("Default Username: username");
            PrismLogHandler.log("Default Password: password");
            PrismLogHandler.log("You will need to provide the required jar libraries that support your database.");
            dbConfig = new HikariConfig();
            dbConfig.setJdbcUrl(jdbcUrl);
            dbConfig.setUsername("username");
            dbConfig.setPassword("password");
            HikariHelper.saveHikariConfig(propFile, dbConfig, false);
            reportJdbcDrivers();
        }
    }

    protected void reportJdbcDrivers() {
        PrismLogHandler.log("Available Drivers:");
        Enumeration<Driver> set = getDrivers();
        while (set.hasMoreElements()) {
            Driver driver = set.nextElement();
            PrismLogHandler.log("   " + driver.getClass().getName());
        }
    }

    @Override
    public PrismHikariDataSource<T> createDataSource() {
        try {
            database = new HikariDataSource(dbConfig);
            createSettingsQuery();
            return this;
        } catch (HikariPool.PoolInitializationException e) {
            PrismLogHandler.warn("Hikari Pool did not Initialize: " + e.getMessage());
            database = null;
        } catch (Exception e) {
            PrismLogHandler.warn("General Exception: " + e.getMessage(),e);
            database = null;
        }
        return this;
    }



}
