package me.botsko.prism.actionlibs;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.api.actions.Action;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.utils.block.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;

public class ActionMessage {

    private static final String format1 = Il8nHelper.getRawMessage("result-message-format-normal");
    private static final String format2line1 = Il8nHelper.getRawMessage("result-message-format-extended-1");
    private static final String format2line2 = Il8nHelper.getRawMessage("result-message-format-extended-2");

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
        Action action = handler.getAction();
        return PlainComponentSerializer.plain().serialize(getMainMessage(action, format1));
    }

    private Component getMainMessage(Action action, String format) {
        final TextColor highlight = NamedTextColor.DARK_AQUA;
        TextComponent out = Component.text().content(format).build();
        Component result = out
                .replaceText(builder -> builder.match("<prefix>").replacement(getPosNegPrefix()).once())
                .replaceText(builder -> builder.match("<index>")
                        .replacement(Component.text("[" + index + "] ").color(NamedTextColor.GRAY)).once())
                .replaceText(builder -> builder.match("<target>")
                        .replacement(Component.text(
                                handler.getSourceName() == null ? "NULL" : handler.getSourceName())
                                .color(highlight)).once())
                .replaceText(builder -> builder.match("<description>").replacement(
                        Component.text(getDescription((ActionImpl) action))
                                .color(NamedTextColor.WHITE)).once())
                .replaceText(builder -> builder.match("<actorNice>").replacement(getActor(action,highlight)).once())
                .replaceText(builder -> builder.match("<actor>").replacement(Component.text(action.getName())).once())
                .replaceText(builder -> builder.match("<extendedInfo>").replacement(getExtendedInfo()).once())
                .replaceText(builder -> builder.match("<timeDiff>").replacement(getTimeDiff()).once())
                .replaceText(builder -> builder.match("<count>").replacement(getCount()).once())
                .replaceText(builder -> builder.match("<actionType>")
                        .replacement(Component.text("(a:" + action.getShortName() + ")")
                                .color(NamedTextColor.GRAY)).once())
                .replaceText(builder -> builder.match("<handlerId>")
                        .replacement(Component.text(handler.getId())
                                .color(NamedTextColor.GRAY)).once());
        return result.hoverEvent(HoverEvent.showText(
                    Component.text("Click to teleport").color(NamedTextColor.DARK_AQUA)))
                .clickEvent(ClickEvent.runCommand("/pr tp " + index));
    }

    /**
     * Get the message.
     *
     * @return String[]
     */
    public Component getMessage() {
        Action action = handler.getAction();
        Component out = getMainMessage(action, format2line1);
        if (showExtended) {
            out = out.append(Component.newline());
            Component line2 = Component.text().content(format2line2).build()
                    .replaceText(builder -> builder.match("<handlerId>")
                            .replacement(Component.text(handler.getId())
                                    .color(NamedTextColor.GRAY)).once())
                    .replaceText(builder -> builder.match("<dateTime>")
                            .replacement(Component.text(handler.getDisplayDate() + " "
                                    + handler.getDisplayTime())))
                    .replaceText(builder -> builder.match("<location")
                            .replacement(getFormattedLocation()).once());
            out = out.append(line2);
        }
        return out;
    }

    private String getFormattedLocation() {
        Location l = handler.getLoc();
        if (l.getWorld() != null) {
            return l.getWorld().getName() + " @ " + l.getBlockX() + " "
                    + l.getBlockY() + " " + l.getBlockZ();
        }
        return "INVALID";
    }


    private String getDescription(ActionImpl action) {
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

    private TextComponent.Builder getActor(Action action, TextColor highlight) {
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
                .content(target)
                .color(highlight);
    }

    private TextComponent getCount() {
        if (handler.getAggregateCount() > 1) {
            return Component.text("x" + handler.getAggregateCount() + " ");
        }
        return Component.empty();
    }

    private TextComponent getTimeDiff() {
        // Time since
        if (!handler.getTimeSince().isEmpty()) {
            return Component.text().content(handler.getTimeSince()).color(NamedTextColor.WHITE).build();
        } else {
            return Component.empty();
        }
    }

    private TextComponent getPosNegPrefix() {
        if (handler.getAction().doesCreateBlock() || handler.getAction().getName().equals("item-insert")
                || handler.getAction().getName().equals("sign-change")) {
            return Component.text("+").color(NamedTextColor.GREEN);
        } else {
            return Component.text("-").color(NamedTextColor.RED);
        }
    }
}