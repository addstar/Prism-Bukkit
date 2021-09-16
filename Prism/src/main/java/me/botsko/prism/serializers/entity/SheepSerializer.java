package me.botsko.prism.serializers.entity;

import me.botsko.prism.utils.MiscUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;

public class SheepSerializer extends EntitySerializer<Sheep> {

    public void setColor(DyeColor color) {
        this.color = color.name().toLowerCase();
    }

    protected String color = null;

    @Override
    public void serialize(Sheep entity) {
        if (entity.getColor() != null) {
            color = entity.getColor().name().toLowerCase();
        } else {
            color = DyeColor.WHITE.name().toLowerCase();
        }
    }

    @Override
    public void deserialize(Sheep entity) {
        entity.setColor(MiscUtils.getEnum(color, DyeColor.WHITE));
    }

    @Override
    protected String getPrefix() {
        return super.getPrefix() + MiscUtils.niceName(color);
    }
}

