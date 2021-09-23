package me.botsko.prism.serializers.entity;

import me.botsko.prism.utils.MiscUtils;
import org.bukkit.entity.Cat;

public class CatSerializer extends TameEntitySerializer<Cat> {

    protected String var = null;

    @Override
    public void serialize(Cat entity) {
        super.serialize(entity);
        var = entity.getCatType().name().toLowerCase();
    }

    @Override
    public void deserialize(Cat entity) {
        super.deserialize(entity);
        Cat.Type type = MiscUtils.getEnum(var, Cat.Type.ALL_BLACK);
        entity.setCatType(type);
    }

    public String getPrefix() {
        return MiscUtils.niceName(var);
    }
}

