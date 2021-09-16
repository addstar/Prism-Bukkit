package me.botsko.prism.monitors;

import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.utils.MiscUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class UseMonitor {
    protected final List<Material> blocksToAlertOnPlace;
    protected final List<Material> blocksToAlertOnBreak;
    private final Prism plugin;
    private ConcurrentHashMap<String, Integer> countedEvents = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public UseMonitor(Prism plugin) {
        this.plugin = plugin;
        blocksToAlertOnPlace = plugin.config.alertConfig.uses.monitorItems;
        blocksToAlertOnBreak = plugin.config.alertConfig.uses.breakItems;
        resetEventsQueue();
    }

    /**
     * Maintain use alert message history.
     * Increments the number of times the player has caused a particular alert.
     * If the count is below the threshold, the alert will be sent.
     *
     * @param playername Player causing the alert
     * @param msg        Alert message
     * @param alertPerm  Players with this permission will receive the alert
     */
    protected void incrementCount(String playername, String msg, String alertPerm) {

        int count = 0;
        String key = playername + msg;
        if (countedEvents.containsKey(key)) {
            count = countedEvents.get(key);
        }
        count++;
        countedEvents.put(key, count);
        TextComponent out = Component.text(playername + " " + msg)
                .color(NamedTextColor.GRAY);
        if (count == 5) {
            out = out.append(Component.text(" - pausing warnings.")
                    .color(NamedTextColor.GRAY));
        }
        if (count <= 5) {
            plugin.alertPlayers(null, out,alertPerm);
            if (plugin.config.alertConfig.uses.logToConsole) {
                PrismLogHandler.log(PlainTextComponentSerializer.plainText().serialize(out));
            }
            // Log to commands
            List<String> commands = plugin.config.alertConfig.uses.logCommands;
            MiscUtils.dispatchAlert(msg, commands);
        }
    }

    /**
     * Check if use alert should be canceled.
     *
     * @param player Player causing the alert
     * @return       true if alert should be canceled
     */
    private boolean checkFeatureShouldCancel(Player player) {

        // Ensure enabled
        if (!plugin.config.alertConfig.uses.enabled) {
            return true;
        }

        // Ignore players who would see the alerts
        if (plugin.config.alertConfig.uses.ignoreStaff
                && player.hasPermission("prism.alerts")) {
            return true;
        }

        // Ignore certain ranks
        return player.hasPermission("prism.bypass-use-alerts");
    }

    /**
     * Alert on block place.
     *
     * @param player    Player
     * @param block     Block
     * @param alertPerm Players with this permission will receive the alert
     */
    public void alertOnBlockPlacement(Player player, Block block, String alertPerm) {

        // Ensure enabled
        if (checkFeatureShouldCancel(player)) {
            return;
        }

        final String playername = player.getName();
        // Ensure we're tracking this block
        if (blocksToAlertOnPlace.contains(block.getType())) {
            final String alias = Prism.getItems().getAlias(block.getType(), block.getBlockData());
            incrementCount(playername, "placed " + alias, alertPerm);
        }
    }

    /**
     * Alert on break.
     *
     * @param player    Player
     * @param block     Block
     * @param alertPerm Players with this permission will receive the alert
     */
    public void alertOnBlockBreak(Player player, Block block, String alertPerm) {

        // Ensure enabled
        if (checkFeatureShouldCancel(player)) {
            return;
        }

        final String playername = player.getName();
        final String blockType = "" + block.getType();

        // Ensure we're tracking this block
        if (blocksToAlertOnBreak.contains(block.getType())) {
            final String alias = Prism.getItems().getAlias(block.getType(), block.getBlockData());
            incrementCount(playername, "broke " + alias, alertPerm);
        }
    }

    /**
     * Alert on use.
     *
     * @param player    Player
     * @param useMsg    Message
     * @param alertPerm Players with this permission will receive the alert
     */
    public void alertOnItemUse(Player player, String useMsg, String alertPerm) {

        // Ensure enabled
        if (checkFeatureShouldCancel(player)) {
            return;
        }

        final String playerName = player.getName();
        incrementCount(playerName, useMsg, alertPerm);

    }

    /**
     * Alert on xray.
     *
     * @param player    Player
     * @param useMsg    Message
     * @param alertPerm Players with this permission will receive the alert
     */
    public void alertOnVanillaXray(Player player, String useMsg, String alertPerm) {

        if (checkFeatureShouldCancel(player)) {
            return;
        }

        final String playerName = player.getName();
        incrementCount(playerName, useMsg, alertPerm);

    }

    /**
     * Periodically clear the use alert history.
     */
    private void resetEventsQueue() {
        plugin.getServer().getScheduler()
                .scheduleSyncRepeatingTask(plugin, () -> countedEvents = new ConcurrentHashMap<>(),
                        7000L, 7000L);
    }
}
