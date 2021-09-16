package me.botsko.prism;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class Messenger {


    private final AudienceProvider audienceProvider;
    private final String pluginName;
    private static final TextColor defaultColor = TextColor.color(0xb5bcc7);
    private static final TextColor headerColor = TextColor.color(0xb597ba);
    private static final TextColor error = TextColor.color(0x6e1017);
    private static final TextColor success = TextColor.color(0x4fab55);

    /**
     * Build the class.
     *
     * @param pluginName String
     */
    @SuppressWarnings("WeakerAccess")
    public Messenger(String pluginName, AudienceProvider provider) {
        this.pluginName = pluginName;
        this.audienceProvider = provider;
    }

    /**
     * Send a message.
     *
     * @param sender  CommandSender
     * @param message {@link Component}
     */
    public void sendMessage(CommandSender sender, Component message) {
        if (sender instanceof ConsoleCommandSender) {
            audienceProvider.console().sendMessage(Identity.nil(),message);
        } else {
            ((BukkitAudiences) audienceProvider).sender(sender).sendMessage(Identity.nil(),
                  message.colorIfAbsent(defaultColor));
        }
    }

    /**
     * Get the header.
     *
     * @param msg the message to prefix.
     * @return String.
     */
    public TextComponent playerHeaderMsg(Component msg) {
        if (msg != null) {
            return Component.text(pluginName + " ")
                    .color(headerColor)
                    .append(msg.colorIfAbsent(NamedTextColor.WHITE));
        }
        return Component.empty();
    }

    @Deprecated
    public TextComponent playerHeaderMsg(String msg) {
        return this.playerHeaderMsg(LegacyComponentSerializer.legacySection().deserialize(msg));
    }

    /**
     * Get the subdued header.
     *
     * @param msg the message to prefix.
     * @return String.
     */
    public TextComponent playerSubduedHeaderMsg(Component msg) {
        if (msg != null) {
            return Component.text(pluginName + " ")
                    .color(headerColor)
                    .append(msg.colorIfAbsent(defaultColor));
        }
        return Component.empty();
    }

    @Deprecated
    public TextComponent playerSubduedHeaderMsg(String msg) {
        return playerSubduedHeaderMsg(LegacyComponentSerializer.legacySection().deserialize(msg));
    }

    /**
     * Get the message.
     *
     * @param msg the message to prefix.
     * @return String.
     */
    @Deprecated
    public Component playerMsg(String msg) {
        if (msg != null) {
            Component component = LegacyComponentSerializer.legacySection().deserialize(msg);
            return playerMsg(component);
        }
        return Component.empty();
    }

    /**
     * Get the message colored white by default.
     *
     * @param msg TextComponent
     * @return TextComponent
     */
    public Component playerMsg(Component msg) {
        if (msg != null) {
            return msg.colorIfAbsent(defaultColor);
        }
        return Component.empty();
    }

    /**
     * Get the help message.
     *
     * @param cmd  cmd to get help.
     * @param help - a message.
     * @return String.
     */
    public TextComponent playerHelp(String cmd, String help) {
        return Component.text("/prism ").color(defaultColor)
                .append(Component.text(cmd).color(headerColor)
                        .append(Component.text(" - " + help).color(NamedTextColor.WHITE)));
    }

    /**
     * Get the error message.
     *
     * @param msg the message to prefix.
     * @return String.
     */
    public TextComponent playerError(Component msg) {
        return Component.text(pluginName + " ")
                .color(headerColor)
                .append(msg.colorIfAbsent(error));
    }

    public TextComponent playerError(String msg) {
        return playerError(Component.text(msg));
    }

    /**
     * Get the Success message.
     *
     * @param msg the message to prefix.
     * @return String.
     */
    public TextComponent playerSuccess(String msg) {
        if (msg != null) {
            return playerSuccess(Component.text(msg));
        }
        return Component.empty();
    }

    /**
     * Get the Success message.
     *
     * @param msg the message to prefix.
     * @return String.
     */
    public TextComponent playerSuccess(Component msg) {
        if (msg != null) {
            return Component.text("[" + pluginName + "]" + " ")
                    .color(headerColor)
                    .append(msg.colorIfAbsent(success));
        }
        return Component.empty();
    }

    /**
     * Send a message to console.
     * @param msg the message.
     */
    public void sendConsoleMessage(Component msg) {
        audienceProvider.console().sendMessage(Identity.nil(),msg);
    }

}