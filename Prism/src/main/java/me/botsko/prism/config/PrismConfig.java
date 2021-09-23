package me.botsko.prism.config;

import me.botsko.prism.api.actions.ActionType;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created for Prism.
 *
 * @author Narimm on 22/02/2021
 * @since 2.1.8
 */
@ConfigSerializable
public class PrismConfig {

    @Setting("debug")
    public boolean debug = false;

    @Setting("preload-materials")
    public boolean preloadMaterials = true;

    @Setting("paste")
    public PasteConfig pasteConfig = new PasteConfig();

    @Setting("allow-metrics")
    public Boolean allowMetrics = null;

    @Setting("wand")
    public WandConfig wandConfig = new WandConfig();

    @Setting("queries")
    public ParameterConfig parameterConfig = new ParameterConfig();

    @Setting("near")
    public CommandDefault nearCommandConfig = new CommandDefault(5,100,100);

    @Setting("drain")
    public CommandDefault drainCommandConfig = new CommandDefault();

    @Setting("ex")
    public CommandDefault extinguishCommandConfig = new CommandDefault();

    @Setting("ignore")
    public IgnoreConfig ignoreConfig = new IgnoreConfig();

    @Setting("purge")
    public PurgeConfig purgeConfig = new PurgeConfig();

    @Setting("applier")
    public ApplierConfig applierConfig = new ApplierConfig();

    @Setting("tracking")
    public TrackingConfig trackingConfig = new TrackingConfig();

    @Setting("do-not-track-commands")
    public List<String> doNotTrackCommands
            = Arrays.asList("vanish","v","login","changepassword","register","unregister");

    @Setting("alerts")
    public AlertConfig alertConfig =  new AlertConfig();

    @Setting("queue")
    public QueueConfig queueConfig = new QueueConfig();

    @ConfigSerializable
    public static class PasteConfig {

        @Setting("enable")
        public boolean enabled = false;

        @Setting("api-key")
        public String apiKey = "API key from http://paste.gg";

    }

    @ConfigSerializable
    public static class WandConfig {

        @Setting("default-mode")
        public WandMode defaultMode = WandMode.HAND;

        @Setting("default-mode-item")
        public Material defaultModeItem = Material.STICK;

        @Setting("default-mode-block")
        public Material defaultModeBlock = Material.SPRUCE_LOG;

        @Setting("auto-equip")
        public boolean autoEquip = true;

        @Setting("allow-user-override")
        public boolean allowUserOverride = true;

        @Setting("inspect-ignore-actions")
        public List<ActionType> wandInspectIgnoreActions = Arrays.asList(ActionType.PLAYER_CHAT,
                ActionType.PLAYER_COMMAND,ActionType.PLAYER_JOIN,ActionType.PLAYER_QUIT);

    }

    @ConfigSerializable
    public static class ParameterConfig {

        @Setting("default-radius")
        public int defaultRadius = 5;

        @Setting("default-time-since")
        public String defaultTimeSince = "2d";

        @Setting("max-lookup-radius")
        public int maxLookupRadius = 100;

        @Setting("max-applier-radius")
        public int maxApplierRadius = 100;

        @Setting("never-use-defaults")
        public boolean neverUseDefaults = false;

        @Setting("lookup-max-results")
        public int lookupMaxResults = 1000;

        @Setting("default-results-per-page")
        public int defaultResultsPerPage = 5;

        @Setting("lookup-auto-group")
        public boolean lookupAutoGroup = true;

        @Setting("always-show-extended")
        public boolean  alwaysShowExtended = false;

    }

    @ConfigSerializable
    public static class CommandDefault {

        @Setting("default-radius")
        public int defaultRadius = 5;

        @Setting("max-results")
        public int maxResults = 100;

        @Setting("max-radius")
        public int maxRadius = 10;

        protected CommandDefault(int defaultRadius, int maxResults, int maxRadius) {
            this.defaultRadius = defaultRadius;
            this.maxResults = maxResults;
            this.maxRadius = maxRadius;
        }

        public CommandDefault() {
        }
    }

    @ConfigSerializable
    public static class IgnoreConfig {

        @Setting("enable-perm-nodes")
        public boolean enablePermNodes = false;

        @Setting("players-in-creative")
        public boolean creativePlayers = false;

        @Setting("players")
        public List<UUID> players = new ArrayList<>();

        @Setting("players_whitelist")
        public boolean isPlayerWhiteList = false;

        @Setting("worlds")
        public List<UUID> worlds = new ArrayList<>();

        @Setting("worlds_whitelist")
        public boolean isWorldWhiteList = false;

    }

    @ConfigSerializable
    public static class PurgeConfig {

        @Setting("rules")
        public List<String> rules = Arrays.asList("before:8w", "a:water-flow before:4w");

        @Setting("batch-tick-delay")
        public int batchTickDelay = 30;

        @Setting("records-per-batch")
        public int recordsPerBatch = 10000;

    }

    @ConfigSerializable
    public static class ApplierConfig {


        @Setting("notify-nearby-enabled")
        public boolean notifyNearby = true;

        @Setting("notify-nearby-additional-radius")
        public int additionalNotifyRadius = 20;

        @Setting("remove-fire-on-burn-rollback")
        public boolean removeFireOnBurnRollback = true;

        @Setting("remove-drops-on-explode-rollback")
        public boolean removeDropsExplodeRollback = true;

