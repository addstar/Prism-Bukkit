package me.botsko.prism.serializers.entity;

import com.google.gson.annotations.SerializedName;
import me.botsko.prism.utils.EntityUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Tameable;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 18/11/2020.
 */
public class TameEntitySerializer<T extends Tameable> extends EntitySerializer<T> {

    @SerializedName(value = "tamingOwner", alternate = "taming_owner")
    protected String tamingOwner = null;

    @Override
    public void serialize(T entity) {
        super.serialize(entity);
        if (entity.getOwner() != null) {
            tamingOwner = entity.getOwner().getUniqueId().toString();
        } else if (entity.isTamed()) {
            tamingOwner = "-none-";
        }
    }

    @Override
    public void deserialize(T entity) {
        super.deserialize(entity);
        if (tamingOwner.equals("-none-")) {
            return;
        }
        entity.setOwner(EntityUtils.offlineOf(tamingOwner));

    }

    @Override
    public String toString() {
        String format = NAME_FORMAT;
        if (tamingOwner != null) {
            OfflinePlayer player = EntityUtils.offlineOf(tamingOwner);
            if (player != null) {
                format = format.replace("<owner>", player.getName() + "'s");
            }
        }
        return format(format);
    }
}
