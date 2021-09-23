package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.TaskManager;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.settings.Settings;
import me.botsko.prism.text.ReplaceableTextComponent;
import me.botsko.prism.utils.ItemUtils;
import me.botsko.prism.wands.Wand;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class SetmyCommand extends AbstractCommand {

    private final Prism plugin;

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public SetmyCommand(Prism plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(CallInfo call) {

        String setType = null;
        if (call.getArgs().length >= 2) {
            setType = call.getArg(1);
        }

        if (setType != null && !setType.equalsIgnoreCase("wand")) {
            Prism.messenger.sendMessage(call.getPlayer(),
                    Prism.messenger.playerError("Invalid arguments. Use /prism ? for help."));
            return;
        }

        if (!plugin.config.wandConfig.allowUserOverride) {
            Prism.messenger.sendMessage(call.getPlayer(),
                    Prism.messenger.playerError("Sorry, but personalizing the wand is currently not allowed."));
        }

        // Check for any wand permissions. @todo There should be some central
        // way to handle this - some way to centralize it at least
        if (checkNoPermissions(call.getPlayer(), "prism.rollback", "prism.restore",
                "prism.wand.*", "prism.wand.inspect", "prism.wand.profile", "prism.wand.rollback",
                "prism.wand.restore")) {
            return;
        }

        // Disable any current wand
        if (Prism.playersWithActiveTools.containsKey(call.getPlayer().getName())) {
            final Wand oldwand = Prism.playersWithActiveTools.get(call.getPlayer().getName());
            oldwand.disable(call.getPlayer());
            Prism.playersWithActiveTools.remove(call.getPlayer().getName());
            WandCommand.sendWandStatus(call.getPlayer(), "wand-current", false, "", "");
        }

        String setSubType = null;
        if (call.getArgs().length >= 3) {
            setSubType = call.getArg(2).toLowerCase();
        }

        if (setSubType != null && setSubType.equals("mode")) {

            String setWandMode = null;
            if (call.getArgs().length >= 4) {
                setWandMode = call.getArg(3);
            }
            if (setWandMode != null
                    && (setWandMode.equals("hand") || setWandMode.equals("item") || setWandMode.equals("block"))) {
                TaskManager manager = Prism.getInstance().getTaskManager();
                final String mode = setWandMode;
                try {
                    manager.addTask(Settings.saveSettingAsync("wand.mode", mode, call.getPlayer()), result -> {
                        if (result) {
                            Prism.messenger.sendMessage(call.getPlayer(), Prism.messenger.playerHeaderMsg(
                                    ReplaceableTextComponent.builder("setWandMode")
                                            .replace("<wandMode>", mode,
                                                    Style.style(NamedTextColor.GREEN))
                                            .build()));
                        } else {
                            Prism.messenger.sendMessage(call.getPlayer(),
                                    Prism.messenger.playerError(Il8nHelper.getMessage("setmy-wandmode-error")));
                        }
                    }, false);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Error executing SetMy Command",e);
                }
                if (!setWandMode.equals("item")) {
                    Settings.deleteSettingAsync("wand.item", call.getPlayer());
                }
                return;
            }
            Prism.messenger.sendMessage(call.getPlayer(),
                    Prism.messenger.playerError(Il8nHelper.getMessage("invalid-arguments")));
            return;
        }

        if (setSubType != null && setSubType.equals("item")) {
            if (call.getArgs().length >= 4) {
                String wandString = call.getArg(3);
                Material setWand = Material.matchMaterial(wandString);

                // If non-material, check for name
                if (setWand == null) {
                    final ArrayList<Material> itemMaterials = Prism.getItems().getMaterialsByAlias(wandString);
                    if (itemMaterials.size() > 0) {
                        setWand = itemMaterials.get(0);
                    } else {
                        Prism.messenger.sendMessage(call.getPlayer(),
                              Prism.messenger.playerError(Il8nHelper.getMessage("item-no-match")));
                        return;
                    }
                }

                if (ItemUtils.isBadWand(setWand)) {
                    Prism.messenger.sendMessage(call.getPlayer(),
                            Prism.messenger.playerError(ReplaceableTextComponent.builder("wand-bad")
                                    .replace("<itemName>", wandString).build()));
                    return;
                }

                Settings.saveSettingAsync("wand.item", wandString, call.getPlayer());
                Prism.messenger.sendMessage(call.getPlayer(), Prism.messenger.playerHeaderMsg(
                        ReplaceableTextComponent.builder("wand-item-change").replace("<itemName>", wandString)
                                .build()));
                return;
            }
        }
        Prism.messenger.sendMessage(call.getPlayer(),
                Prism.messenger.playerError(Il8nHelper.getMessage("invalid-arguments")));
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-wand-set")};
    }

    @Override
    public String getRef() {
        return "/wand.html#setting-resetting-the-wand";
    }
}