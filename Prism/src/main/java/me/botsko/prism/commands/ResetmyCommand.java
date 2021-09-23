package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.TaskManager;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.settings.Settings;
import me.botsko.prism.text.ReplaceableTextComponent;
import me.botsko.prism.wands.Wand;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

import java.util.List;
import java.util.function.Consumer;

public class ResetmyCommand extends AbstractCommand {

    private final Prism plugin;

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public ResetmyCommand(Prism plugin) {
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
                    Prism.messenger.playerError(Il8nHelper.formatMessage("invalid-arguments",setType)));
            return;
        }

        if (!plugin.config.wandConfig.allowUserOverride) {
            Prism.messenger.sendMessage(call.getPlayer(),
                    Prism.messenger.playerError(Il8nHelper.getMessage("wand-personal-blocked")));
        }
        if (checkNoPermissions(call.getPlayer(), "prism.rollback", "prism.restore", "prism.wand.*",
                "prism.wand.inspect", "prism.wand.profile", "prism.wand.rollback",
                "prism.wand.restore")) {
            return;
        }
        // Disable any current wand
        if (Prism.playersWithActiveTools.containsKey(call.getPlayer().getName())) {
            final Wand oldwand = Prism.playersWithActiveTools.get(call.getPlayer().getName());
            oldwand.disable(call.getPlayer());
            Prism.playersWithActiveTools.remove(call.getPlayer().getName());
            Prism.messenger.sendMessage(call.getPlayer(), Prism.messenger
                    .playerHeaderMsg(ReplaceableTextComponent.builder("wand-current")
                            .replace("<status", Il8nHelper.getRawMessage("disabled"),
                                    Style.style(NamedTextColor.RED))
                            .build()));
        }
        TaskManager manager = Prism.getInstance().getTaskManager();
        try {
            manager.addTask(Settings.deleteSettingAsync("wand.item", call.getPlayer()), new Consumer<Boolean>() {
                @Override
                public void accept(Boolean result) {
                    if (result) {
                        Prism.messenger.sendMessage(call.getPlayer(),
                                Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("wand-item-reset")));
                    }
                }
            }, false);
            manager.addTask(Settings.deleteSettingAsync("wand.mode", call.getPlayer()), success -> {
                if (success) {
                    Prism.messenger.sendMessage(call.getPlayer(),
                            Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("wand-mode-reset")));
                }
            },false);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-wand-reset-default")};
    }

    @Override
    public String getRef() {
        return "/wand.html#setting-resetting-the-wand";
    }
}