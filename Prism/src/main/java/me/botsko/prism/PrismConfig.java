package me.botsko.prism;

import me.botsko.prism.database.PrismDatabaseFactory;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PrismConfig extends ConfigBase {

    /**
     * Constructor.
     *
     * @param plugin Plugin.
     */
    @SuppressWarnings("WeakerAccess")
    public PrismConfig(Plugin plugin) {
        super(plugin);
    }

    /**
     * Get the fileConfig.
     */
    @Override
    public FileConfiguration getConfig() {
        config = super.getConfig();
        // set defaults
        config.addDefault("prism.debug", false);
        config.addDefault("prism.preload-materials", false);
        // config.addDefault("prism.language", "en-us");

        config.addDefault("prism.allow-metrics", true);

        // Database
        PrismDatabaseFactory.createDefaultConfig(config);

        // paste.gg sharing.
        config.addDefault("prism.paste.enable", true);
        config.addDefault("prism.paste.api-key", "API key from http://paste.gg");
        // Wands
        config.addDefault("prism.wands.default-mode", "hand"); // hand, item,
        // or block
        config.addDefault("prism.wands.default-item-mode-id", "stick");
        config.addDefault("prism.wands.default-block-mode-id", "spruce_log");
        config.addDefault("prism.wands.auto-equip", true);
        config.addDefault("prism.wands.allow-user-override", true);
        final Collection<String> ignoreActionsForInspect = new ArrayList<>();
        ignoreActionsForInspect.add("player-chat");
        ignoreActionsForInspect.add("player-command");
        ignoreActionsForInspect.add("player-join");
        ignoreActionsForInspect.add("player-quit");
        config.addDefault("prism.wands.inspect.ignore-actions", ignoreActionsForInspect);

        // Queries
        config.addDefault("prism.queries.default-radius", 5);
        config.addDefault("prism.queries.default-time-since", "3d");
        config.addDefault("prism.queries.max-lookup-radius", 100);
        config.addDefault("prism.queries.max-applier-radius", 100);
        config.addDefault("prism.queries.never-use-defaults", false);
        config.addDefault("prism.queries.lookup-max-results", 1000);
        config.addDefault("prism.queries.default-results-per-page", 5);
        config.addDefault("prism.queries.lookup-auto-group", true);

        // Messenger
        config.addDefault("prism.messenger.always-show-extended", false);

        // Near
        config.addDefault("prism.near.default-radius", 5);
        config.addDefault("prism.near.max-results", 100);

        // Drain
        config.addDefault("prism.drain.max-radius", 10);
        config.addDefault("prism.drain.default-radius", 5);

        // Ex
        config.addDefault("prism.ex.max-radius", 100);
        config.addDefault("prism.ex.default-radius", 10);

        // Ignore
        config.addDefault("prism.ignore.enable-perm-nodes", false);
        config.addDefault("prism.ignore.players-in-creative", false);
        config.addDefault("prism.ignore.players", new ArrayList<String>());
        config.addDefault("prism.ignore.players_whitelist", false);
        config.addDefault("prism.ignore.worlds", new ArrayList<String>());
        config.addDefault("prism.ignore.worlds_whitelist", false);

        // Purge
        final Collection<String> purgeRules = new ArrayList<>();
        purgeRules.add("before:8w");
        purgeRules.add("a:water-flow before:4w");
        config.addDefault("prism.db-records-purge-rules", purgeRules);
        config.addDefault("prism.purge.batch-tick-delay", 30);
        config.addDefault("prism.purge.records-per-batch", 100000);

        // Appliers
        config.addDefault("prism.appliers.allow-rollback-items-removed-from-container", true);
        config.addDefault("prism.appliers.notify-nearby.enabled", true);
        config.addDefault("prism.appliers.notify-nearby.additional-radius", 20);
        config.addDefault("prism.appliers.remove-fire-on-burn-rollback", true);
        config.addDefault("prism.appliers.remove-drops-on-explode-rollback", true);

        // Illegal Entity Rollbacks
        final Collection<String> illegalEntities = new ArrayList<>();
        illegalEntities.add("creeper");
        config.addDefault("prism.appliers.never-spawn-entity", illegalEntities);

        // Illegal Block Rollbacks
        final Collection<String> illegalBlocks = new ArrayList<>();
        illegalBlocks.add("water");
        illegalBlocks.add("lava");
        illegalBlocks.add("flowing_lava");

        config.addDefault("prism.appliers.never-place-block", illegalBlocks);

        // Tracking
        config.addDefault("prism.tracking.block-break", true);
        config.addDefault("prism.tracking.block-burn", true);
        config.addDefault("prism.tracking.block-dispense", true);
        config.addDefault("prism.tracking.block-fade", true);
        config.addDefault("prism.tracking.block-fall", true);
        config.addDefault("prism.tracking.block-form", true);
        config.addDefault("prism.tracking.block-place", true);
        config.addDefault("prism.tracking.block-shift", true);
        config.addDefault("prism.tracking.block-spread", true);
        config.addDefault("prism.tracking.block-use", true);
        config.addDefault("prism.tracking.bucket-fill", true);
        config.addDefault("prism.tracking.bonemeal-use", true);
        config.addDefault("prism.tracking.container-access", true);
        config.addDefault("prism.tracking.cake-eat", true);
        config.addDefault("prism.tracking.craft-item", false);
        config.addDefault("prism.tracking.creeper-explode", true);
        config.addDefault("prism.tracking.crop-trample", true);
        config.addDefault("prism.tracking.dragon-eat", true);
        config.addDefault("prism.tracking.enchant-item", false);
        config.addDefault("prism.tracking.enderman-pickup", true);
        config.addDefault("prism.tracking.enderman-place", true);
        config.addDefault("prism.tracking.entity-break", true);
        config.addDefault("prism.tracking.entity-dye", false);
        config.addDefault("prism.tracking.entity-explode", true);
        config.addDefault("prism.tracking.entity-follow", true);
        config.addDefault("prism.tracking.entity-form", true);
        config.addDefault("prism.tracking.entity-kill", true);
        config.addDefault("prism.tracking.entity-leash", true);
        config.addDefault("prism.tracking.entity-shear", true);
        config.addDefault("prism.tracking.entity-spawn", true);
        config.addDefault("prism.tracking.entity-unleash", true);
        config.addDefault("prism.tracking.fireball", true);
        config.addDefault("prism.tracking.fire-spread", false);
        config.addDefault("prism.tracking.firework-launch", true);
        config.addDefault("prism.tracking.hangingitem-break", true);
        config.addDefault("prism.tracking.hangingitem-place", true);
        config.addDefault("prism.tracking.item-drop", true);
        config.addDefault("prism.tracking.item-insert", true);
        config.addDefault("prism.tracking.item-pickup", true);
        config.addDefault("prism.tracking.item-remove", true);
        config.addDefault("prism.tracking.item-break", false);
        config.addDefault("prism.tracking.item-rotate", true);
        config.addDefault("prism.tracking.lava-break", true);
        config.addDefault("prism.tracking.lava-bucket", true);
        config.addDefault("prism.tracking.lava-flow", false);
        config.addDefault("prism.tracking.lava-ignite", true);
        config.addDefault("prism.tracking.leaf-decay", true);
        config.addDefault("prism.tracking.lighter", true);
        config.addDefault("prism.tracking.lightning", true);
        config.addDefault("prism.tracking.mushroom-grow", true);
        config.addDefault("prism.tracking.player-chat", false);
        config.addDefault("prism.tracking.player-command", false);
        config.addDefault("prism.tracking.player-death", true);
        config.addDefault("prism.tracking.player-gamemodechange", false);
        config.addDefault("prism.tracking.player-join", false);
        config.addDefault("prism.tracking.player-kill", true);
        config.addDefault("prism.tracking.player-quit", false);
        config.addDefault("prism.tracking.player-teleport", false);
        config.addDefault("prism.tracking.player-trade", false);
        config.addDefault("prism.tracking.portal-create", true);
        config.addDefault("prism.tracking.potion-splash", true);
        config.addDefault("prism.tracking.sheep-eat", true);
        config.addDefault("prism.tracking.sign-change", true);
        config.addDefault("prism.tracking.spawnegg-use", true);
        config.addDefault("prism.tracking.target-hit", false);
        config.addDefault("prism.tracking.tnt-explode", true);
        config.addDefault("prism.tracking.bed-explode", true);
        config.addDefault("prism.tracking.tnt-prime", true);
        config.addDefault("prism.tracking.tree-grow", true);
        config.addDefault("prism.tracking.vehicle-break", true);
        config.addDefault("prism.tracking.vehicle-enter", true);
        config.addDefault("prism.tracking.vehicle-exit", true);
        config.addDefault("prism.tracking.vehicle-place", true);
        config.addDefault("prism.tracking.water-break", true);
        config.addDefault("prism.tracking.water-bucket", true);
        config.addDefault("prism.tracking.water-flow", false);
        config.addDefault("prism.tracking.world-edit", false);
        config.addDefault("prism.tracking.xp-pickup", false);

        // Tracker configs
        config.addDefault("prism.track-player-ip-on-join", false);
        config.addDefault("prism.track-hopper-item-events", false);

        final Collection<String> doNotTrackCommand = new ArrayList<>();
        doNotTrackCommand.add("vanish");
        doNotTrackCommand.add("v");
        doNotTrackCommand.add("login");
        doNotTrackCommand.add("changepassword");
        doNotTrackCommand.add("register");
        doNotTrackCommand.add("unregister");
        config.addDefault("prism.do-not-track.commands", doNotTrackCommand);

        config.addDefault("prism.tracking.api.enabled", true);
        final Collection<String> allowedApiPlugins = new ArrayList<>();
        allowedApiPlugins.add("DarkMythos");
        allowedApiPlugins.add("PrismApiDemo");
        config.addDefault("prism.tracking.api.allowed-plugins", allowedApiPlugins);

        // Ore Alerts
        config.addDefault("prism.alerts.alert-staff-to-applied-process", true);
        config.addDefault("prism.alerts.alert-player-about-self", true);
        config.addDefault("prism.alerts.ores.enabled", true);
        config.addDefault("prism.alerts.ores.log-to-console", true);
        config.addDefault("prism.alerts.ores.log-commands", Collections.singletonList("examplecommand <alert>"));
        // Ore blocks
        final Map<String, String> oreBlocks = new LinkedHashMap<>();
        oreBlocks.put(Material.IRON_ORE.name().toLowerCase(), "#808080");
        oreBlocks.put(Material.GOLD_ORE.name().toLowerCase(), "#ffe17d");
        oreBlocks.put(Material.LAPIS_ORE.name().toLowerCase(), "#0670cc");
        oreBlocks.put(Material.DIAMOND_ORE.name().toLowerCase(), "#04babd");
        oreBlocks.put(Material.EMERALD_ORE.name().toLowerCase(), "#21bf60");
        oreBlocks.put(Material.NETHER_GOLD_ORE.name().toLowerCase(), "#ff7308");
        oreBlocks.put(Material.ANCIENT_DEBRIS.name().toLowerCase(), "#856d3e");
        config.addDefault("prism.alerts.ores.blocks", oreBlocks);

        // Illegal Command Alerts
        config.addDefault("prism.alerts.illegal-commands.enabled", false);
        config.addDefault("prism.alerts.illegal-commands.log-to-console", true);
        config.addDefault("prism.alerts.illegal-commands.log-commands",
                Collections.singletonList("examplecommand <alert>"));
        final Collection<String> illegalCommands = new ArrayList<>();
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
        config.addDefault("prism.alerts.illegal-commands.commands", illegalCommands);

        // Use Alerts
        config.addDefault("prism.alerts.uses.enabled", true);
        config.addDefault("prism.alerts.uses.ignore-staff", false);
        config.addDefault("prism.alerts.uses.log-to-console", true);
        config.addDefault("prism.alerts.uses.log-commands", Collections.singletonList("examplecommand <alert>"));
        config.addDefault("prism.alerts.uses.lighter", true);
        config.addDefault("prism.alerts.uses.lava", true);

        Collection<String> monitorItems = new ArrayList<>();
        monitorItems.add("bedrock");
        monitorItems.add("sticky_piston");
        monitorItems.add("tnt");
        monitorItems.add("lava");
        config.addDefault("prism.alerts.uses.item-placement", monitorItems);

        monitorItems = new ArrayList<>();
        config.addDefault("prism.alerts.uses.item-break", monitorItems);

        // Misc Alerts
        config.addDefault("prism.alerts.vanilla-xray.enabled", true);

        // Internal
        config.addDefault("prism.queue-empty-tick-delay", 3);

        // Copy defaults
        config.options().copyDefaults(true);
        // save the defaults/config
        super.write("config",config);

        return config;

    }
}
