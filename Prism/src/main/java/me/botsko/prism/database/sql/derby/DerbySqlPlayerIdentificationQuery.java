package me.botsko.prism.database.sql.derby;

import me.botsko.prism.database.sql.SqlPlayerIdentificationQuery;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 23/01/2021.
 */
public class DerbySqlPlayerIdentificationQuery extends SqlPlayerIdentificationQuery {
    @Override
    protected String getSelectByUuid() {
        return "SELECT player_id, player, player_uuid FROM " + prefix + "players WHERE player_uuid = ?";
    }

    @Override
    protected String getSelectByName() {
        return "SELECT player_id, player, player_uuid FROM " + prefix + "players WHERE player = ?";
    }

    @Override
    protected String getInsertPlayer() {
        return "INSERT INTO " + prefix + "players (player,player_uuid) VALUES (?,?)";
    }

    @Override
    protected String getUpdatePlayerSql() {
        return "UPDATE " + prefix + "players SET player = ?, "
                + "player_uuid = ? WHERE player_id = ?";
    }

    @Override
    protected String getSelectByNames() {
        return "SELECT player_id, player, player_uuid FROM " + prefix + "players WHERE player IN (?)";
    }
}
