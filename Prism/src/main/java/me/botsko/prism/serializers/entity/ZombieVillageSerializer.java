package me.botsko.prism.serializers.entity;

import me.botsko.prism.utils.MiscUtils;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.ZombieVillager;

public class ZombieVillageSerializer extends EntitySerializer<ZombieVillager> {
    protected String profession = null;

    @Override
    public void serialize(ZombieVillager entity) {
        super.serialize(entity);
        if (entity.getVillagerProfession() != null) {
            profession = entity.getVillagerProfession().name().toLowerCase();
        }
    }

    @Override
    public void deserialize(ZombieVillager entity) {
        super.deserialize(entity);
        entity.setVillagerProfession(MiscUtils.getEnum(profession, Profession.class));
    }

    protected String getSuffix() {
        return "(" + MiscUtils.niceName(profession) + ")";
    }
}
