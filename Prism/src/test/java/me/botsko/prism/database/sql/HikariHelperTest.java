package me.botsko.prism.database.sql;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

/**
 * Created for Prism.
 *
 * @author Narimm on 3/03/2021
 * @since 2.1.8
 */
class HikariHelperTest {

    @TempDir
    File tempDirectory;

    @Test
    void createPropertiesFileTest() {
        HikariConfig config = new HikariConfig();
        config.addDataSourceProperty("maximumPoolSize", 10);
        config.addDataSourceProperty("minimumIdle", 2);
        Assertions.assertEquals(2,config.getDataSourceProperties().get("minimumIdle"));
        File file = new File(tempDirectory, "hikari.properties");
        Assertions.assertTrue(HikariHelper.saveHikariConfig(file, config, false));
        HikariConfig dbConfig = new HikariConfig(file.getPath());
        Assertions.assertEquals(config.getMinimumIdle(), dbConfig.getMinimumIdle());
        File file2 = new File(tempDirectory, "hikari2.properties");
        Assertions.assertTrue(HikariHelper.saveHikariConfig(file2, config, true));
        HikariConfig dbConfig2 = new HikariConfig(file.getPath());
        Assertions.assertNull(dbConfig2.getJdbcUrl());
    }
}