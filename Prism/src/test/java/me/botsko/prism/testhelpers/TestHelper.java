package me.botsko.prism.testhelpers;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 22/11/2020.
 */
public class TestHelper {

    private PrismTestPlugin plugin;

    public static boolean isEnabled(TestHelper helper) {
        return helper.plugin.isEnabled();
    }

    public static void shutdownHelper(TestHelper helper) {
        helper.shutdown();
    }

    /**
     * Setup for mocking.
     * @return ServerMock.
     */
    public ServerMock setup() {
        //System.out.println("Loading Server");
        ServerMock server = MockBukkit.getOrCreateMock();
        server.addSimpleWorld("Normal");
        @SuppressWarnings("unused")
        Metrics metrics = null;
        //System.out.println("Loading Test Plugin");
        File pluginDescriptorFile = new File("src/test/resources/plugin.yml");
        try {
            FileReader reader = new FileReader(pluginDescriptorFile);
            PluginDescriptionFile file = new PluginDescriptionFile(reader);
            plugin = MockBukkit.loadWith(PrismTestPlugin.class,file);
            //System.out.println("--- Loaded ---");
            server.getScheduler().performTicks(20);
            //System.out.println("Ticked 20");
        } catch (FileNotFoundException | InvalidDescriptionException e) {
            e.printStackTrace();
        }
        return server;
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        Path path = null;
        if (plugin != null) {
            path = plugin.getDataFolder().toPath();
            plugin.onDisable();
        }
       /* try {
        MockBukkit.unmock();
       } catch (Exception e) {
            System.out.println(e.getMessage());
        }*/
        if (path != null) {
            cleanFileSystem(path);
        }

    }

    private void cleanFileSystem(Path path) {
        try {
            Files.list(path).forEach(path1 -> {
                try {
                    Files.delete(path1);
                    //System.out.println("Deleting " + path1.toString());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            });
            Files.delete(path);
            //System.out.println("Deleting " + path.toString());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
