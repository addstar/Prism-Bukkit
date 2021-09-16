package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.config.PrismConfig;
import me.botsko.prism.settings.Settings;
import me.botsko.prism.utils.InventoryUtils;
import me.botsko.prism.utils.ItemUtils;
import me.botsko.prism.wands.InspectorWand;
import me.botsko.prism.wands.ProfileWand;
import me.botsko.prism.wands.QueryWandBase;
import me.botsko.prism.wands.RestoreWand;
import me.botsko.prism.wands.RollbackWand;
import me.botsko.prism.wands.Wand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.PropertyKey;

import java.util.List;
import java.util.Objects;

public class WandCommand extends AbstractCommand {

    private final Prism plugin;

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public WandCommand(Prism plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(CallInfo call) {
        String type = "i";
        final boolean isInspect = call.getArg(0).equalsIgnoreCase("inspect") || call.getArg(0).equalsIgnoreCase("i");
        if (!isInspect) {
            if (call.getArgs().length < 2) {
                Prism.messenger.sendMessage(call.getPlayer(),
                        Prism.messenger.playerError(Il8nHelper.getMessage("wand-error-type")));
                return;
            }
            type = call.getArg(1);
        }

        Wand oldWand = null;
        if (Prism.playersWithActiveTools.containsKey(call.getPlayer().getName())) {
            // Pull the wand in use
            oldWand = Prism.playersWithActiveTools.get(call.getPlayer().getName());
        }

        // Always remove the old one
        Prism.playersWithActiveTools.remove(call.getPlayer().getName());

        // Determine default mode
        PrismConfig.WandMode mode = plugin.config.wandConfig.defaultMode;

        // Check if the player has a personal override
        boolean allowUserOveride = plugin.config.wandConfig.allowUserOverride;
        if (allowUserOveride) {
            final PrismConfig.WandMode personalMode = PrismConfig.WandMode.valueOf(
                    Settings.getSetting("wand.mode", call.getPlayer()));
            if (personalMode != null) {
                mode = personalMode;
            }
        }

        // Determine which item we're using.
        Material toolKey = null;
        if (PrismConfig.WandMode.ITEM.equals(mode)) {
            toolKey = plugin.config.wandConfig.defaultModeItem;
        } else if (PrismConfig.WandMode.BLOCK.equals(mode)) {
            toolKey = plugin.config.wandConfig.defaultModeBlock;
        }

        // Check if the player has a personal override
        if (allowUserOveride) {
            final Material personalToolKey = Material.valueOf(Settings.getSetting("wand.item", call.getPlayer()));
            if (personalToolKey != null) {
                toolKey = personalToolKey;
            }
        }

        Material itemMaterial = toolKey;
        String wandOn = "";
        String itemName = "";
        StringBuilder parameters = new StringBuilder();
        if (itemMaterial != null) {
            itemName = Prism.getItems().getAlias(itemMaterial, null);
            wandOn += " on a " + itemName;
        }

        for (int i = (isInspect ? 1 : 2); i < call.getArgs().length; i++) {
            parameters.append(" ").append(call.getArg(i));
        }

        if (ItemUtils.isBadWand(itemMaterial)) {
            final String itemNameFinal = itemName;
            Prism.messenger.sendMessage(call.getPlayer(),
                    Prism.messenger.playerError(Il8nHelper.getMessage("wand-bad")
                            .replaceText(builder -> builder.match("<itemName>").replacement(
                                    Component.text(itemNameFinal))))
            );
            return;
        }

        boolean enabled = false;
        Wand wand = null;
        /*
          Inspector wand
         */
        switch (type.toLowerCase()) {
            case "i", "inpect" -> {
                if (checkNoPermissions(call.getPlayer(), "prism.lookup", "prism.wand.inspect")) {
                    return;
                }
                if (oldWand instanceof InspectorWand) {
                    sendWandStatus(call.getPlayer(), "wand-inspection", false, wandOn, parameters.toString());
                } else {
                    wand = new InspectorWand(plugin);
                    sendWandStatus(call.getPlayer(), "wand-inspection", true, wandOn, parameters.toString());
                    enabled = true;
                }
            }
            case "p", "profile" -> {
                if (checkNoPermissions(call.getPlayer(), "prism.lookup", "prism.wand.profile")) {
                    return;
                }
                if (oldWand instanceof ProfileWand) {
                    sendWandStatus(call.getPlayer(), "wand-profile", false, wandOn, parameters.toString());
                } else {
                    wand = new ProfileWand();
                    enabled = true;
                    sendWandStatus(call.getPlayer(), "wand-profile", true, wandOn, parameters.toString());
                }
            }
            case "rollback", "rb" -> {
                if (checkNoPermissions(call.getSender(), "prism.rollback", "prism.wand.rollback")) {
                    return;
                }
                if (oldWand instanceof RollbackWand) {
                    sendWandStatus(call.getPlayer(), "wand-rollback", false, wandOn, parameters.toString());
                } else {
                    wand = new RollbackWand(plugin);
                    sendWandStatus(call.getPlayer(), "wand-rollback", true, wandOn, parameters.toString());
                    enabled = true;
                }
            }
            case "restore", "rs" -> {
                if (checkNoPermissions(call.getPlayer(), "prism.restore", "prism.wand.restore")) {
                    return;
                }
                if (oldWand instanceof RestoreWand) {
                    sendWandStatus(call.getPlayer(), "wand-restore", false, wandOn, parameters.toString());

                } else {
                    wand = new RestoreWand(plugin);
                    enabled = true;
                    sendWandStatus(call.getPlayer(), "wand-restore", true, wandOn, parameters.toString());
                }
            }
            case "off" -> sendWandStatus(call.getPlayer(), "wand-current", false, wandOn, parameters.toString());
            default -> {
                Prism.messenger.sendMessage(call.getPlayer(),
                        Prism.messenger.playerError(Il8nHelper.getMessage("wand-invalid")));
                return;
            }
        }
        constructWand(call, enabled, itemMaterial, mode, wand, isInspect, oldWand);
    }

    private void constructWand(CallInfo call, boolean enabled,
                               final Material itemMaterial, PrismConfig.WandMode mode, Wand wand,
                               boolean isInspect,
                               Wand oldWand) {
        Material item = itemMaterial;
        final PlayerInventory inv = call.getPlayer().getInventory();
        if (enabled) {

            if (item == null) {
                if (Objects.equals(mode, PrismConfig.WandMode.BLOCK)) {
                    item = Material.SPRUCE_LOG;
                } else if (Objects.equals(mode, "item")) {
                    item = Material.STICK;
                } else {
                    item = Material.AIR;
                }
            }

            wand.setWandMode(mode);
            wand.setItem(item);

            PrismLogHandler.debug("Wand activated for player - mode: " + mode + " Item:" + item);

            // Move any existing item to the hand, otherwise give it to them
            if (plugin.config.wandConfig.autoEquip) {
                if (!InventoryUtils.moveItemToHand(inv, item)) {
                    // Store the item they're holding, if any
                    wand.setOriginallyHeldItem(inv.getItemInMainHand());
                    // They don't have the item, so we need to give them an item
                    if (InventoryUtils.handItemToPlayer(inv, new ItemStack(item, 1))) {
                        wand.setItemWasGiven(true);
                    } else {
                        Prism.messenger.sendMessage(call.getPlayer(),
                                Prism.messenger.playerError(Il8nHelper.getMessage("wand-inventory-full")));
                    }
                }
                InventoryUtils.updateInventory(call.getPlayer());
            }

            // Let's build the QueryParameters for it if it's a Query wand.
            if (wand instanceof QueryWandBase) {
                if (!((QueryWandBase) wand).setParameters(call.getPlayer(), call.getArgs(), (isInspect ? 1 : 2))) {
                    // This
                    // returns
                    // if
                    // it
                    // was
                    // successful
                    Prism.messenger.sendMessage(call.getPlayer(),
                            Prism.messenger.playerError(Il8nHelper.getMessage("wand-params-few")));
                }
            }

            // Store
            Prism.playersWithActiveTools.put(call.getPlayer().getName(), wand);
        } else {
            if (oldWand != null) {
                oldWand.disable(call.getPlayer());
            }
        }
    }

    static void sendWandStatus(final CommandSender sender,
                               @PropertyKey(resourceBundle = "languages.message") String wandStatusMessageKey,
                               final boolean status,
                               final String wandType,
                               final String parameters) {
        final TextComponent state;
        if (status) {
            state = Il8nHelper.getMessage("enabled").color(NamedTextColor.GREEN);
        } else {
            state = Il8nHelper.getMessage("disabled").color(NamedTextColor.RED);
        }
        TextComponent out = Prism.messenger
                .playerHeaderMsg(Il8nHelper.getMessage(wandStatusMessageKey)
                        .replaceText(builder -> builder.match("<status").replacement(state))
                );
        if (status) {
            out = out.append(Component.newline()).append(Il8nHelper.getMessage("wand-item-type")
                            .replaceText(builder -> builder.match("<itemType>").replacement(wandType).once())
                            .replaceText(builder -> builder.match("<parameters>").replacement(parameters))
                    );
        }
        Prism.messenger.sendMessage(sender, out);

    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{
                Il8nHelper.getRawMessage("help-inspector-wand"),
                Il8nHelper.getRawMessage("help-restore-wand"),
                Il8nHelper.getRawMessage("help-rollback-wand"),
                Il8nHelper.getRawMessage("help-profile-wand"),
        };
    }

    @Override
    public String getRef() {
        return "/wands.html";
    }
}