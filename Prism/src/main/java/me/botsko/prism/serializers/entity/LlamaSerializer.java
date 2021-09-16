package me.botsko.prism.serializers.entity;

import com.google.gson.annotations.SerializedName;
import me.botsko.prism.serializers.items.ItemStackSerializer;
import me.botsko.prism.utils.MiscUtils;
import org.bukkit.entity.Llama;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 18/11/2020.
 */
public class LlamaSerializer extends ChestedHorseSerializer<Llama> {
    @SerializedName(value = "color", alternate = "horseColor")
    protected String color = null;

    protected ItemStackSerializer decor;

    @Override
    public void serialize(Llama entity) {
        super.serialize(entity);
        color = entity.getColor().name();
        decor = ItemStackSerializer.createItemStackSerialized(entity.getInventory().getDecor());
    }

    @Override
    public void deserialize(Llama entity) {
        super.deserialize(entity);
        Llama.Color lColor = MiscUtils.getEnum(color, Llama.Color.CREAMY);
        entity.setColor(lColor);
        entity.getInventory().setDecor(decor.toBukkit());
    }

    @Override
    protected String getPrefix() {
        return super.getPrefix();
    }
}
