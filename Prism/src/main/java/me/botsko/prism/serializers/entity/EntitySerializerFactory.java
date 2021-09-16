package me.botsko.prism.serializers.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;

public class EntitySerializerFactory {
    private static EntitySerializerFactory factory = null;
    private final EnumMap<EntityType, Class<? extends EntitySerializerInterface<? extends Entity>>> entitySerializers =
            new EnumMap<>(EntityType.class);

    private EntitySerializerFactory() {
        entitySerializers.put(EntityType.HORSE, HorseSerializer.class);
        entitySerializers.put(EntityType.LLAMA, LlamaSerializer.class);
        entitySerializers.put(EntityType.MULE, MuleSerializer.class);
        entitySerializers.put(EntityType.DONKEY, DonkeySerializer.class);
        entitySerializers.put(EntityType.ZOMBIE_HORSE, ZombieHorseSerializer.class);
        entitySerializers.put(EntityType.SKELETON_HORSE, SkeletonHorseSerializer.class);


        entitySerializers.put(EntityType.CAT, CatSerializer.class);
        entitySerializers.put(EntityType.PARROT, ParrotSerializer.class);
        entitySerializers.put(EntityType.SHEEP, SheepSerializer.class);
        entitySerializers.put(EntityType.WANDERING_TRADER, WanderingTraderSerializer.class);
        entitySerializers.put(EntityType.VILLAGER, VillagerSerializer.class);

        entitySerializers.put(EntityType.WOLF, WolfSerializer.class);
        entitySerializers.put(EntityType.ZOMBIE_VILLAGER, ZombieVillageSerializer.class);
        entitySerializers.put(EntityType.PANDA, PandaSerializer.class);
        entitySerializers.put(EntityType.ENDERMAN, EndermanSerializer.class);

        /*
         * TODO: Creeper charge, magma cube size, shulker color, slime
         * size, rabbit color
         */
    }

    static EntitySerializerFactory get() {
        if (factory != null) {
            return factory;
        }

        return (factory = new EntitySerializerFactory());
    }

    public static Class<? extends EntitySerializerInterface<? extends Entity>> getSerializingClass(EntityType type) {
        return get().entitySerializers.getOrDefault(type, DefaultEntitySerializer.class);
    }

    /**
     * Get a serializer class for entity.
     * @param type EntityType
     * @return EntitySerializer
     */
    public static EntitySerializerInterface<?> getSerializer(Entity type) {
        Class<? extends EntitySerializerInterface<? extends Entity>> clazz = getSerializingClass(type.getType());
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("No default constructor found for " + clazz.getName());
        }
    }
}
