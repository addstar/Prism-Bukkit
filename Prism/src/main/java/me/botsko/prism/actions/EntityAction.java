package me.botsko.prism.actions;

import com.google.gson.JsonObject;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.api.ChangeResult;
import me.botsko.prism.api.ChangeResultType;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.appliers.ChangeResultImpl;
import me.botsko.prism.serializers.SerializationHelper;
import me.botsko.prism.serializers.entity.EntitySerializerFactory;
import me.botsko.prism.serializers.entity.EntitySerializerInterface;
import me.botsko.prism.serializers.entity.SheepSerializer;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class EntityAction extends GenericAction {

    private EntitySerializerInterface serializer;

    /**
     * Constructor.
     * @param name String
     * @return EntityType
     */
    @Nullable
    public static EntityType getEntityType(String name) {
        try {
            return EntityType.valueOf(name.toUpperCase());
        } catch (final IllegalArgumentException e) {
            // In pre-RC builds we logged the wrong name of entities, sometimes
            // the names
            // don't match the enum.
        }
        return null;
    }

    /**
     * Set the entity.
     * @param entity Entity
     * @param dyeUsed String
     */
    public void setEntity(Entity entity, DyeColor dyeUsed) {

        // Build an object for the specific details of this action
        if (entity != null) {
            entity.getType();
            entity.getType();
            setLoc(entity.getLocation());
            serializer = EntitySerializerFactory.getSerializer(entity);
            serializer.serialize(entity);
            if (dyeUsed != null) {
                if (serializer instanceof SheepSerializer) {
                    ((SheepSerializer) serializer).setColor(dyeUsed);
                }
            }
        }
    }

    @Override
    public String getCustomDesc() {
        if (serializer != null) {
            return serializer.customDesc();
        }

        return null;
    }

    @Override
    public boolean hasExtraData() {
        return serializer != null;
    }

    @Override
    public String serialize() {
        return SerializationHelper.gson().toJson(serializer);
    }

    @Override
    public void deserialize(String data) {
        if (data != null && data.startsWith("{")) {
            String entityName = SerializationHelper.gson()
                    .fromJson(data, JsonObject.class).get("entityName").getAsString();
            serializer = SerializationHelper.gson()
                    .fromJson(data, EntitySerializerFactory.getSerializingClass(getEntityType(entityName)));
        }
    }

    /**
     * Get nice name.
     * @return String
     */
    @Override
    public String getNiceName() {
        if (serializer != null) {
            return serializer.toString();
        }

        return "unknown";
    }

    @Override
    public ChangeResult applyRollback(Player player, PrismParameters parameters, boolean isPreview) {
        if (serializer == null) {
            return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
        }

        EntityType entityType = getEntityType(serializer.getEntityName());
        if (entityType != null && !Prism.getIllegalEntities().contains(entityType)) {
            if (!isPreview) {
                final Location loc = getLoc().add(0.5, 0.0, 0.5);
                if (entityType.getEntityClass() != null && loc.getWorld() != null) {
                    PrismLogHandler.debug("Spawning on Rollback: " + SerializationHelper.gson().toJson(serializer));
                    loc.getWorld().spawn(loc, entityType.getEntityClass(), entity -> serializer.deserialize(entity));
                    //todo this doesnt work for some reason in terms of applying serializations on spawn....
                    // Villagers dont seem to appear as per professions would require.
                } else {
                    return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
                }
                return new ChangeResultImpl(ChangeResultType.APPLIED, null);

            } else {
                return new ChangeResultImpl(ChangeResultType.PLANNED, null);
            }
        } else {
            return new ChangeResultImpl(ChangeResultType.SKIPPED, null);
        }
    }
}
