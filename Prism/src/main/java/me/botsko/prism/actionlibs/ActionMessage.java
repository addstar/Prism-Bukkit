package me.botsko.prism.actionlibs;

import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.utils.block.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.regex.Pattern;

public class ActionMessage {

    protected final Handler handler;

    private boolean showExtended = false;

    private int index = 0;

    public ActionMessage(Handler handler) {
        this.handler = handler;
    }

    public final int getIndex() {
        return index;
    }

    public void showExtended() {
        showExtended = true;
    }

    public void setResultIndex(int index) {
        this.index = index;
    }

    /**
     * Here, we don't use formatting or anything, we just use a regular message raw.
     * This will automatically show extended information, as this can be passed to a
     * paste service.
     *
     * @return String
     */
    public String getRawMessage() {
        String format1 = "<prefix><handlerId><target><actor><extendedInfo><actorNice><count>"
                + "<timeDiff><location>";
        ActionType action = handler.getActionType();
        return PlainComponentSerializer.plain().serialize(getMainMessage(action, format1));
    }

    private TextComponent getMainMessage(ActionType action, String format1) {
        final TextColor highlight = NamedTextColor.DARK_AQUA;
        TextComponent out = Component.text().content(format1).build();
        Component result = out
                .replaceFirstText(Pattern.compile("<prefix>"),
                        builder -> getPosNegPrefix().decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE))
                .replaceFirstText(Pattern.compile("<index>"),
                        builder -> builder.content("[" + index + "] ").color(NamedTextColor.GRAY).decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE))
                .replaceFirstText(Pattern.compile("<target>"),
                        builder -> Component.text().content(" " + handler.getSourceName() + " ").color(highlight))
                .replaceFirstText(Pattern.compile("<description>"),
                        builder -> Component.text().content(getDescription((ActionTypeImpl)action)+ " ")
                                .color(NamedTextColor.WHITE))
                .replaceFirstText(Pattern.compile("<actorNice>"),
                        builder -> getActor((ActionTypeImpl)action, highlight))
                .replaceFirstText(Pattern.compile("<actor>"),
                        builder -> Component.text().content(action.getName() + " "))
                .replaceFirstText(Pattern.compile("<extendedInfo>"),
                        builder -> Component.text().append(getExtendedInfo()))
                .replaceFirstText(Pattern.compile("<timeDiff>"),
                        builder -> Component.text().append(getTimeDiff()))
                .replaceFirstText(Pattern.compile("<count>"),
                        builder -> Component.text().append(getCount()))
                .replaceFirstText(Pattern.compile("<actionType>"),
                        builder -> Component.text()
                                .content(" (a:" + action.getShortName() + ")")
                                .color(NamedTextColor.GRAY).decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE))
                .replaceFirstText(Pattern.compile("<handlerId>"),
                        builder -> Component.text(handler.getId()).toBuilder()
                                .color(NamedTextColor.GRAY));
        if (handler.isRollbacked()) {
            result = result.decorate(TextDecoration.STRIKETHROUGH);
        }
        return Component.text()
                .content("")
                .append(result)
                .hoverEvent(HoverEvent.showText(Component.text("Click to teleport")
                        .color(NamedTextColor.DARK_AQUA)))
                .clickEvent(ClickEvent.runCommand("/pr tp " + index))
                .build();

    }

    /**
     * Get the message.
     *
     * @return String[]
     */
    public TextComponent getMessage() {
        String format1 =
                "<prefix><index><target><description><actorNice><extendedInfo><count><timeDiff><actionType>";
        String format2 = "-<handlerId>- <dateTime> -<location>";
        ActionType action = handler.getActionType();
        TextComponent out = getMainMessage(action, format1);
        if (showExtended) {
            out = out.append(Component.newline());
            Component line2 = Component.text().content(format2).build()
                    .replaceFirstText(Pattern.compile("<handlerId>"),
                            builder -> Component.text(handler.getId()).toBuilder()
                                    .color(NamedTextColor.GRAY))
                    .replaceFirstText(Pattern.compile("<dateTime>"),
                            builder -> Component.text()
                                    .content(handler.getDisplayDate() + " " + handler.getDisplayTime()))
                    .replaceFirstText(Pattern.compile("<location>"),
                            builder -> Component.text().content(getFormattedLocation()));
            out = out.append(line2);
        }
        return out;
    }

    private String getFormattedLocation() {
        Location l = handler.getLoc();
        if (l.getWorld() != null) {
            return " " + l.getWorld().getName() + " @ " + l.getBlockX() + " "
                    + l.getBlockY() + " " + l.getBlockZ();
        }
        return "INVALID";
    }


    private String getDescription(ActionTypeImpl action) {
        String description = handler.getCustomDesc();
        if (description == null) {
            description = action.getNiceDescription();
        }
        return description;
    }

    private TextComponent getExtendedInfo() {
        if (showExtended && (handler.getMaterial() != Material.AIR)) {
            return Component.text(handler.getMaterial() + Utilities.dataString(handler.getBlockData()) + " ");
        }
        return Component.empty();
    }

    private TextComponent.Builder getActor(ActionTypeImpl action, TextColor highlight) {
        String target = "unknown";
        if (action.getHandler() != null) {
            if (!handler.getNiceName().isEmpty()) {
                target = handler.getNiceName();
            }
        } else {
            // We should really improve this, but this saves me from having to
            // make
            // a custom handler.
            if (action.getName().equals("lava-bucket")) {
                target = "lava";
            } else if (action.getName().equals("water-bucket")) {
                target = "water";
            }
        }
        return Component.text()
                .content(target + " ")
                .color(highlight);
    }

    private TextComponent getCount() {
        if (handler.getAggregateCount() > 1) {
            return Component.text("x" + handler.getAggregateCount() + "  ");
        }
        return Component.text(" ");
    }

    private TextComponent getTimeDiff() {
        // Time since
        if (!handler.getTimeSince().isEmpty()) {
            return Component.text().content(handler.getTimeSince()).color(NamedTextColor.WHITE).build();
        } else {
            return Component.empty();
        }
    }

    private TextComponent.Builder getPosNegPrefix() {
        if (handler.getActionType().doesCreateBlock() || handler.getActionType().getName().equals("item-insert")
                || handler.getActionType().getName().equals("sign-change")) {
            return Component.text().content("+ ").color(NamedTextColor.GREEN);
        } else {
            return Component.text().content("- ").color(NamedTextColor.RED);
        }
    }
}