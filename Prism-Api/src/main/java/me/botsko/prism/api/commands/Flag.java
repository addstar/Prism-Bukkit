package me.botsko.prism.api.commands;

import org.bukkit.permissions.Permissible;

public interface Flag {

    String getDescription();

    /**
     * Get usage for flag.
     * @return String
     */
    String getUsage();

    String getPermission();

    boolean hasPermission(Permissible permissible);

}