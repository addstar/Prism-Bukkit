package me.botsko.prism.database.sql;

import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.database.AbstractSettingsQuery;
import me.botsko.prism.database.SettingsQuery;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/04/2019.
 */
@SuppressWarnings("SqlResolve")
public class SqlSettingsQuery extends AbstractSettingsQuery implements SettingsQuery {
    private final SqlPrismDataSource dataSource;
    protected static String prefix = "prism_";

    public SqlSettingsQuery(SqlPrismDataSource dataSource) {
        this.dataSource = dataSource;
        prefix = dataSource.getPrefix();
    }

    @Override
    public boolean deleteSetting(String key, Player player) {
        try (
                Connection conn =  dataSource.getConnection();
                PreparedStatement s = conn.prepareStatement("DELETE FROM " + prefix + "meta WHERE k = ?")
        ) {
            String finalKey = key;
            if (player != null) {
                finalKey = getPlayerKey(player, key);
            }
            s.setString(1, finalKey);
            int result = s.executeUpdate();
            return result == 1;
        } catch (final SQLException e) {
            PrismLogHandler.debug("Database Error:" + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean saveSetting(String key, String value, Player player) {
        if (!deleteSetting(key,player)) {
            PrismLogHandler.debug("Setting " + key + " was not found - DELETE failed");
        }
        String finalKey = key;
        if (player != null) {
            finalKey = getPlayerKey(player, key);
        }
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement s2 = conn.prepareStatement(getInsertQuery())
                ) {
            s2.setString(1, finalKey);
            s2.setString(2, value);
            return s2.executeUpdate() == 1;
        } catch (final SQLException e) {
            PrismLogHandler.debug("Database Error:" + e.getMessage());
        }
        return false;
    }


    @Override
    public @Nullable  String getSetting(String key, Player player) {
        String value = null;
        ResultSet rs;


        String finalKey = key;
        if (player != null) {
            finalKey = getPlayerKey(player, key);
        }
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement s =
                        conn.prepareStatement(getSelectQuery())
        ) {
            s.setString(1, finalKey);
            rs = s.executeQuery();

            while (rs.next()) {
                value = rs.getString("v");
            }

        } catch (final SQLException e) {
            PrismLogHandler.debug("Database Error:" + e.getMessage());
        }
        return value;
    }

    protected String getSelectQuery() {
        return "SELECT v FROM " + prefix + "meta WHERE k = ? FETCH FIRST 1 row only";
    }

    protected String getInsertQuery() {
        return "INSERT INTO " + prefix + "meta (id, k , v) VALUES (DEFAULT,?,?)";
    }
}