        @Setting("never-spawn-entity")
        public Set<EntityType> neverSpawn = EnumSet.of(EntityType.CREEPER);

        @Setting("never-place-block")
        public Set<Material> neverPlace = EnumSet.of(Material.LAVA,Material.WATER);

        @Setting("allow-rollback-items-removed-from-container")
        public boolean allowRollbackItemsRemovedFromContainer = true;
        
    }

    @ConfigSerializable
    public static class TrackingConfig {

        public Map<ActionType,Boolean> trackers = new HashMap<>();

        @Setting("player-ip-on-join")
        public boolean playerIpOnJoin = false;

        @Setting("hopper-item-events")
        public boolean hopperItemEvents = false;

        @Setting("api-enabled")
        public boolean apiEnabled = true;

        @Setting("api-allowed-plugins")
        public List<String> allowedPlugins = new ArrayList<>();

        protected TrackingConfig() {
            Set<ActionType> actions = ActionType.getTypes();
            for (ActionType a:actions) {
                switch (a) {
                    case CRAFT_ITEM, ENCHANT_ITEM, ENTITY_DYE, FIRE_SPREAD, ITEM_BREAK, PLAYER_CHAT, PLAYER_COMMAND,
                            PLAYER_GAMEMODECHANGE, PLAYER_JOIN, PLAYER_QUIT, PLAYER_TRADE, PLAYER_TELEPORT, TARGET_HIT,
                            WATER_FLOW, WORLD_EDIT, XP_PICKUP -> trackers.put(a, false);
                    default -> trackers.put(a, true);
                }
            }
        }
    }


    @ConfigSerializable
    public static class AlertConfig {

        @Setting("alert-staff-to-applied-process")
        public boolean alertStaffToAppliedProcess = true;

        @Setting("alert-player-about-self")
        public boolean alertPlayerAboutSelf = true;

        @Setting("ores")
        public OreAlerts oreAlerts = new OreAlerts();

        @Setting("illegal-commands")
        public IllegalCommands illegalCommands = new IllegalCommands();

        @Setting("uses")
        public UsesConfig uses = new UsesConfig();

        @Setting("vanilla-xray-enabled")
        public boolean vanillaXray = true;


        public abstract static class AlertBase {

            @Setting("enabled")
            public boolean enabled = true;

            @Setting("log-to-console")
            public boolean logToConsole = true;

            @Setting("log-commands")
            public List<String> logCommands = Collections.singletonList("examplecommand <alert>");

        }

        @ConfigSerializable
        public static class OreAlerts extends AlertBase {

            @Setting("blocks")
            public Map<Material, TextColor> oreBlocks = new LinkedHashMap<>();

            /**
             * Constructor.
             */
            public OreAlerts() {
                oreBlocks.put(Material.IRON_ORE, TextColor.fromCSSHexString("#444444"));
                oreBlocks.put(Material.GOLD_ORE, TextColor.fromCSSHexString("#ffe17d"));
                oreBlocks.put(Material.LAPIS_ORE, TextColor.fromCSSHexString("#0670cc"));
                oreBlocks.put(Material.DIAMOND_ORE, TextColor.fromCSSHexString("#04babd"));
                oreBlocks.put(Material.EMERALD_ORE, TextColor.fromCSSHexString("#21bf60"));
                oreBlocks.put(Material.NETHER_GOLD_ORE, TextColor.fromCSSHexString("#ff7308"));
                oreBlocks.put(Material.ANCIENT_DEBRIS, TextColor.fromCSSHexString("#856d3e"));
            }

        }

        @ConfigSerializable
        public static class IllegalCommands extends AlertBase {

            @Setting("commands")
            public List<String> illegalCommands = new ArrayList<>();

            /**
             * Constructor.
             */
            public IllegalCommands() {
                enabled = false;
                illegalCommands.add("op");
                illegalCommands.add("deop");
                illegalCommands.add("stop");
                illegalCommands.add("reload");
                illegalCommands.add("bukkit:op");
                illegalCommands.add("bukkit:deop");
                illegalCommands.add("bukkit:stop");
                illegalCommands.add("bukkit:reload");
                illegalCommands.add("minecraft:op");
                illegalCommands.add("minecraft:deop");
                illegalCommands.add("minecraft:stop");
                illegalCommands.add("minecraft:reload");
            }
        }

        @ConfigSerializable
        public static class UsesConfig extends AlertBase {

            public boolean lighter = true;

            public boolean lava = true;

            @Setting("item-placement")
            public List<Material> monitorItems = new ArrayList<>();

            @Setting("item-break")
            public List<Material> breakItems = new ArrayList<>();

            @Setting("ignore-staff")
            public boolean ignoreStaff;

            /**
             * Constructor.
             */
            public UsesConfig() {
                monitorItems.add(Material.BEDROCK);
                monitorItems.add(Material.STICKY_PISTON);
                monitorItems.add(Material.TNT);
                monitorItems.add(Material.LAVA);
                breakItems.addAll(monitorItems);
            }
        }
    }

    @ConfigSerializable
    public static class QueueConfig {

        @Setting("empty-tick-delay")
        public int emptyTickDelay = 3;

        @Setting("max-failures-before-wait")
        public int maxFailures = 5;

        @Setting("actions-per-insert-batch")
        public int actionsPerBatch = 1000;

        @Setting("force-write-queue-on-shutdown")
        public boolean forceWriteOnClose = true;

    }

    public enum WandMode {
        HAND,
        ITEM,
        BLOCK
    }
}
