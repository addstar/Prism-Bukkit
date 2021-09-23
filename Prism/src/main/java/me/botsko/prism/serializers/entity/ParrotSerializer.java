package me.botsko.prism.serializers.entity;

import me.botsko.prism.utils.MiscUtils;
import org.bukkit.entity.Parrot;

public class ParrotSerializer extends TameEntitySerializer<Parrot> {
    protected String var = null;

    @Override
    public void serialize(Parrot entity) {
        var = entity.getVariant().name().toLowerCase();
    }

    @Override
    public void deserialize(Parrot entity) {
        entity.setVariant(MiscUtils.getEnum(var, Parrot.Variant.RED));
    }

    @Override
    protected String getPrefix() {
        return super.getPrefix() + MiscUtils.niceName(var);
    }
}
