package me.botsko.prism.listeners;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.api.actions.ActionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.util.List;
import java.util.UUID;

public class PrismVehicleEvents implements Listener {

    private final Prism plugin;

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public PrismVehicleEvents(Prism plugin) {
        this.plugin = plugin;
    }

    /**
     * VehicleCreateEvent.
     *
     * @param event VehicleCreateEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleCreate(final VehicleCreateEvent event) {

        final Vehicle vehicle = event.getVehicle();
        final Location loc = vehicle.getLocation();

        final String coord_key = loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
        String value = plugin.preplannedVehiclePlacement.get(coord_key);
        UUID uuid = null;
        try {
            uuid = UUID.fromString(value);
        } catch (Exception ignored) {
            //ignored.
        }
        final OfflinePlayer player = uuid != null ? Bukkit.getOfflinePlayer(uuid) : null;
        if (player != null) {
            // TODO: name ref
            if (!Prism.getIgnore().event(ActionType.VEHICLE_PLACE, loc.getWorld(), player.getUniqueId())) {
                return;
            }
            RecordingQueue.addToQueue(ActionFactory.createVehicle(ActionType.VEHICLE_PLACE, vehicle, player));
        }
    }

    /**
     * VehicleDestroyEvent.
     *
     * @param event VehicleDestroyEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent event) {

        final Vehicle vehicle = event.getVehicle();
        final Entity attacker = event.getAttacker();
        // Was it broken by an attack
        if (attacker != null) {
            handlePlayerAction(attacker, vehicle, ActionType.VEHICLE_BREAK);
        } else {

            // Otherwise its driver was reckless
            final List<Entity> passengers = vehicle.getPassengers();
            if (!passengers.isEmpty()) {
                Entity passenger = passengers.get(0);
                handlePlayerAction(passenger, vehicle, ActionType.VEHICLE_BREAK);
            }
        }
    }

    /**
     * VehicleEnterEvent.
     *
     * @param event VehicleEnterEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event) {
        final Vehicle vehicle = event.getVehicle();
        final Entity entity = event.getEntered();
        handlePlayerAction(entity, vehicle, ActionType.VEHICLE_ENTER);
    }

    /**
     * VehicleExitEvent.
     *
     * @param event VehicleExitEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(final VehicleExitEvent event) {

        final Vehicle vehicle = event.getVehicle();
        final Entity entity = event.getExited();
        handlePlayerAction(entity, vehicle, ActionType.VEHICLE_EXIT, ActionType.VEHICLE_ENTER);
    }

    private void handlePlayerAction(Entity entity, Vehicle vehicle, ActionType action) {
        handlePlayerAction(entity, vehicle, action, action);
    }

    private void handlePlayerAction(Entity entity, Vehicle vehicle, ActionType action, ActionType customCheck) {
        if (entity instanceof Player) {
            if (!Prism.getIgnore().event(customCheck, ((Player) entity))) {
                return;
            }
            RecordingQueue.addToQueue(ActionFactory.createVehicle(action, vehicle, (Player) entity));
        } else {
            if (!Prism.getIgnore().event(customCheck, entity.getWorld())) {
                return;
            }
            RecordingQueue.addToQueue(ActionFactory.createVehicle(action, vehicle,
                    entity.getType().name().toLowerCase()));
        }
    }
}