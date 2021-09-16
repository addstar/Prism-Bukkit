package me.botsko.prism.serializers.entity;

import me.botsko.prism.utils.MiscUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;

public class WolfSerializer extends TameEntitySerializer<Wolf> {
    protected String color = null;

    @Override
    public void serialize(Wolf entity) {
        super.serialize(entity);
        color = entity.getCollarColor().name().toLowerCase();
    }

    @Override
    public void deserialize(Wolf entity) {
        super.serialize(entity);
        entity.setCollarColor(MiscUtils.getEnum(color, DyeColor.RED));
    }

    @Override
    protected String getPrefix() {
        return MiscUtils.niceName(color);
    }
}
