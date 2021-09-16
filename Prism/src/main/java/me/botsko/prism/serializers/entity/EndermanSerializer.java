package me.botsko.prism.serializers.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Enderman;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 20/10/2020.
 */
public class EndermanSerializer extends EntitySerializer<Enderman> {

    private String blockData;

    @Override
    public void serialize(Enderman entity) {
        super.serialize(entity);
        if (entity.getCarriedBlock() != null) {
            blockData = entity.getCarriedBlock().toString();
        }
    }

    @Override
    public void deserialize(Enderman entity) {
        super.deserialize(entity);
        if (blockData != null) {
            entity.setCarriedBlock(Bukkit.createBlockData(blockData));
        }
    }
}
