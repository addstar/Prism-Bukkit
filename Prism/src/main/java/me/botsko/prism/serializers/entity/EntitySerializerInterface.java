package me.botsko.prism.serializers.entity;

import org.bukkit.entity.Entity;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 18/11/2020.
 */
public interface EntitySerializerInterface<T extends Entity> {

    void serialize(T entity);

    void deserialize(T entity);

    String customDesc();

    String getEntityName();
}
