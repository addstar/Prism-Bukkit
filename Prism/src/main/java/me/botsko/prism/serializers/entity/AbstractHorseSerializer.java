package me.botsko.prism.serializers.entity;

import me.botsko.prism.utils.ItemUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;

public class AbstractHorseSerializer<T extends AbstractHorse> extends EntitySerializer<T> {
    protected String saddle = null;
    protected int dom = 0;
    protected int maxDom = 20;
    protected double jump = 1.0;
    protected double maxHealth = 20.0;
    protected double movementSpeed = 0.2;

    /**
     * {@inheritDoc}
     */
    public void serialize(T entity) {
        super.serialize(entity);
        final AbstractHorse h = entity;
        saddle = ItemUtils.smallString(h.getInventory().getSaddle());
        dom = h.getDomestication();
        maxDom = h.getMaxDomestication();
        jump = h.getJumpStrength();
        maxHealth = h.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();

        AttributeInstance attributeInstance = h.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attributeInstance != null) {
            movementSpeed = attributeInstance.getBaseValue();
        }
    }

    @Override
    public void deserialize(T entity) {
        super.deserialize(entity);
        final AbstractHorse h = entity;
        h.getInventory().setSaddle(ItemUtils.itemOf(saddle));
        maxDom = Math.max(1, maxDom);
        dom = Math.min(Math.max(0, dom), maxDom);
        jump = Math.min(Math.max(0.0, jump), 2.0);
        h.setDomestication(dom);
        h.setMaxDomestication(maxDom);
        h.setJumpStrength(jump);
        h.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);

        AttributeInstance attributeInstance = h.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(movementSpeed);
        }
    }

    protected String getPrefix() {
        return super.getPrefix();
    }

}

