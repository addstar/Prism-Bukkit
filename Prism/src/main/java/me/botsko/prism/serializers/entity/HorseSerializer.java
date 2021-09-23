package me.botsko.prism.serializers.entity;

import me.botsko.prism.serializers.items.ItemStackSerializer;
import me.botsko.prism.utils.MiscUtils;
import org.bukkit.entity.Horse;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 18/11/2020.
 */
public class HorseSerializer extends AbstractHorseSerializer<Horse>  {

    protected String horseColor = null;
    protected String style = null;
    protected ItemStackSerializer armor = null;

    @Override
    public void serialize(Horse horse) {
        super.serialize(horse);
        horseColor = horse.getColor().name();
        style = horse.getStyle().name();
        armor = ItemStackSerializer.createItemStackSerialized(horse.getInventory().getArmor());
    }

    @Override
    public void deserialize(Horse horse) {
        super.deserialize(horse);
        Horse.Color color = MiscUtils.getEnum(horseColor, Horse.Color.WHITE);
        Horse.Style vStyle = MiscUtils.getEnum(style, Horse.Style.NONE);
        horse.setColor(color);
        horse.setStyle(vStyle);
        horse.getInventory().setArmor(armor.toBukkit());
    }

    @Override
    protected String getPrefix() {
        return super.getPrefix() + " " + style;
    }
}
