package me.botsko.prism.players;

import org.bukkit.entity.Player;

import java.util.UUID;

public class PrismPlayer {

    private int playerId;
    private String player;
    private UUID playerUuid;

    /**
     * Constructor.
     * @param playerId int
     * @param player Player
     */
    @SuppressWarnings("unused")
    public PrismPlayer(int playerId, Player player) {
        this(playerId, player.getUniqueId(), player.getName());
    }

    /**
     * Constructor.
     * @param playerId int
     * @param playerUuid Uuid
     * @param player Player
     */
    public PrismPlayer(int playerId, UUID playerUuid, String player) {
        this.playerId = playerId;
        this.playerUuid = playerUuid;
        this.player = player;
    }

    /**
     * Get id.
     * @return id
     */
    public int getId() {
        return playerId;
    }

    /**
     * Set id.
     */
    public void setId(int newId) {
        if (playerId > 0) {
            throw new IllegalArgumentException("Cannot overwrite PrismPlayer primary key.");
        }
        playerId = newId;
    }

    /**
     * Get Name.
     * @return String
     */
    public String getName() {
        return player;
    }

    /**
     * Set name.
     */
    public void setName(String name) {
        player = name;
    }

    /**
     * Get Uuid.
     *
     * @return Uuid
     */
    public UUID getUuid() {
        return playerUuid;
    }

    /**
     * Set Uuid.
     */
    public void setUuid(UUID uuid) {
        playerUuid = uuid;
    }
}