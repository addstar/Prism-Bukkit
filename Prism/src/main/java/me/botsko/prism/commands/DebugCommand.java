package me.botsko.prism.commands;

import com.zaxxer.hikari.HikariDataSource;
import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.SubHandler;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.measurement.QueueStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.kitteh.pastegg.PasteBuilder;
import org.kitteh.pastegg.PasteContent;
import org.kitteh.pastegg.PasteFile;
import org.kitteh.pastegg.Visibility;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by narimm on 3/03/2020.
 */
public class DebugCommand implements SubHandler {

    @Override
    public void handle(CallInfo call) {
        if (call.getArgs().length == 2) {
            String arg = call.getArg(1);
            switch (arg.toLowerCase()) {
                case "on" -> Prism.setDebug(true);
                case "off" -> Prism.setDebug(false);
                case "toggle" -> Prism.setDebug(!Prism.isDebug());
                default -> Prism.messenger.sendMessage(call.getSender(), Il8nHelper.getMessage("debug-help"));
            }
            Prism.messenger.sendMessage(call.getSender(), Prism.messenger.playerMsg(
                    Component.text(Il8nHelper.getRawMessage("debug-msg") + " " + Prism.isDebug())));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Prism.getInstance(), () -> createPaste(
                call.getSender()));
    }

    private String getFile(Path file) {
        try {
            String out = Files.readString(file,StandardCharsets.UTF_8);
            out = removePatterns("hostname: (.*)",out,"**secret.host**");
            out = removePatterns("username: (.*)",out,"**username**");
            return removePatterns("password: (.*)",out,"**password**");
        } catch (IOException e) {
            return ExceptionUtils.getFullStackTrace(e);
        }

    }

    private String removePatterns(String patten, String haystack, String replacement) {
        Pattern p = Pattern.compile(patten);
        Matcher matcher = p.matcher(haystack);
        while (matcher.find()) {
             haystack =  new StringBuilder(haystack)
                   .replace(matcher.start(1),matcher.end(1),replacement)
                   .toString();
        }
        return haystack;
    }

