package me.botsko.prism.utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Objects;

public class DeathUtils {

    /**
     * Returns the name of what caused an entity to die.
     *
     * @return String
     */
    public static String getCauseNiceName(Entity entity) {

        EntityDamageEvent e = entity.getLastDamageCause();

        if (e == null) {
            return "unknown";
        }

        // Determine the root cause
        DamageCause damageCause = e.getCause();
        Entity killer = null;

        // If was damaged by an entity
        if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent entityDamageByEntityEvent) {
            // Arrow?
            if (entityDamageByEntityEvent.getDamager() instanceof Arrow) {
                Projectile arrow = (Arrow) entityDamageByEntityEvent.getDamager();
                ProjectileSource source = arrow.getShooter();
                if (source instanceof Player) {
                    killer = ((Player) source);
                }
            } else {
                killer = entityDamageByEntityEvent.getDamager();
            }
        }

        if (entity instanceof Player player) {

            // Detect additional suicide. For example, when you potion
            // yourself with instant damage it doesn't show as suicide.
            if (killer instanceof Player) {
                // Themself
                if (killer.getName().equals(player.getName())) {
                    return "suicide";
                }
                // translate bukkit events to nicer names
                if ((damageCause.equals(DamageCause.ENTITY_ATTACK) || damageCause.equals(DamageCause.PROJECTILE))) {
                    return "pvp";
                }
            }
        }

        // Causes of death for either entities or players
        return switch (damageCause) {
            case ENTITY_ATTACK -> "mob";
            case PROJECTILE -> "skeleton";
            case ENTITY_EXPLOSION -> "creeper";
            case CONTACT -> "cactus";
            case BLOCK_EXPLOSION -> "tnt";
            case FIRE, FIRE_TICK -> "fire";
            case MAGIC -> "potion";
            default -> damageCause.name().toLowerCase();
        };
    }

    /**
     * Returns the name of the attacker, whether mob or player.
     *
     * @return String
     */
    public static String getAttackerName(Entity victim) {

        // Determine base cause
        String cause = getCauseNiceName(victim);

        if (victim instanceof Player) {
            Player killer = ((Player) victim).getKiller();
            if (killer != null) {
                return killer.getName();
            }
        }

        if (cause.equals("mob")) {

            Entity killer = ((EntityDamageByEntityEvent) Objects.requireNonNull(
                    victim.getLastDamageCause())).getDamager();
            return getEntityName(killer);
        }
        return cause;
    }

    private static String getEntityName(Entity entity) {
        if (entity instanceof Player) {
            return entity.getName();
        } else if (entity instanceof Skeleton) {
            if (entity instanceof WitherSkeleton) {
                return "witherskeleton";
            } else {
                return "skeleton";
            }
        } else if (entity instanceof Arrow) {
            return "skeleton";
        } else if (entity instanceof Wolf) {
            Tameable wolf = (Wolf) entity;
            if (wolf.isTamed()) {
                if (wolf.getOwner() instanceof Player || wolf.getOwner() instanceof OfflinePlayer) {
                    return "pvpwolf";
                } else {
                    return "wolf";
                }
            } else {
                return "wolf";
            }
        } else {
            return entity.getType().name().toLowerCase();
        }
    }

    /**
     * Returns the name of the attacker, whether mob or player.
     *
     * @return String.
     */
    @SuppressWarnings("unused")
    public static String getVictimName(Entity victim) {
        return getEntityName(victim);
    }

    /**
     * Determines the owner of a tamed wolf.
     *
     * @param event  EntityDeathEvent
     * @return String
     */
    public static String getTameWolfOwner(EntityDeathEvent event) {
        String owner = "";
        EntityDamageByEntityEvent e =
                ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause());
        if (e != null) {
            Entity killer = e.getDamager();
            if (killer instanceof Wolf wolf) {
                if (wolf.isTamed()) {
                    if (wolf.getOwner() instanceof Player) {
                        owner = ((Player) wolf.getOwner()).getName();
                    }
                    if (wolf.getOwner() instanceof OfflinePlayer) {
                        owner = wolf.getOwner().getName();
                    }
                }
            }
        }
        return owner;
    }

    /**
     * Determines the weapon used to kill an entity.
     *
     * @return String
     */
    @SuppressWarnings("unused")
    public static String getWeapon(LivingEntity entity) {
        String deathWeapon = "";
        if (entity.getKiller() != null) {
            ItemStack weapon = entity.getKiller().getInventory().getItemInMainHand();
            deathWeapon = weapon.getType().toString().toLowerCase();
            deathWeapon = deathWeapon.replaceAll("_", " ");
            if (deathWeapon.equalsIgnoreCase("air")) {
                deathWeapon = " hands";
            }
        }
        return deathWeapon;
    }
}