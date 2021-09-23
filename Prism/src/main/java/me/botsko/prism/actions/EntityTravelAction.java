package me.botsko.prism.actions;

import me.botsko.prism.serializers.SerializationHelper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class EntityTravelAction extends GenericAction {
    protected EntityTravelActionData actionData;

    public EntityTravelAction() {
        actionData = new EntityTravelActionData();
    }

    /**
     * Set entity.
     * @param entity Entity.
     */
    public void setEntity(Entity entity) {
        if (entity != null) {
            if (entity instanceof Player) {
                setPlayer((Player) entity);
            } else {
                setSourceName(entity.getType().name().toLowerCase());
            }
        }
    }

    /**
     * Set Location .
     * @param to Location.
     */
    public void setToLocation(Location to) {
        if (to != null) {
            actionData.x = to.getBlockX();
            actionData.y = to.getBlockY();
            actionData.z = to.getBlockZ();
        }
    }

    /**
     * Set cause.
     * @param cause TeleportCause
     */
    public void setCause(TeleportCause cause) {
        if (cause != null) {
            actionData.cause = cause.name().toLowerCase();
        }
    }

    @Override
    public boolean hasExtraData() {
        return actionData != null;
    }

    @Override
    public String serialize() {
        return SerializationHelper.gson().toJson(actionData);
    }

    @Override
    public void deserialize(String data) {
        if (data != null && data.startsWith("{")) {
            actionData = SerializationHelper.gson().fromJson(data, EntityTravelActionData.class);
        }
    }

    /**
     * Set TravelActionData.
     * @return EntityTravelActionData
     */
    @SuppressWarnings("unused")
    public EntityTravelActionData getActionData() {
        return actionData;
    }

    /**
     * Get nice name.
     * @return String
     */
    @Override
    public String getNiceName() {
        if (actionData != null) {
            final String cause = (actionData.cause == null ? "unknown" : actionData.cause.replace("_", " "));
            return "using " + cause + " to " + actionData.x + " " + actionData.y + " " + actionData.z;
        }
        return "teleported somewhere";
    }

    public static class EntityTravelActionData {
        int x;
        int y;
        int z;
        String cause;
    }
}