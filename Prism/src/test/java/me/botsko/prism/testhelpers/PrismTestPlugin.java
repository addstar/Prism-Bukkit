package me.botsko.prism.testhelpers;

import io.papermc.lib.PaperLib;
import me.botsko.prism.DatabaseUpdater;
import me.botsko.prism.Messenger;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.TaskManager;
import me.botsko.prism.actionlibs.ActionRegistryImpl;
import me.botsko.prism.actionlibs.HandlerRegistry;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.players.PlayerIdentification;
import me.botsko.prism.settings.Settings;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created for the Prism-Bukkit Project.
 *
 * @author Narimm on
 * @since 25/01/2021
 */
public class PrismTestPlugin extends Prism {

    protected PrismTestPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        instance = this;
    }

    protected PrismTestPlugin(PrismDataSource source, JavaPluginLoader loader, PluginDescriptionFile description,
                              File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        prismDataSource = source;
        instance = this;
    }

    @Override
    public void onEnable() {
        logHandler = new PrismLogHandler();
        pluginName = this.getDescription().getName();
        pluginVersion = this.getDescription().getVersion();
        loadConfig();        // Load configuration, or install if new
        messenger = new Messenger(pluginName, BukkitAudiences.create(this));
        taskManager = new TaskManager(Bukkit.getScheduler(),this);

        PrismLogHandler.log("Initializing Prism " + pluginVersion
                + ". Originally by Viveleroi; maintained by the AddstarMC Network");
        config.allowMetrics = false;
        config.preloadMaterials = false;
        debug = config.debug = true;
        ConfigurationNode dataSourceConfig = configHandler.getDataSourceConfig();
        try {
            dataSourceConfig.node("type").set("derby");
        } catch (SerializationException exception) {
            exception.printStackTrace();
        }
        isPaper = PaperLib.isPaper();
        checkPluginDependencies();
        pasteKey = null;
        final List<String> worldNames = getServer().getWorlds().stream()
                .map(World::getName).collect(Collectors.toList());
        final String[] playerNames = getServer().getOnlinePlayers().stream()
                .map(Player::getName).toArray(String[]::new);
        // init db async then call back to complete enable.
        final BukkitTask updating = getServer().getScheduler().runTaskTimerAsynchronously(instance, () -> {
            if (!isEnabled()) {
                PrismLogHandler.warn("Prism is loading and updating the database; logging is NOT enabled");
            }
        }, 100, 200);
        if (prismDataSource == null) {
            prismDataSource = new TestPrismDataSource(dataSourceConfig);
            prismDataSource.createDataSource();
        }
        StringBuilder builder = new StringBuilder();
        if (!prismDataSource.reportDataSource(builder,true)) {
            PrismLogHandler.warn(builder.toString());
            notifyDisabled();
            enableFailedDatabase();
            updating.cancel();
            return;
        }
        // Info needed for setup, init these here
        Settings.setDataSource(prismDataSource);
        handlerRegistry = new HandlerRegistry();
        actionRegistry = new ActionRegistryImpl();
        playerIdentifier = new PlayerIdentification(prismDataSource);

        // Setup databases
        prismDataSource.setupDatabase(actionRegistry);

        // Cache world IDs
        prismDataSource.cacheWorldPrimaryKeys(prismWorlds);
        prismDataSource.getPlayerIdHelper().cacheOnlinePlayerPrimaryKeys(playerNames);

        // ensure current worlds are added
        for (final String w : worldNames) {
            if (!Prism.prismWorlds.containsKey(w)) {
                prismDataSource.addWorldName(w);
            }
        }
        // Apply any updates
        final DatabaseUpdater up = new DatabaseUpdater(prismDataSource);
        up.applyUpdates(prismDataSource);
        Bukkit.getScheduler().runTask(instance, () -> instance.enabled());
        updating.cancel();
    }

}