    private String getMainInfo() {
        StringBuilder mainInfo = new StringBuilder();
        mainInfo.append(Bukkit.getName()).append(" version: ").append(Bukkit.getServer()
                .getVersion()).append(System.lineSeparator());
        mainInfo.append("Plugin version: ").append(Prism.getInstance().getDescription()
                .getVersion()).append(System.lineSeparator());
        mainInfo.append("Java version: ").append(System.getProperty("java.version")).append('\n');
        mainInfo.append(System.lineSeparator());
        mainInfo.append("Plugins:").append(System.lineSeparator());
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            mainInfo.append(' ').append(plugin.getName()).append(" - ").append(
                    plugin.getDescription().getVersion()).append(System.lineSeparator());
            mainInfo.append("  ").append(plugin.getDescription().getAuthors()).append(System.lineSeparator());
        }
        return mainInfo.toString();
    }

    private String getDataSourceInfo() {
        PrismDataSource dataSource = Prism.getInstance().getPrismDataSource();
        StringBuilder out = new StringBuilder();
        String name = dataSource.getClass().getName();
        out.append("DataSource Name: ").append(name).append(System.lineSeparator());
        if (dataSource.getDataSource() instanceof HikariDataSource ds) {
            out.append("Running: ").append(ds.isRunning())
                    .append("Total Connections: ")
                    .append(ds.getHikariPoolMXBean().getTotalConnections())
                    .append(System.lineSeparator())
                    .append("Total Connections: ")
                    .append(ds.getHikariPoolMXBean().getActiveConnections())
                    .append(System.lineSeparator());
        }
        out.append("Illegal Blocks:").append(System.lineSeparator());
        for (Material mat : Prism.getIllegalBlocks()) {
            out.append("   ").append(mat.name()).append(System.lineSeparator());
        }
        out.append("Worlds Tracked: ").append(Prism.prismWorlds.size()).append(System.lineSeparator());
        out.append("Players Tracked: ").append(Prism.getPrismPlayers().size()).append(System.lineSeparator());
        out.append("Players with Tools: ").append(Prism.playersWithActiveTools.size())
                .append(System.lineSeparator());
        out.append("Recording Queue: ").append(RecordingQueue.getQueueSize()).append(System.lineSeparator());
        out.append("Queue Stats: Insert Av /min: ").append(QueueStats.getPerMinuteInsertAverage())
                .append(System.lineSeparator());
        out.append("             Build Av /min: ").append(QueueStats.getPerMinuteBatchBuildAverage())
                .append(System.lineSeparator());
        out.append("             Process Av /min: ").append(QueueStats.getPerMinuteBatchProcessAverage())
                .append(System.lineSeparator());
        out.append("Recent Runs: ").append(System.lineSeparator());
        Map<Long, QueueStats.TaskRunInfo> runs = Prism.getInstance().queueStats.getRecentRunCounts();
        for (final Map.Entry<Long, QueueStats.TaskRunInfo> entry : runs.entrySet()) {
            final String time = new SimpleDateFormat("HH:mm:ss").format(entry.getKey());
            out.append("  ").append(time).append(" ")
                    .append(entry.getValue().getRecords()).append(System.lineSeparator());
        }
        return out.toString();
    }


    private void createPaste(CommandSender sender) {
        Path dataPath = Prism.getInstance().getDataFolder().toPath();
        Path prismConfig = dataPath.resolve("config.yml");
        Path hikariProps = dataPath.resolve("hikari.properties");
        List<String> logs = new ArrayList<>();
        try {
            Files.find(dataPath, 1, (path, basicFileAttributes) ->
                            path.toFile().getName().matches("prism.log(\\.?)(\\d*)"))
                    .forEach(path -> {
                        String pLog;
                        if (path.toFile().length() < 2000000L) {
                            pLog = getFile(path);
                        } else {
                            pLog = "TRUNCATED DUE TO LARGE SIZE: you may need to manually paste. <pluginDir>/prism.log";
                        }
                        logs.add(pLog);
                    });
        } catch (IOException e) {
            PrismLogHandler.warn("IOException accessing logs",e);
        }
        PasteBuilder pasteBuilder = new PasteBuilder().name("Prism Debug Output")
                .visibility(Visibility.UNLISTED)
                .setApiKey(Prism.getPasteKey())
                .expires(ZonedDateTime.now().plusDays(1)) //1 day
                .addFile(new PasteFile("Main Info",
                        new PasteContent(PasteContent.ContentType.TEXT, getMainInfo())))
                .addFile(new PasteFile("config.yml",
                        new PasteContent(PasteContent.ContentType.TEXT, getFile(prismConfig))))
                .addFile(new PasteFile("hikari.properties",
                        new PasteContent(PasteContent.ContentType.TEXT, getFile(hikariProps))))
                .addFile(new PasteFile("dataSource Properties",
                        new PasteContent(PasteContent.ContentType.TEXT, getDataSourceInfo())));
        int i = 1;
        for (String log : logs) {
            pasteBuilder.addFile(new PasteFile("Prism Log " + i, new PasteContent(PasteContent.ContentType.TEXT, log)));
            i++;
        }
        PasteBuilder.PasteResult result = pasteBuilder.build();
        if (result.getPaste().isPresent()) {
            String pasteUrl = "https://paste.gg/" + result.getPaste().get().getId();
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerMsg(Il8nHelper.getMessage("paste-output")
                            .replaceText(builder -> builder.match("<pasteUrl>")
                                    .replacement(Component.text(pasteUrl).clickEvent(ClickEvent.openUrl(pasteUrl))))
                    )
            );
            PrismLogHandler.log("Paste Created : " + pasteUrl);
            result.getPaste().get().getDeletionKey().ifPresent(
                  s -> {
                      Prism.messenger.sendMessage(sender, Prism.messenger.playerMsg(Il8nHelper.getMessage("delete-key")
                              .replaceText(builder -> builder.match("<deletekey>").replacement(Component.text(s)
                                      .clickEvent(ClickEvent.copyToClipboard(s))))
                              )
                      );
                      PrismLogHandler.log("Deletion Key:" + s);
                  }
            );
        } else {
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerError(Il8nHelper.getMessage("debug-paste-error")));
        }
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("debug-help")};
    }

    @Override
    public String getRef() {
        return "/debug.html";
    }
}
