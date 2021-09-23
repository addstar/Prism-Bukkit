package me.botsko.prism.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.util.PropertyElf;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by Narimm on 1/01/2021.
 */
public class HikariHelper {

    /**
     * Creates the Standard Hikari Properties file.
     * @param propFile File
     * @param dbConfig HikariConfig
     * @param skipCoreValue if true skips the jbdcurl and username and password.
     */
    public static boolean saveHikariConfig(File propFile, HikariConfig dbConfig, boolean skipCoreValue) {
        dbConfig.setPoolName("prism");
        if (dbConfig.getMinimumIdle() < 0) {
            dbConfig.setMinimumIdle(2);
        }
        if (dbConfig.getMaximumPoolSize() < 0) {
            dbConfig.setMaximumPoolSize(10);
        }
        Properties prop = new Properties();
        Set<String> keys = PropertyElf.getPropertyNames(HikariConfig.class);
        for (String k : keys) {
            if(checkConfigValueSave(k,skipCoreValue)){
                Object out = PropertyElf.getProperty(k, dbConfig);
                if (out != null) {
                    prop.setProperty(k, out.toString());
                }
            }
        }
        Properties datasourceProps = dbConfig.getDataSourceProperties();
        Set<Object> names = datasourceProps.keySet();
        for (Object name : names) {
            Object val = datasourceProps.get(name);
            if (val != null) {
                prop.setProperty("dataSource." + name, val.toString());
            }
        }
        if (!propFile.getParentFile().exists() && !propFile.getParentFile().mkdirs()) {
            PrismLogHandler.warn("Prism Directory couldn't be created");
            return false;
        }
        try {
            return savePropertiesFile(prop, propFile);
        } catch (IOException e) {
            PrismLogHandler.warn("Error saving configuration file to " + propFile.getPath(),e);
            return false;
        }
    }

    private static boolean checkConfigValueSave(String value,boolean skipCoreValue){
        switch (value){
            case "dataSourceProperties":
            case "healthCheckProperties":
                return false;
            case "jbdcUrl":
            case "username":
            case "password":
                if(skipCoreValue){
                    return false;
                }
            default:
                return true;
        }
    }


    /**
     * Saves the config to the default dataFolder.
     *
     * @param config HikariConfig
     * @param skipCoreValues boolean
     */
    public static void createPropertiesFile(HikariConfig config,boolean skipCoreValues) {
        File propFile = new File(Prism.getInstance().getDataFolder(), "hikari.properties");
        if (propFile.exists()) {
            HikariConfig old = new HikariConfig(propFile.getPath());
            config.copyStateTo(old);
        }
        saveHikariConfig(propFile,config,skipCoreValues);
    }

    private static boolean savePropertiesFile(Properties prop, File propFile) throws IOException {
        try (OutputStream out = new FileOutputStream(propFile)) {
            prop.store(out, "Prism Hikari Datasource Properties for"
                    + " advanced database Configuration - "
                    + " Username and Password in the primary config"
                    + " overwrite values entered here.");
            PrismLogHandler.log("Database Configuration saved to - " + propFile.getPath());
            return true;
        }
    }

}
