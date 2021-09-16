package me.botsko.prism.database.sql;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.database.ActionReportQuery;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.utils.TypeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SqlActionReportQueryBuilder extends SqlSelectQueryBuilder implements ActionReportQuery {

    public SqlActionReportQueryBuilder(PrismDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public String getQuery(PrismParameters parameters, boolean shouldGroup) {

        this.parameters = parameters;
        this.shouldGroup = shouldGroup;

        // Reset
        columns = new ArrayList<>();
        conditions = new ArrayList<>();

        String query = select();

        query += ";";

        PrismLogHandler.debug(query);

        return query;

    }

    @Override
    public String select() {
        return "SELECT COUNT(*), a.action " + "FROM " + prefix + "data "
                + "INNER JOIN " + prefix + "actions a "
                + "ON a.action_id = " + prefix + "data.action_id "
                + where() + " "
                + "GROUP BY a.action_id "
                + "ORDER BY COUNT(*) DESC";
    }

    @Override
    public void report(CommandSender sender) {
        String tempName = "";
        for (final String player : parameters.getPlayerNames().keySet()) {
            tempName = player;
            break;
        }
        final String playerName = tempName;
        final int colTextLen = 16;
        final int colIntLen = 12;
        Prism.messenger.sendMessage(sender, Prism.messenger.playerSubduedHeaderMsg(
                Il8nHelper.formatMessage("actionreport-crafting", playerName)));
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement s = conn.prepareStatement(getQuery(parameters, shouldGroup));
                ResultSet rs = s.executeQuery()
        ) {
            TextComponent.Builder builder = Component.text();
            builder.append(Component.text(TypeUtils.padStringRight("Action", colTextLen), NamedTextColor.GRAY))
                    .append(Component.text(TypeUtils.padStringRight("Count", colIntLen), NamedTextColor.GRAY));
            while (rs.next()) {
                final String action = rs.getString(2);
                final int count = rs.getInt(1);

                final String colAlias = TypeUtils.padStringRight(action, colTextLen);
                final String colPlaced = TypeUtils.padStringRight("" + count, colIntLen);
                builder.append(Component.text(colAlias).color(TextColor.color(0x158258))
                        .append(Component.text(colPlaced, NamedTextColor.GREEN)));

            }
            Prism.messenger.sendMessage(sender, builder.build());

        } catch (final SQLException e) {
            dataSource.handleDataSourceException(e);
        }
    }
}
