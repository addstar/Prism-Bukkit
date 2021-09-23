package me.botsko.prism;

import io.papermc.lib.PaperLib;
import me.botsko.prism.actionlibs.ActionRegistryImpl;
import me.botsko.prism.actionlibs.ActionsQuery;
import me.botsko.prism.actionlibs.HandlerRegistry;
import me.botsko.prism.actionlibs.Ignore;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.actionlibs.QueueDrain;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.actions.ActionMeter;
import me.botsko.prism.api.PrismApi;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.api.Result;
import me.botsko.prism.api.actions.ActionRegistry;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.appliers.PreviewSession;
import me.botsko.prism.commands.PrismCommands;
import me.botsko.prism.commands.WhatCommand;
import me.botsko.prism.config.ConfigHandler;
import me.botsko.prism.config.PrismConfig;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.PrismDatabaseFactory;
import me.botsko.prism.events.EventHelper;
import me.botsko.prism.listeners.PaperListeners;
import me.botsko.prism.listeners.PrismBlockEvents;
import me.botsko.prism.listeners.PrismCustomEvents;
import me.botsko.prism.listeners.PrismEntityEvents;
import me.botsko.prism.listeners.PrismInventoryEvents;
import me.botsko.prism.listeners.PrismInventoryMoveItemEvent;
import me.botsko.prism.listeners.PrismPlayerEvents;
import me.botsko.prism.listeners.PrismVehicleEvents;
import me.botsko.prism.listeners.PrismWorldEvents;
import me.botsko.prism.listeners.self.PrismMiscEvents;
import me.botsko.prism.measurement.QueueStats;
import me.botsko.prism.measurement.TimeTaken;
import me.botsko.prism.monitors.OreMonitor;
import me.botsko.prism.monitors.UseMonitor;
import me.botsko.prism.parameters.ActionParameter;
import me.botsko.prism.parameters.BeforeParameter;
import me.botsko.prism.parameters.BlockParameter;
import me.botsko.prism.parameters.EntityParameter;
import me.botsko.prism.parameters.FlagParameter;
import me.botsko.prism.parameters.IdParameter;
import me.botsko.prism.parameters.KeywordParameter;
import me.botsko.prism.parameters.PlayerParameter;
import me.botsko.prism.parameters.PrismParameterHandler;
import me.botsko.prism.parameters.RadiusParameter;
import me.botsko.prism.parameters.SinceParameter;
import me.botsko.prism.parameters.WorldParameter;
import me.botsko.prism.players.PlayerIdentification;
import me.botsko.prism.players.PrismPlayer;
import me.botsko.prism.settings.Settings;
import me.botsko.prism.utils.MaterialAliases;
import me.botsko.prism.wands.Wand;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Prism extends JavaPlugin implements PrismApi {

    public static final ConcurrentHashMap<String, Wand> playersWithActiveTools = new ConcurrentHashMap<>();
    public static final HashMap<String, Integer> prismWorlds = new HashMap<>();
    private static final Map<Material, TextColor> alertedOres = new HashMap<>();
    private static final HashMap<String, PrismParameterHandler> paramHandlers = new HashMap<>();
    private static final String baseUrl = "https://prism-bukkit.readthedocs.io/en/latest/";
    public static Messenger messenger;
    protected ConfigHandler configHandler;
    public PrismConfig config;
    public static boolean isPaper = true;
    protected static PrismLogHandler logHandler;
    protected PrismDataSource<?> prismDataSource = null;
    protected static String pluginName;
    protected static String pasteKey;
    protected static ActionRegistryImpl actionRegistry;
    protected static HandlerRegistry handlerRegistry;
    protected static Prism instance;
    protected static boolean debug = false;
    private static EnumSet<Material> illegalBlocks;
    private static EnumSet<EntityType> illegalEntities;
    private static MaterialAliases items;
    private static Ignore ignore;
    private static BukkitTask debugWatcher;
    public final ConcurrentHashMap<String, PreviewSession> playerActivePreviews = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, ArrayList<Block>> playerActiveViews = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, QueryResult> cachedQueries = new ConcurrentHashMap<>();
    public final Map<Location, Long> alertedBlocks = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, String> preplannedVehiclePlacement = new ConcurrentHashMap<>();
    /**
     * We store a basic index of hanging entities we anticipate will fall, so that
     * when they do fall we can attribute them to the player who broke the original
     * block.
     */
    public final ConcurrentHashMap<String, String> preplannedBlockFalls = new ConcurrentHashMap<>();
    protected TaskManager taskManager;
    public boolean monitoring = false;
    public OreMonitor oreMonitor;
    public UseMonitor useMonitor;
    public TimeTaken eventTimer;
    public QueueStats queueStats;
    protected PlayerIdentification playerIdentifier;
    protected String pluginVersion;
    private PrismCommands commands = null;

    public Prism() {
        instance = this;
    }

    protected Prism(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        instance = this;
    }

    @Override
    public void reloadConfig() {
        configHandler.loadConfiguration(getDataFolder().toPath().resolve("config.yml"));
    }

    @Override
    public void saveConfig() {
        if (prismDataSource != null) {
            configHandler.applyDataSourceConfig(prismDataSource);
        }
        configHandler.saveConfiguration(getDataFolder().toPath().resolve("config.yml"));
    }

    @Override
    public void saveDefaultConfig() {
        //ignore
    }


    public TaskManager getTaskManager() {
        return taskManager;
    }

    public static boolean isDebug() {
        return debug;
    }

    /**
     * Set the debug state.
     *
     * @param debug bool.
     */
    public static void setDebug(boolean debug) {
        Prism.debug = debug;
        PrismLogHandler.setDebug(debug);
        if (debug && (debugWatcher == null || debugWatcher.isCancelled())) {
            PrismLogHandler.debug("ALERT : Prism has debug mode enabled - LOGS will rapidly grow!!!");
            debugWatcher = Bukkit.getScheduler().runTaskTimerAsynchronously(Prism.getInstance(), () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("prism.debug")) {
                        if (Prism.messenger != null) {
                            Prism.messenger.sendMessage(p, Component.text("ALERT : Prism has debug mode enabled - "
                                    + " LOGS will rapidly grow!!!").color(NamedTextColor.RED));
                        }
                    }
                }
            }, 500, 4000);
        } else {
            if (debugWatcher != null) {
                debugWatcher.cancel();
            }
        }
    }

    public PrismDataSource<?> getPrismDataSource() {
        return this.prismDataSource;
    }

    public static String getPasteKey() {
        return pasteKey;
    }

    /**
     * Get the plugin name.
     *
     * @return String
     */
    public static String getPrismName() {
        return pluginName;
    }

    /**
     * Get a list of illegal materials.
     *
     * @return List of Blocks
     */
    public static EnumSet<Material> getIllegalBlocks() {
        return illegalBlocks;
    }

    /**
     * Get List of illegal entities.
     *
     * @return List
     */
    public static EnumSet<EntityType> getIllegalEntities() {
        return illegalEntities;
    }

    /**
     * Get List of Ores to alert.
     *
     * @return list
     */
    public static Map<Material, TextColor> getAlertedOres() {
        return alertedOres;
    }

    /**
     * Get material aliases.
     *
     * @return MaterialAliases
     */
    public static MaterialAliases getItems() {
        return items;
    }

    @Override
    public ActionRegistry getActionRegistry() {
        return Prism.getActionRegistryImpl();
    }

    @Override
    public void handleCustomAction(Handler handler) {
        if (handler.getAction().getActionType() == ActionType.CUSTOM_ACTION) {
            if (actionRegistry.getCustomAction(handler.getAction().getName()) != null) {
                RecordingQueue.addToQueue(handler);
            }
        }
    }

    /**
     * Get the Action Registry.
     *
     * @return ActionRegistryImpl
     */
    public static ActionRegistryImpl getActionRegistryImpl() {
        return actionRegistry;
    }

    /**
     * Get the HandlerRegistry.
     *
     * @return HandlerRegistry
     */
    public static HandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
    }

    /**
     * Ignore.
     *
     * @return Ignore.
     */
    public static Ignore getIgnore() {
        return ignore;
    }

    /**
     * Registers a parameter and a handler. Example:
     * pr l a:block-break. The "a" is an action, and the action handler will process what
     * "block-break" refers to.
     *
     * @param handler PrismParameterHandler.
     */
    @SuppressWarnings("WeakerAccess")
    public static void registerParameter(PrismParameterHandler handler) {
        paramHandlers.put(handler.getName().toLowerCase(), handler);
    }

    /**
     * Map of Strings and PrismParameterHandlers.
     *
     * @return HashMap
     */
    public static HashMap<String, PrismParameterHandler> getParameters() {
        return paramHandlers;
    }

    /**
     * PrismParameterHandler.
     *
     * @return PrismParameterHandler
     */
    public static PrismParameterHandler getParameter(String name) {
        return paramHandlers.get(name);
    }

    public static Prism getInstance() {
        return instance;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Get an instance of PrismPlayers if Prism initialized.
     *
     * @return Map
     */
    public static Map<UUID, PrismPlayer> getPrismPlayers() {
        if (Prism.getInstance() == null) {
            return Collections.emptyMap();
        }
        return Prism.getInstance().playerIdentifier.getPrismPlayers();
    }

    public PlayerIdentification getPlayerIdentifier() {
        return playerIdentifier;
    }

    /**
     * Enables the plugin and activates our player listeners.
     */
    @Override
    public void onEnable() {
        pluginName = this.getDescription().getName();
        pluginVersion = this.getDescription().getVersion();
        logHandler = new PrismLogHandler();
        loadConfig();
        debug = config.debug;
        taskManager = new TaskManager(Bukkit.getScheduler(), this);
        messenger = new Messenger(pluginName, BukkitAudiences.create(this));
        PrismLogHandler.log("Initializing Prism " + pluginVersion
                + ". Originally by Viveleroi; maintained by the AddstarMC Network");
        isPaper = PaperLib.isPaper();
        if (isPaper) {
            PrismLogHandler.log("Optional Paper Events will be enabled.");
        } else {
            PrismLogHandler.log("Paper not detected - Optional Paper Events will NOT be enabled.");
        }
        checkPluginDependencies();
        if (config.pasteConfig.enabled) {
            pasteKey = config.pasteConfig.apiKey;
            if (pasteKey != null && (pasteKey.startsWith("API key") || pasteKey.length() < 6)) {
                pasteKey = null;
            } else {
                PrismLogHandler.log("PasteApi is configured and available");
            }
        } else {
            pasteKey = null;
        }
        // init db async then call back to complete enable.
        final BukkitTask updating = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            if (!isEnabled()) {
                PrismLogHandler.warn("Prism is loading and updating the database; logging is NOT enabled");

            }
        }, 100, 200);

        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            prismDataSource = PrismDatabaseFactory.createDataSource(configHandler.getDataSourceConfig());
            if (prismDataSource != null) {
                StringBuilder builder = new StringBuilder();
                if (!prismDataSource.reportDataSource(builder, true)) {
                    notifyDisabled();
                    Bukkit.getScheduler().runTask(instance, () -> instance.enableFailedDatabase());
                    updating.cancel();
                    return;
                }
            } else {
                notifyDisabled();
                Bukkit.getScheduler().runTask(instance, () -> instance.enableFailedDatabase());
                updating.cancel();
                return;
            }
            Settings.setDataSource(prismDataSource);
            // Info needed for setup, init these here
            handlerRegistry = new HandlerRegistry();
            actionRegistry = new ActionRegistryImpl();
            playerIdentifier = new PlayerIdentification(Prism.getInstance().getPrismDataSource());

            // Setup databases
            prismDataSource.setupDatabase(actionRegistry);
            // Apply any updates
            final DatabaseUpdater up = new DatabaseUpdater(prismDataSource);
            up.applyUpdates(prismDataSource);
            Bukkit.getScheduler().runTask(instance, () -> instance.enabled());
            updating.cancel();
        });
    }

    protected void notifyDisabled() {
        final String[] dbDisabled = new String[3];
        dbDisabled[0] = "Prism will disable most commands because it couldn't connect to a database.";
        dbDisabled[1] = "If you're using MySQL, check your config. Be sure MySQL is running.";
        dbDisabled[2] = "For help - try our Discord Channel or the Wiki on Github.";
        PrismLogHandler.logSection(dbDisabled);

    }

    /**
     * Enable if db failed.
     */
    public void enableFailedDatabase() {
        if (isEnabled()) {
            PluginCommand command = getCommand("prism");
            if (command != null) {
                commands = new PrismCommands(this, true);
                command.setExecutor(commands);
                command.setTabCompleter(commands);
                taskManager.active = false;
                taskManager.shutdown();
            } else {
                PrismLogHandler.warn("Command Executor Error: Check plugin.yml");
                Bukkit.getPluginManager().disablePlugin(instance);
            }
            saveConfig();
        }
    }

    /**
     * Enable post run. must be run sync
     */
    public void enabled() {
        if (isEnabled()) {
            // Cache world IDs
            final List<String> worldNames = getServer().getWorlds().stream()
                    .map(World::getName).collect(Collectors.toList());

            final String[] playerNames = Bukkit.getServer().getOnlinePlayers().stream()
                    .map(Player::getName).toArray(String[]::new);
            prismDataSource.cacheWorldPrimaryKeys(prismWorlds);
            prismDataSource.getPlayerIdHelper().cacheOnlinePlayerPrimaryKeys(playerNames);

            // ensure current worlds are added
            for (final String w : worldNames) {
                if (!Prism.prismWorlds.containsKey(w)) {
                    prismDataSource.addWorldName(w);
                }
            }
            eventTimer = new TimeTaken(this);
            queueStats = new QueueStats();
            ignore = new Ignore(config);
            taskManager.run();
            registerEvents();
            // Add commands
            registerCommands();
            // Register official parameters
            registerParameter(new ActionParameter());
            registerParameter(new BeforeParameter());
            registerParameter(new BlockParameter());
            registerParameter(new EntityParameter());
            registerParameter(new FlagParameter());
            registerParameter(new IdParameter());
            registerParameter(new KeywordParameter());
            registerParameter(new PlayerParameter());
            registerParameter(new RadiusParameter());
            registerParameter(new SinceParameter());
            registerParameter(new WorldParameter());

            // Init re-used classes
            oreMonitor = new OreMonitor(instance);
            useMonitor = new UseMonitor(instance);

            // Init async tasks
            taskManager.actionRecorderTask();

            // Init scheduled events
            endExpiredQueryCaches();
            endExpiredPreviews();
            removeExpiredLocations();

            // Delete old data based on config
            taskManager.launchScheduledPurgeManager();

            // Keep watch on db connections, other sanity
            taskManager.launchInternalAffairs();

            checkAndPreloadMaterials();
            Bukkit.getScheduler().runTaskAsynchronously(instance,
                  () -> Bukkit.getPluginManager().callEvent(EventHelper.createLoadEvent(Prism.getInstance())));
        }
        saveConfig();

    }

    private void checkAndPreloadMaterials(){
        if (config.preloadMaterials) {
            config.preloadMaterials = false;
            saveConfig();
            PrismLogHandler.log("Preloading materials - This will take a while!");
            items.initAllMaterials();
            PrismLogHandler.log("Preloading complete!");
        } else {
            items.initMaterials(Material.DIRT);
        }
    }

    private void registerEvents(){
        // Assign event listeners
        getServer().getPluginManager().registerEvents(new PrismBlockEvents(this), this);
        getServer().getPluginManager().registerEvents(new PrismEntityEvents(this), this);
        getServer().getPluginManager().registerEvents(new PrismWorldEvents(), this);
        getServer().getPluginManager().registerEvents(new PrismPlayerEvents(this), this);
        if (isPaper) {
            //register listeners that only work with paper.
            getServer().getPluginManager().registerEvents(new PaperListeners(this), this);
        }
        getServer().getPluginManager().registerEvents(new PrismInventoryEvents(this), this);
        getServer().getPluginManager().registerEvents(new PrismVehicleEvents(this), this);

        // InventoryMoveItem
        if (config.trackingConfig.hopperItemEvents && Prism.getIgnore().event(ActionType.ITEM_INSERT)) {
            getServer().getPluginManager().registerEvents(new PrismInventoryMoveItemEvent(), this);
        }

        if (config.trackingConfig.apiEnabled) {
            getServer().getPluginManager().registerEvents(new PrismCustomEvents(this), this);
        }

        getServer().getPluginManager().registerEvents(new PrismMiscEvents(), this);
    }

    private void registerCommands(){
        PluginCommand command = getCommand("prism");
        if (command != null) {
            commands = new PrismCommands(this, false);
            command.setExecutor(commands);
            command.setTabCompleter(commands);
        } else {
            PrismLogHandler.warn("Command Executor Error: Check plugin.yml");
            Bukkit.getPluginManager().disablePlugin(instance);
            return;
        }
        PluginCommand commandAlt = getCommand("what");
        if (commandAlt != null) {
            commandAlt.setExecutor(new WhatCommand(this));
        } else {
            PrismLogHandler.log("Command Executor Error: Check plugin.yml - what command not found ");
        }
    }

    /**
     * The version of Prism.
     *
     * @return String
     */
    public String getPrismVersion() {
        return this.pluginVersion;
    }

    /**
     * Load configuration and language files.
     */
    public void loadConfig() {
        PrismLogHandler.log("Loading Config...");
        configHandler = new ConfigHandler();
        Path path = Paths.get(getDataFolder().getPath(), "config.yml");
        configHandler.loadConfiguration(path);
        try {
            PrismDatabaseFactory.createDefaultConfig(configHandler.getDataSourceConfig());
        } catch (SerializationException e) {
            PrismLogHandler.warn("Error creating database config", e);
        }
        config = configHandler.getConfig();
        // Cache config arrays we check constantly
        illegalBlocks = EnumSet.copyOf(config.applierConfig.neverPlace);
        illegalEntities = EnumSet.copyOf(config.applierConfig.neverSpawn);
        alertedOres.clear();
        alertedOres.putAll(config.alertConfig.oreAlerts.oreBlocks);
        items = new MaterialAliases();
        setDebug(config.debug);
        PrismLogHandler.log("Config Loaded");
        PrismLogHandler.debug("Debug Mode Active");
        PrismLogHandler.debug(config.toString());

    }

    protected void checkPluginDependencies() {
        //DripReporter
        ApiHandler.configureMonitor();
        // WorldEdit
        ApiHandler.hookWorldEdit();
        //bstats
        if (config.allowMetrics != Boolean.FALSE) {
            int pluginId = 4365; // assigned by bstats.org
            try {
                Metrics metrics = new Metrics(this, pluginId);
                if (!metrics.isEnabled()) {
                    PrismLogHandler.warn("bStats failed to initialise! Please check Prism/bStats configs.");
                }
                Metrics.MultiLineChart blockBreaksHour =
                        new Metrics.MultiLineChart("//TODO", ActionMeter::getMetricMeter);
                metrics.addCustomChart(blockBreaksHour);
                PrismLogHandler.log("Prism bStats metrics are enabled - thank you!");
            } catch (ExceptionInInitializerError e) {
                PrismLogHandler.warn("bStats failed to initialise! Please check Prism/bStats configs: "
                        + e.getMessage());
            }
        }
    }

    /**
     * Check if a dependency so names is available.
     *
     * @return true
     */
    @Deprecated
    public boolean dependencyEnabled(String pluginName) {
        return ApiHandler.checkDependency(pluginName);
    }


    /**
     * Clears the Query Cache.
     */
    @SuppressWarnings("WeakerAccess")
    public void endExpiredQueryCaches() {
        getServer().getScheduler().runTaskTimer(this, () -> {
            final java.util.Date date = new java.util.Date();
            for (final Entry<String, QueryResult> query : cachedQueries.entrySet()) {
                final QueryResult result = query.getValue();
                final long diff = (date.getTime() - result.getQueryTime()) / 1000;
                if (diff >= 120) {
                    cachedQueries.remove(query.getKey());
                }
            }
        }, 2400L, 2400L);
    }

    /**
     * Clears expired Previews.
     */
    @SuppressWarnings("WeakerAccess")
    public void endExpiredPreviews() {
        getServer().getScheduler().runTaskTimer(this, () -> {
            final java.util.Date date = new java.util.Date();
            for (final Entry<String, PreviewSession> query : playerActivePreviews.entrySet()) {
                final PreviewSession result = query.getValue();
                final long diff = (date.getTime() - result.getQueryTime()) / 1000;
                if (diff >= 60) {
                    // inform player

                    final Player player = result.getPlayer();
                    if (player.isOnline()) {
                        Prism.messenger.sendMessage(player,
                                Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("cancel-preview-forgotten")));
                    }
                    playerActivePreviews.remove(query.getKey());
                }
            }
        }, 1200L, 1200L);
    }

    /**
     * Remove expired locations.
     */
    private void removeExpiredLocations() {
        getServer().getScheduler().runTaskTimer(this, () -> {
            final java.util.Date date = new java.util.Date();
            // Remove locations logged over five minute ago.
            for (final Entry<Location, Long> entry : alertedBlocks.entrySet()) {
                final long diff = (date.getTime() - entry.getValue()) / 1000;
                if (diff >= 300) {
                    alertedBlocks.remove(entry.getKey());
                }
            }
        }, 1200L, 1200L);
    }

    /**
     * Send an alert to players.
     *
     * @param player    Player which caused the alert
     * @param msg       Alert message
     * @param alertPerm Players with this permission (or prism.alerts) will receive the alert
     */
    public void alertPlayers(Player player, Component msg, String alertPerm) {
        for (final Player p : getServer().getOnlinePlayers()) {
            if ((!p.equals(player) || config.alertConfig.alertPlayerAboutSelf)
                    && (p.hasPermission("prism.alerts") || (alertPerm != null && p.hasPermission(alertPerm)))) {
                TextComponent message = Il8nHelper.getMessage("alert-prefix").color(NamedTextColor.RED).append(msg);
                Prism.messenger.sendMessage(p,message);
            }
        }
    }

    /**
     * Report nearby players of a set radius.
     *
     * @param player Player
     * @param radius int
     * @param msg    String
     */
    public void notifyNearby(Player player, int radius, Component msg) {
        if (config.applierConfig.notifyNearby) {
            int distance = (radius + config.applierConfig.additionalNotifyRadius) ^ 2;
            for (final Player p : player.getServer().getOnlinePlayers()) {
                if (!p.getUniqueId().equals(player.getUniqueId())
                        && player.getWorld().equals(p.getWorld())
                        && player.getLocation().distanceSquared(p.getLocation()) <= distance) {
                    Prism.messenger.sendMessage(p, messenger.playerHeaderMsg(msg));
                }
            }
        }
    }

    /**
     * Shutdown.
     */
    @Override
    public void onDisable() {
        taskManager.shutdown();
        Bukkit.getPluginManager().callEvent(EventHelper.createUnLoadEvent());
        if (config.queueConfig.forceWriteOnClose) {
            final QueueDrain drainer = new QueueDrain(this);
            drainer.forceDrainQueue();
        }
        if (!ApiHandler.disable()) {
            PrismLogHandler.log("Possible errors unhooking dependencies...");
        }

        Bukkit.getScheduler().cancelTasks(this);
        // Close prismDataSource connections when plugin disables
        if (prismDataSource != null) {
            prismDataSource.dispose();
        }
        PrismLogHandler.log("Closing plugin.");
        logHandler.close();
        super.onDisable();
    }

    @Override
    public PrismParameters createParameters() {
        return new QueryParameters();
    }

    @Override
    public Future<Result> performLookup(PrismParameters parameters, CommandSender sender) {
        CompletableFuture<Result> resultCompletableFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            Result result = new ActionsQuery(Prism.getInstance()).lookup(parameters, sender);
            resultCompletableFuture.complete(result);
        });
        return resultCompletableFuture;
    }

    public PrismCommands getCommands() {
        return commands;
    }
}
