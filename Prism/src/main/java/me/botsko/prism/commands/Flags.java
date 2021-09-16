package me.botsko.prism.commands;

import me.botsko.prism.api.commands.Flag;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.PropertyKey;

public enum Flags implements Flag {
    DRAIN("flag-drain"),
    DRAIN_LAVA("flag-drain-lava"),
    DRAIN_WATER("flag-drain-water"),
    EXTENDED("flag-extended"),
    NO_EXT("flag-no-extinguish"),
    NO_ITEMCLEAR("flag-no-item-clear"),
    PER_PAGE("-per-page=#", "flag-result-per-page"),
    NO_GROUP("flag-no-group"),
    OVERWRITE("flag-overwrite"),
    SHARE("-share=player1[,player2...]", "flag-share-result"),
    PASTE("flag-paste"),
    NO_PHYS("flag-no-physics");

    private final String description;
    private final String permission;
    private String usage;

    /**
     * Defaults {@link #usage} to -(flagname).
     *
     * @param description string - generally this is a property Key found in Prism library.
     */
    Flags(@PropertyKey(resourceBundle = "languages.message") String description) {
        this("", description);
    }

    Flags(String usage,@PropertyKey(resourceBundle = "languages.message") String description) {
        this.usage = usage;
        this.description = description;
        this.permission = "prism.parameters.flag." + name().toLowerCase().replace('_', '-');
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get usage for flag.
     * @return String
     */
    public String getUsage() {
        if (usage.isEmpty()) {
            usage = "-" + this.name().toLowerCase();
        }
        return usage;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission(Permissible permissible) {
        return permissible.hasPermission(permission);
    }
}
