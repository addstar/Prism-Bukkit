package me.botsko.prism.serializers.entity;

import me.botsko.prism.testhelpers.TestHelper;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 18/11/2020.
 */
class EntitySerializerFactoryTest {
    /**
     * Required to avoid NPE.
     */
    private static TestHelper helper;

    @BeforeEach
    public void setUp() {
        helper = new TestHelper();
        //ServerMock server = helper.setup();
    }

    @Test
    public void constructorTest() {
        EntitySerializerFactory factory = EntitySerializerFactory.get();
        assertEquals(EntitySerializerFactory.getSerializingClass(EntityType.HORSE),HorseSerializer.class);
    }

    @AfterAll
    public static void tearDownFinal() {
        TestHelper.shutdownHelper(helper);
    }

}