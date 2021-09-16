package me.botsko.prism.testhelpers;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bstats.bukkit.Metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 22/11/2020.
 */
public class TestHelper {

    public static void shutdownHelper(TestHelper helper) {
        helper.shutdown();
    }

    /**
     * Setup for mocking.
     * @return ServerMock.
     */
    public ServerMock setup() {
        ServerMock server = MockBukkit.getOrCreateMock();
        server.addSimpleWorld("Normal");
        Metrics metrics = null;
        server.getScheduler().performTicks(20);
        return server;
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        Path path = null;
        try {
            MockBukkit.unmock();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (path != null) {
            cleanFileSystem(path);
        }

    }

    private void cleanFileSystem(Path path) {
        try {
            Files.list(path).forEach(path1 -> {
                try {
                    Files.delete(path1);
                    System.out.println("Deleting " + path1);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            });
            Files.delete(path);
            System.out.println("Deleting " + path);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
