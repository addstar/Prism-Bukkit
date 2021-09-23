package me.botsko.prism.database.mysql;

import me.botsko.prism.database.sql.SqlPlayerIdentificationQuery;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 23/01/2021.
 */
public class MySqlPlayerIdentificationQuery extends SqlPlayerIdentificationQuery {

    protected String getSelectByUuid() {
        return "SELECT player_id, player, HEX(player_uuid) FROM " + prefix + "players WHERE player_uuid = UNHEX(?)";
    }

    @Override
    protected String getSelectByName() {
        return "SELECT player_id, player, HEX(player_uuid) FROM " + prefix + "players WHERE player = ?";
    }

    @Override
    protected String getSelectByNames() {
        return "SELECT player_id, player, HEX(player_uuid) FROM " + prefix + "players WHERE player IN (?)";
    }

    @Override
    protected String getInsertPlayer() {
        return "INSERT INTO " + prefix + "players (player,player_uuid) VALUES (?,UNHEX(?))";
    }

    @Override
    protected String getUpdatePlayerSql() {
        return "UPDATE " + prefix + "players SET player = ?, "
                + "player_uuid = UNHEX(?) WHERE player_id = ?";
    }

}
