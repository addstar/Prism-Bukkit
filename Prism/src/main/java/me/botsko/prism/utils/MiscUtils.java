package me.botsko.prism.utils;

import com.google.common.base.CaseFormat;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.actionlibs.ActionMessage;
import me.botsko.prism.api.Result;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.config.PrismConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.kitteh.pastegg.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class MiscUtils {

    /**
     * Placing this here so it's easier to share the logic.
     *
     * @param player        Player
     * @param desiredRadius integer
     * @param processType   {@link PrismProcessType}
     * @param config        {@link FileConfiguration}
     * @return integer
     */
    public static int clampRadius(Player player, int desiredRadius, PrismProcessType processType,
                                  PrismConfig config) {

        if (desiredRadius <= 0) {
            return config.nearCommandConfig.defaultRadius;
        }

        // Safety checks for max lookup radius
        int maxLookupRadius = config.parameterConfig.maxLookupRadius;
        if (maxLookupRadius <= 0.99) {
            maxLookupRadius = 5;
            PrismLogHandler.log("Max lookup radius may not be lower than one. Using safe input of five.");
        }

        // Safety checks for max applier radius
        int maxApplierRadius = config.parameterConfig.maxApplierRadius;
        if (maxApplierRadius <= 0.99) {
            maxApplierRadius = 5;
            PrismLogHandler.log("Max applier radius may not be lower than one. Using safe input of five.");
        }

        // Does the radius exceed the configured max?
        if (processType.equals(PrismProcessType.LOOKUP) && desiredRadius > maxLookupRadius) {
            // If player does not have permission to override the max
            if (player != null && !player.hasPermission("prism.override-max-lookup-radius")) {
                return maxLookupRadius;
            }
            // Otherwise non-player
            return desiredRadius;
        } else if (!processType.equals(PrismProcessType.LOOKUP) && desiredRadius
                > maxApplierRadius) {
            // If player does not have permission to override the max
            if (player != null && !player.hasPermission("prism.override-max-applier-radius")) {
                return maxApplierRadius;
            }
            // Otherwise non-player
            return desiredRadius;
        } else {
            // Otherwise, the radius is valid and is not exceeding max
            return desiredRadius;
        }
    }

    /**
     * Get an enum that extends T as a fall back class.
     *
     * @param from     String
     * @param fallback the Class to fallback too
     * @param <T>      THe Class extending Enum
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T getEnum(String from, T fallback) {
        if (from != null) {
            try {
                return (T) Enum.valueOf(fallback.getClass(), from.toUpperCase());
            } catch (IllegalArgumentException e) {
                PrismLogHandler.debug(e.getMessage());
            }
        }
        return fallback;
    }

    /**
     * Get the enum from the class.
     * @param from String
     * @param enumClass Class
     * @param <T> Enum
     * @return Enum
     */
    public static <T extends Enum<T>> T getEnum(String from, Class<T> enumClass) {
        if (from != null) {
            try {
                return Enum.valueOf(enumClass, from.toUpperCase());
            } catch (IllegalArgumentException e) {
                PrismLogHandler.debug(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Nice name for a String.
     * @param in String
     * @return String
     */
    public static String niceName(String in) {
        if (in == null) {
            return "";
        }
        String[] parts = in.replace('_', ' ').trim().split("", 2);
        return parts[0].toUpperCase() + parts[1].toLowerCase();
    }

    /**
     * Paste results and return a web address.
     * Async Method.
     *
     * @param sender  CommandSender.
     * @param results String
     */
    public static void paste_results(CommandSender sender, String results) {

        final String prismWebUrl = "https://paste.gg/";

        if (!Prism.getInstance().config.pasteConfig.enabled) {
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerError(
                            Component.text("Paste.gg support is currently disabled by config.")));
            return;
        }
        ZonedDateTime expire = ZonedDateTime.now().plusMinutes(60);
        PasteFile file = new PasteFile("Prism Result",
                new PasteContent(PasteContent.ContentType.TEXT, results));

        final PasteBuilder.PasteResult result
                = new PasteBuilder().name("Prism Results")
                .setApiKey(Prism.getPasteKey())
                .expires(expire)
                .addFile(file)
                .visibility(Visibility.UNLISTED)
                .build();
        if (result.getPaste().isPresent()) {
            Paste paste = result.getPaste().get();
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerSuccess("Successfully pasted results: "
                            + prismWebUrl
                            + paste.getId()).clickEvent(ClickEvent.openUrl(prismWebUrl)));
        } else {
            String message = result.getMessage().isPresent() ? result.getMessage().get() : "";
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerError(
                            Component.text("Unable to paste results (")
                                    .append(Component.text(message).color(NamedTextColor.YELLOW))
                                    .append(Component.text(")."))
                    ));
        }

    }

    /**
     * Send clickable record.
     *
     * @param a      ActionMessage
     * @param player CommandSender
     */
    public static void sendClickableTpRecord(ActionMessage a, CommandSender player) {
        Prism.messenger.sendMessage(player, a.getMessage());
    }

    /**
     * Send page buttons.
     *
     * @param results Results
     * @param player  Player
     */
    public static void sendPageButtons(Result results, CommandSender player) {
        if (player instanceof Player) {
            if (results.getPage() == 1) {
                if (results.getTotalPages() > 1) {
                    Prism.messenger.sendMessage(player, getNextButton());
                }
            } else if (results.getPage() < results.getTotalPages()) {
                Prism.messenger.sendMessage(player, MiscUtils.getPrevNextButtons());
            } else if (results.getPage() == results.getTotalPages()) {
                Prism.messenger.sendMessage(player, MiscUtils.getPreviousButton());
            }
        }
    }

    /**
     * Gets a list of strings starting with.
     *
     * @param start         String
     * @param options       Options
     * @param caseSensitive if case sensitive
     * @return List of Strings
     */
    public static List<String> getStartingWith(final String start, Iterable<String> options,
                                               boolean caseSensitive) {
        final List<String> result = new ArrayList<>();
        if (caseSensitive) {
            for (final String option : options) {
                if (option.startsWith(start)) {
                    result.add(option);
                }
            }
        } else {
            String caseStart = start.toLowerCase();
            for (final String option : options) {
                if (option.toLowerCase().startsWith(caseStart)) {
                    result.add(option);
                }
            }
        }

        return result;
    }

    public static List<String> getStartingWith(String arg, Iterable<String> options) {
        return getStartingWith(arg, options, true);
    }

    /**
     * Send alert.
     *
     * @param msg      the message
     * @param commands the commands
     */
    public static void dispatchAlert(String msg, Iterable<String> commands) {
        String cleanMessage = PlainTextComponentSerializer.plainText().deserialize(msg).content();
        for (String command : commands) {
            if ("examplecommand <alert>".equals(command)) {
                continue;
            }
            String processedCommand = command.replace("<alert>", cleanMessage);
            Bukkit.getScheduler().runTask(Prism.getInstance(),
                    () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand));
        }
    }

    /**
     * FInd a nice name for entity.
     *
     * @param entity Entity
     * @return String
     */
    public static String getEntityName(Entity entity) {
        if (entity == null) {
            return "unknown";
        }
        if (entity.getType() == EntityType.PLAYER) {
            return entity.getName();
        }
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, entity.getType().name());
    }

    /**
     * Prev Button.
     *
     * @return TextComponent
     */
    public static TextComponent getPreviousButton() {
        return Component.text(" [<< Prev]")
                .color(TextColor.fromHexString("#ef9696"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to view the previous page")))
                .clickEvent(ClickEvent.runCommand("/pr pg p"));
    }

    /**
     * NExt button.
     *
     * @return TextComponent
     */
    public static TextComponent getNextButton() {
        return Component.text("           ")
                .color(TextColor.fromHexString("#01a960"))
                .append(MiscUtils::getNextButtonComponent);
    }

    /**
     * Next Button.
     *
     * @return BaseComponent.
     */
    private static TextComponent getNextButtonComponent() {
        return Component.text("[Next >>]")
                .hoverEvent(HoverEvent.showText(Component.text("Click to view the next page")))
                .color(TextColor.fromHexString("#01a960"))
                .clickEvent(ClickEvent.runCommand("/pr pg n"));
    }

    /**
     * Prev Button.
     *
     * @return BaseComponent.
     */
    public static TextComponent getPrevNextButtons() {
        TextComponent divider = Component.text(" | ")
                .color(TextColor.fromHexString("#969696"));
        return Component.text()
                .append(getPreviousButton())
                .append(divider)
                .append(getNextButton()).build();
    }
}