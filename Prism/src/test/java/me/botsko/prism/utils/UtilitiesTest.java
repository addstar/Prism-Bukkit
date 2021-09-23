package me.botsko.prism.utils;

import be.seeseemelk.mockbukkit.ServerMock;
import me.botsko.prism.Prism;
import me.botsko.prism.testhelpers.TestHelper;
import me.botsko.prism.utils.block.Utilities;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 23/05/2020.
 */
class UtilitiesTest {

    static boolean integrationTesting = false;
    private static ServerMock server;
    private static TestHelper helper;

    @BeforeAll
    static void setUpAll() {
        helper = new TestHelper();
        server = helper.setup();
        if (Prism.getInstance().getPrismDataSource().getDataSource() != null) {
            integrationTesting = true;
        }
    }

    @AfterAll
    static void tearDownFinal() {
        TestHelper.shutdownHelper(helper);
    }

    @Test
    public void testAreBlockIdsSameCoreItem() {
        Material m1 = Material.DIRT;
        Material m2 = Material.DIRT;
        assertTrue(Utilities.areBlockIdsSameCoreItem(m1,m2));
        m1 = Material.GRASS_BLOCK;
        assertTrue(Utilities.areBlockIdsSameCoreItem(m1,m2));
    }

}