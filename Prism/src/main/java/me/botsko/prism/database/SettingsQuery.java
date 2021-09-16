package me.botsko.prism.database;

import org.bukkit.entity.Player;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/04/2019.
 */
public interface SettingsQuery {

    /**
     * False if fails.
     * @param key Key
     * @param player player
     * @return boolean.
     */
    boolean deleteSetting(String key, Player player);

    /**
     * Saves.
     * @param key Key
     * @param value String
     * @param player Player
     * @return false if fails to save
     */
    boolean saveSetting(String key, String value, Player player);

    /**
     * Returns {@code null} if the setting does not exist.
     * @param key Key
     * @param player Player
     * @return value of the setting as a String
     */
    String getSetting(String key, Player player);
}
