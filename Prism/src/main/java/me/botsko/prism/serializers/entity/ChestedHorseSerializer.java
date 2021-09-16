package me.botsko.prism.serializers.entity;

import org.bukkit.entity.ChestedHorse;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 18/11/2020.
 */
public class ChestedHorseSerializer<T extends ChestedHorse> extends AbstractHorseSerializer<T> {

    protected Boolean chest = null;

    @Override
    public void serialize(T entity) {
        super.serialize(entity);
        chest = entity.isCarryingChest();

    }

    @Override
    public void deserialize(T entity) {
        super.deserialize(entity);
        entity.setCarryingChest(Boolean.TRUE.equals(chest));

    }

}
