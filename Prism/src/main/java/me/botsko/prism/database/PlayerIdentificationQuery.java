package me.botsko.prism.database;

import me.botsko.prism.players.PrismPlayer;

import java.util.UUID;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 22/01/2021.
 */
public interface PlayerIdentificationQuery {

    void addPlayer(final String name, final UUID uuid);

    PrismPlayer addFakePlayer(String playerName);

    void cacheOnlinePlayerPrimaryKeys(String[] playerNames);

    PrismPlayer lookupByName(String playerName);

    PrismPlayer lookupByUuid(UUID uuid);

    void updatePlayer(PrismPlayer prismPlayer);
}
