package me.botsko.prism.config;

import io.leangen.geantyref.TypeToken;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.PrismSqlConfig;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created for Prism.
 *
 * @author Narimm on 22/02/2021
 * @since 2.1.8
 */
public class ConfigHandler {

    private static final YamlConfigurationLoader.Builder builder;
    private PrismConfig config;
    private ConfigurationNode main;
    private ConfigurationNode dataSourceConfig;

    static {
        ConfigurateComponentSerializer.Builder serializerBuilder = ConfigurateComponentSerializer.builder();
        ConfigurateComponentSerializer serializer = serializerBuilder.build();
        ConfigurationOptions options =  YamlConfigurationLoader.builder().defaultOptions()
                .header("Prism Config : Generated: " + DateFormat.getInstance().format(new Date()))
                .serializers(serializer.serializers());
        builder = YamlConfigurationLoader.builder().defaultOptions(options).nodeStyle(NodeStyle.BLOCK).indent(2);
    }

    /**
     * Load the Configuration from the provided path.
     * @param path  Path
     */
    public void loadConfiguration(Path path) {
        try {
            main = builder.path(path).build().load();
            config = main.node("prism").get(TypeToken.get(PrismConfig.class),new PrismConfig());
            dataSourceConfig = main.node("datasource");
        } catch (ConfigurateException e) {
            PrismLogHandler.warn(e.getMessage(),e);
            e.printStackTrace();
        }
    }

    /**
     * Applies the dataSource configuration to the configuration node.
     * @param dataSource the DataSource.
     * @param <T> an implementation of PrismSqlConfig.
     */
    public <T extends PrismSqlConfig> void applyDataSourceConfig(PrismDataSource<T> dataSource) {
        try {
            dataSourceConfig.node("type").set(dataSource.getName());
            dataSourceConfig.node("properties")
                    .set(TypeToken.get(dataSource.getConfigurationClass()),dataSource.getConfig());
        } catch (SerializationException e) {
            PrismLogHandler.warn(e.getMessage(),e);
        }
    }

    /**
     * Save the Configuration to the provided path.
     * @param path Path
     */
    public void saveConfiguration(Path path) {
        YamlConfigurationLoader loader =  builder.path(path).build();
        try {
            main.node("prism").set(config);
            main.node("datasource").from(dataSourceConfig);
            loader.save(main);
        } catch (ConfigurateException e) {
            PrismLogHandler.warn(e.getMessage(),e);
        }
    }

    public ConfigurationNode getDataSourceConfig() {
        return dataSourceConfig;
    }

    /**
     * Load the PrismSQlConfig from the ConfigurationNode.
     * @param clazz T extends PrismSQlConfig.class
     * @param dataSourceConfig ConfigurationNode
     * @param <T> PrismSQlConfig
     * @return extends PrismSQlConfig
     */
    public static <T extends PrismSqlConfig> T getDataSourceConfig(Class<T> clazz, ConfigurationNode dataSourceConfig) {
        T defaultObject = null;
        try {
            defaultObject = clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            PrismLogHandler.warn(e.getMessage());
        }
        try {
            return dataSourceConfig.get(TypeToken.get(clazz),defaultObject);
        } catch (ConfigurateException e) {
            PrismLogHandler.warn(e.getMessage());
            return defaultObject;
        }
    }

    public PrismConfig getConfig() {
        return config;
    }
}
