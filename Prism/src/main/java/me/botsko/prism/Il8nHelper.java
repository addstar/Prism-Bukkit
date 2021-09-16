package me.botsko.prism;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.jetbrains.annotations.PropertyKey;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 29/07/2020.
 */
public class Il8nHelper {
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("languages.message",
            new UTF8ResourceBundleControl());

    /**
     * Returns a TextComponent un-styled from the give key.
     *
     * @param key String
     * @return TextComponent
     */
    public static TextComponent getMessage(@PropertyKey(resourceBundle = "languages.message") String key) {
        return getMessage(key, "");
    }

    /**
     * Returns a TextComponent un-styled from the give key.
     * @param key String
     * @return TextComponent
     */
    public static TextComponent getMessage(@PropertyKey(resourceBundle = "languages.message") String key,
                                           String extra) {
        return Component.text(getRawMessage(key) + " " + extra);
    }

    /**
     * Returns a raw string message.
     * @param key String
     * @return String
     */
    public static String getRawMessage(@PropertyKey(resourceBundle = "languages.message") String key) {
        if (resourceBundle == null) {
            return key;
        }
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            PrismLogHandler.log("Missing Resource " + e.getMessage());
            return key;
        }
    }

    /**
     * Produces a TexComponent that can accept typical java String.format type replacement before its created.  This
     * uses {@link java.util.Formatter}  printf-style format strings.
     * @param key Il8n key containing printf style formatting marks
     * @param args Objects to insert.
     * @return TextComponent
     */
    public static TextComponent formatMessage(@PropertyKey(resourceBundle = "languages.message") String key,
                                              Object... args) {
        if (resourceBundle == null) {
            return Component.text(key);
        }

        try {
            String format = resourceBundle.getString(key);
            String out = String.format(format, args);
            return Component.text(out);
        } catch (MissingResourceException e) {
            PrismLogHandler.log("Missing Resource " + e.getMessage());
            return Component.text(key);
        } catch (MissingFormatArgumentException e) {
            PrismLogHandler.log("Missing Format Argument " + e.getMessage());
            return getMessage(key);
        }
    }
}
