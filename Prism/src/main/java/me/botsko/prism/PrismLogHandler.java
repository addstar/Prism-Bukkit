package me.botsko.prism;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created for the Prism-Bukkit Project.
 *
 * @author Narimm
 */
public class PrismLogHandler implements Closeable {

    private static String name;
    private static final  Logger prismLog;
    private static boolean debug = false;

    static {
        if (Prism.getInstance() != null) {
            prismLog = createPrismLogger();
            debug = Prism.isDebug();
        } else {
            prismLog = Logger.getAnonymousLogger();
        }
        name = (Prism.getInstance() == null && Prism.getPrismName() == null) ? "[Test Prism]" : Prism.getPrismName();
    }

    /**
     * Class which handles all Prism Logging.
     */
    public PrismLogHandler() {
        name = (Prism.getInstance() == null && Prism.getPrismName() == null) ? "[Test Prism]" : Prism.getPrismName();
    }

    protected void setName(String n){
       name = n;
    }

    public static void setDebug(boolean debug) {
        PrismLogHandler.debug = debug;
    }

    private static Logger getLogger() {
        return Logger.getLogger("Minecraft");
    }

    /**
     * Log a message at {@link Level} info.
     *
     * @param message String
     */
    public static void log(String message) {
        getLogger().info("[" + name + "] " + message);
        prismLog.info(message);
    }

    /**
     * Log a warning.
     *
     * @param message String
     */
    public static void warn(String message) {
        getLogger().warning("[" + name + "] " + message);
        prismLog.warning(message);
    }

    /**
     * Log a message at {@link Level} warning.
     *
     * @param message String
     */
    public static void warn(String message, Exception e) {
        warn(Level.WARNING, "[" + name + "] " + message, e);
    }

    /**
     * Log a message at {@link Level} warning.
     *
     * @param warning Level
     * @param s       String
     * @param e       Exception
     */
    public static void warn(Level warning, String s, Exception e) {
        getLogger().log(warning, s, e);
        prismLog.log(Level.WARNING,s, e);

    }

    /**
     * Log a series of messages, precedent by a header.
     *
     * @param messages String[]
     */
    public static void logSection(String[] messages) {
        if (messages.length > 0) {
            log("--------------------- ## Important ## ---------------------");
            for (final String msg : messages) {
                log(msg);
            }
            log("--------------------- ## ========= ## ---------------------");
        }
    }

    /**
     * Log a debug message if config.yml has debug: true.
     *
     * @param message String
     */
    public static void debug(String message) {
        if (debug) {
            String m = "- Debug - " + message;
            //log(m);
            prismLog.info(m);
        }
    }

    /**
     * Log a debug message that occurs with an exception.
     * @param message String the message
     * @param e {@link Throwable}
     */
    public static void debug(String message, Throwable e) {
        if (debug) {
            String m = "- Debug - " + message;
            prismLog.log(Level.WARNING,m,e);
            prismLog.warning(e.getClass().toString() + e.getMessage());
            StackTraceElement[] stack = e.getStackTrace();
            for (StackTraceElement s: stack) {
                prismLog.log(Level.WARNING, "  at " + s.getClassName() + "." + s.getMethodName() + "("
                        + s.getFileName() + ":" + s.getLineNumber() + ")");
            }
        }
    }

    /**
     * Log the current location as a debug message.
     *
     * @param loc Location.
     */
    public static void debug(Location loc) {
        debug("Location: " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
    }

    private static Logger createPrismLogger() {
        Logger result = Logger.getLogger("PrismLogger");
        try {
            File prismFileLog = Prism.getInstance().getDataFolder().toPath().resolve("prism.log").toFile();
            if (!prismFileLog.getParentFile().exists()) {
                prismFileLog.getParentFile().mkdirs();
            }
            FileHandler handler = new PrismFileHandler(prismFileLog);
            result.setUseParentHandlers(false);
            for (Handler h : result.getHandlers()) {
                result.removeHandler(h);
            }
            result.addHandler(handler);
            result.setLevel(Level.CONFIG);

        } catch (IOException e) {
            if (debug) {
                e.printStackTrace();
            }
            result.log(Level.WARNING,e.getMessage(),e);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        for (Handler handler : PrismLogHandler.prismLog.getHandlers()) {
            handler.close();
        }
    }

    private static class PrismFileHandler extends FileHandler {

        public PrismFileHandler(File file) throws IOException, SecurityException {
            super(file.toString(),1000000,2);
            setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord lr) {
                    boolean mt = Bukkit.isPrimaryThread();
                    String thread;
                    if (mt) {
                        thread = "[M]";
                    } else {
                        thread = "[" + lr.getLongThreadID() + "]";
                    }
                    String thrown;
                    if (lr.getThrown() == null) {
                        thrown = "";
                    } else {
                        thrown = lr.getThrown().toString();
                    }
                    return String.format("[%1$tF %1$tT] [%2$-7s] " + thread + " %3$s%4$s%n",
                            new Date(lr.getMillis()),
                            lr.getLevel().getLocalizedName(),
                            lr.getMessage(),
                            thrown
                    );
                }
            });
        }

        @Override
        public synchronized void publish(LogRecord record) {
            super.publish(record);
            flush();
        }
    }
}
