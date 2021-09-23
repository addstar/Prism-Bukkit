package me.botsko.prism.listeners;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import me.botsko.prism.Prism;
import me.botsko.prism.testhelpers.TestHelper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 19/11/2020.
 */
class PrismEntityEventsTest {

    static ServerMock server;
    static boolean integrationTesting = false;
    private static TestHelper helper;

    @BeforeAll
    static void setUpAll() {
        helper = new TestHelper();
        server = helper.setup();
        if (Prism.getInstance().getPrismDataSource().getDataSource() != null) {
            integrationTesting = true;
        }
    }

    @BeforeEach
    void setUp() {
        Prism.setDebug(true);
        server.clearRecipes();
        server.getPluginManager().clearEvents();
        server.getScheduler().performOneTick();
    }

    @AfterAll
    static void tearDownFinal() {
        TestHelper.shutdownHelper(helper);
    }

    @Test
    void onCreatureSpawn() {
        WorldMock world = (WorldMock) server.getWorld("Normal");
        server.addPlayer("Mock");
        assertNotNull(world);
        Prism.setDebug(true);
        Zombie z = (Zombie) world.spawnEntity(world.getSpawnLocation(),EntityType.ZOMBIE);
        server.getPluginManager().callEvent(new CreatureSpawnEvent(z, CreatureSpawnEvent.SpawnReason.NATURAL));
        server.getScheduler().performTicks(100);
        MockBukkit.getMock().getPluginManager().assertEventFired(event -> event instanceof CreatureSpawnEvent);
    }
}