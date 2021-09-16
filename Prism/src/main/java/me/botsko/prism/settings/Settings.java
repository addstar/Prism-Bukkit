package me.botsko.prism.settings;

import me.botsko.prism.database.PrismDataSource;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class Settings {

    private static PrismDataSource dataSource;


    /**
     * Delete the setting.
     *
     * @param key String
     */
    public static CompletableFuture<Boolean> deleteSettingAsync(String key) {
        return  CompletableFuture.supplyAsync(() -> deleteSetting(key, null));
    }
    
    /**
     * Delete the setting.
     *
     * @param key String
     */
    public static CompletableFuture<Boolean> deleteSettingAsync(String key,Player player) {
        return  CompletableFuture.supplyAsync(() -> deleteSetting(key, player));
    }

    /**
     * Delete the setting.
     * @param key String
     * @param player Player
     * @deprecated Use {@link Settings#deleteSettingAsync(String, Player)}
     */
    public static boolean deleteSetting(String key, Player player) {
        return dataSource.createSettingsQuery().deleteSetting(key, player);
    }

    /**
     * Save the setting.
     * @param key String
     * @param value String.
     */
    public static CompletableFuture<Boolean> saveSettingAsync(String key, String value) {
        return saveSettingAsync(key,value,null);
    }

    /**
     * Save the setting.
     * @param key String
     * @param value String.
     */
    public static CompletableFuture<Boolean> saveSettingAsync(String key, String value, Player player) {
        return CompletableFuture.supplyAsync(() -> saveSetting(key, value, player));
    }

    /**
     * Save. Run Async.
     * @param key String
     * @param value String
     * @param player Player
     */
    private static boolean saveSetting(String key, String value, Player player) {
        if (dataSource == null) {
            throw new NullPointerException("You did not define a Settings Datasource.");
        }
        return dataSource.createSettingsQuery().saveSetting(key, value, player);
    }

    /**
     * Get.
     * @param key String
     * @return String
     */
    public static String getSetting(String key) {
        return getSetting(key, null);
    }

    /**
     * Get.
     * @param key String
     * @param player Player
     * @return String
     */
    public static String getSetting(String key, Player player) {
        return dataSource.createSettingsQuery().getSetting(key, player);
    }

    public static void setDataSource(PrismDataSource dataSource) {
        Settings.dataSource = dataSource;
    }
}