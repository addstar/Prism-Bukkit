package me.botsko.prism.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EntityUtils {

    private static final HashMap<String, String> descriptionCache = new HashMap<>();

    /**
     * Get Offline player. Should run Async.
     * @param uuidOrName String
     * @return OfflinePlayer
     */
    public @Nullable static OfflinePlayer offlineOf(String uuidOrName) {
        if (uuidOrName != null) {
            OfflinePlayer result;
            try {
                result = Bukkit.getOfflinePlayer(UUID.fromString(uuidOrName));
            } catch (IllegalArgumentException e) {
                @SuppressWarnings("deprecation")
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuidOrName);
                result = player;
            }

            return result.hasPlayedBefore() ? result : null;
        }

        return null;
    }

    /**
     * Get Uuid - should be handled Async as this may do a remote call.
     * @param uuidOrName String
     * @return Uuid.
     */
    public static UUID uuidOf(String uuidOrName) {
        if (uuidOrName != null) {
            try {
                return UUID.fromString(uuidOrName);
            } catch (IllegalArgumentException ignored) {
                //ignored
            }

            @SuppressWarnings("deprecation")
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuidOrName);
            return player.getUniqueId();
        }

        return null;
    }

    /**
     * Projectile Description.
     * @param source Projectile
     * @return String.
     */
    public static String getCustomProjectileDescription(Projectile source) {
        String description = descriptionCache.get(source.getClass().getSimpleName());

        if (description == null) {
            if (source instanceof org.bukkit.entity.Trident) {
                description = "skewered";
            } else if (source instanceof org.bukkit.entity.Arrow) {
                description = "shot";
            } else if (source instanceof org.bukkit.entity.Egg) {
                description = "became the very best of";
            } else if (source instanceof org.bukkit.entity.EnderPearl) {
                description = "vwooped";
            } else if (source instanceof org.bukkit.entity.SmallFireball) {
                description = "ignited";
            } else if (source instanceof org.bukkit.entity.Fireball) {
                description = "exploded";
            } else if (source instanceof org.bukkit.entity.FishHook) {
                description = "hooked";
            } else if (source instanceof org.bukkit.entity.ThrownPotion) {
                description = "doused";
            } else if (source instanceof org.bukkit.entity.LlamaSpit) {
                description = "disrespected";
            } else if (source instanceof org.bukkit.entity.ShulkerBullet) {
                description = "ascended";
            } else if (source instanceof org.bukkit.entity.Snowball) {
                description = "iced";
            } else if (source instanceof org.bukkit.entity.ThrownExpBottle) {
                description = "taught";
            } else {
                description = "";
            }

            descriptionCache.put(source.getClass().getSimpleName(), description);
        }

        if (description.length() > 0) {
            return description;
        }

        return null;
    }

    public static void sendBlockChange(Player p, Location loc, BlockData data) {
        p.sendBlockChange(loc, data);
    }

    /**
     * Removes item drops near an entity.
     *
     * @param player Player
     * @param radius int
     * @return int number removed.
     */
    public static int removeNearbyItemDrops(Player player, int radius) {
        int removed = 0;
        List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
        for (Entity e : nearby) {
            if (e instanceof Item || e instanceof ExperienceOrb) {
                e.remove();
                removed++;
            }
        }
        return removed;
    }

    /**
     * Whether or not an entity is within a cube radius.
     *
     * @param loc1 Location
     * @param radius int
     * @param loc2 Location
     * @return in Cube.
     */
    public static boolean inCube(Location loc1, int radius, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return false;
        }
        return (loc1.getBlockX() + radius > loc2.getBlockX() && loc1.getBlockX() - radius < loc2.getBlockX()
                && loc1.getBlockY() + radius > loc2.getBlockY() && loc1.getBlockY() - radius < loc2.getBlockY()
                && loc1.getBlockZ() + radius > loc2.getBlockZ() && loc1.getBlockZ() - radius < loc2.getBlockZ());
    }

    /**
     * Determines which blocks a player my "co-exist" with.
     *
     * @param m Material
     * @return boolean
     * @todo doesn't bukkit have this already?
     */

    @SuppressWarnings("deprecation")
    public static boolean playerMayPassThrough(Material m) {
        // Close enough, pray you don't land in a portal
        return m.isTransparent();
    }
}