package me.botsko.prism.players;

import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.database.PlayerIdentificationQuery;
import me.botsko.prism.database.PrismDataSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerIdentification {

    public static final Map<UUID, PrismPlayer> prismPlayers = new HashMap<>();
    private static PlayerIdentificationQuery playerIdHelper;

    public PlayerIdentification(PrismDataSource dataSource) {
        PlayerIdentification.playerIdHelper = dataSource.getPlayerIdHelper();
    }

    public Map<UUID, PrismPlayer> getPrismPlayers() {
        return prismPlayers;
    }

    /**
     * Loads `prism_players` ID for a real player into our cache.
     * Runs during PlayerJoin events, so it will never be for a fake/null player.
     *
     * @param uuid Player uuid
     * @param name String
     */
    public void cachePrismPlayer(UUID uuid, String name) {
        PrismPlayer prismPlayer;
        prismPlayer = getPrismPlayer(uuid, name);
        if (prismPlayer != null) {
            comparePlayerToCache(name, uuid, prismPlayer);
            PrismLogHandler.debug("Saving player " + name + ", id: " + uuid + " into the cache.");
            prismPlayers.put(uuid, prismPlayer);
            return;
        }
        playerIdHelper.addPlayer(name, uuid);
    }

    /**
     * Gets a player from the cache by name in general this is always an online player but if not it
     * will attempt to get the player id from the database.
     */
    public PrismPlayer getPrismPlayerByNameFromCache(final String playerName) {

        // Lookup the player
        PrismPlayer prismPlayer = getPrismPlayer(playerName);
        if (prismPlayer != null) {
            // prismPlayer = comparePlayerToCache( player, prismPlayer );
            PrismLogHandler.debug("Loaded player " + prismPlayer.getName() + ", id: "
                    + prismPlayer.getId() + " from the cache.");
            // Prism.prismPlayers.put( player.getUniqueId(), prismPlayer );
            return prismPlayer;
        }

        // Player is fake, create a record for them
        prismPlayer = playerIdHelper.addFakePlayer(playerName);

        return prismPlayer;

    }

    /**
     * Returns a `prism_players` ID for the described player name. If one cannot be
     * found, returns 0.
     * Used by the recorder in determining proper foreign key
     * - Possibly performs db lookup.,
     *
     * @param playerName String
     * @return PrismPlayer
     */
    private PrismPlayer getPrismPlayer(String playerName) {

        Player player = Bukkit.getPlayer(playerName);

        if (player != null) {
            return getPrismPlayer(player.getUniqueId(), player.getName());
        }

        // Player not online, we need to go to cache

        return playerIdHelper.lookupByName(playerName);

    }

    /**
     * Returns a `prism_players` ID for the described player object. If one cannot
     * be found, returns 0. - Possibly performs db lookup.,
     * Used by the recorder in determining proper foreign key
     *
     * @return PrismPlayer
     */
    private @Nullable PrismPlayer getPrismPlayer(UUID uuid, String name) {

        PrismPlayer prismPlayer;
        // Are they in the cache?
        prismPlayer = prismPlayers.get(uuid);
        if (prismPlayer != null) {
            return prismPlayer;
        }

        // Lookup by UUID
        prismPlayer = playerIdHelper.lookupByUuid(uuid);
        if (prismPlayer != null) {
            if (!prismPlayer.getName().equals(name)) {
                prismPlayer.setName(name);
                playerIdHelper.updatePlayer(prismPlayer);
            }
            return prismPlayer;
        }
        // Still not found, try looking them up by name
        prismPlayer = playerIdHelper.lookupByName(name);
        prismPlayer = comparePlayerToCache(name, uuid, prismPlayer);
        // now check if the uuid is the same as the one logging in ...if it isn't we likely need to
        // create a new player and update the old one with a new name
        return prismPlayer;

    }

    /**
     * Compares the known player to the cached data. If there's a difference we need
     * to handle it.
     * If usernames are different: Update `prism_players` with new name (@todo track
     * historical?)
     * If UUID is different, log an error.
     *
     * @param name        Player name
     * @param uuid        UUID player uuid
     * @param prismPlayer PrismPlayer
     * @return PrismPlayer
     */

    private @Nullable PrismPlayer comparePlayerToCache(final String name, final UUID uuid,
                                                       PrismPlayer prismPlayer) {
        if (prismPlayer == null) {
            return null;
        }
        if (!name.equals(prismPlayer.getName())) {
            //ok but now names can be used so lets check if an existing player uses that name
            PrismPlayer test = playerIdHelper.lookupByName(name);
            if (test != null && test.getUuid() != prismPlayer.getUuid()) {
                PrismLogHandler.warn("Player UUID for " + name + " conflicts with another player: " + test.getUuid()
                        + " we are attempting to update that UUID with a new name before allowing this cache.");
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(test.getUuid());
                test.setName(offlinePlayer.getName());
                if (test.getName().equals(name)) {
                    PrismLogHandler.warn("Players appear to have the same name "
                            + "- generally this is impossible with online servers.");
                }
                playerIdHelper.updatePlayer(test);
            }
            prismPlayer.setName(name);
            playerIdHelper.updatePlayer(prismPlayer);
        }
        if (!uuid.equals(prismPlayer.getUuid())) {
            PrismLogHandler.warn("Player UUID for " + name + " does not match our cache! " + uuid
                    + " versus cache of " + prismPlayer.getName() + " / " + prismPlayer.getUuid());

            // Update anyway...
            prismPlayer.setUuid(uuid);
            playerIdHelper.updatePlayer(prismPlayer);
        }
        return prismPlayer;
    }

}