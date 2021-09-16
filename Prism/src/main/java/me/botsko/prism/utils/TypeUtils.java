package me.botsko.prism.utils;

import com.sk89q.worldedit.util.formatting.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyFormat;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class TypeUtils {
    private static final String HEX_REGEX = "#([A-Fa-f0-9]{6})";
    private static final Pattern HEX_PATTERN = Pattern.compile(HEX_REGEX);

    /**
     * Is the string numeric.
     *
     * @param str String
     * @return boolean
     */
    public static boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Format a double to a float.
     *
     * @param val double
     * @return Float
     */
    public static float formatDouble(double val) {
        return Float.parseFloat(new DecimalFormat("#.##").format(val));
    }

    /**
     * Replaces string template placeholders with values in a Hashtable. Text should
     * be formatted with %(key) type placeholders.
     *
     * @param msg      the message
     * @param replacer the replacement Map
     * @return a String
     */
    public static String getStringFromTemplate(String msg, Map<String, String> replacer) {
        if (msg != null && !replacer.isEmpty()) {
            for (Entry<String, String> entry : replacer.entrySet()) {
                msg = msg.replace("%(" + entry.getKey() + ")", entry.getValue());
            }
        }
        return msg;
    }

    /**
     * Converts colors place-holders.
     *
     * @param text String
     * @return String
     */
    public static Component colorize(String text) {
        return LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.SECTION_CHAR)
                .hexColors()
                .build()
                .deserialize(text);
    }

    /**
     * Get Color.
     *
     * @param hex String.
     * @return ChatColor.
     */
    public static TextColor from(String hex) {
        if (hex.length() == 2 && hex.startsWith("&")) {
            LegacyFormat format = LegacyComponentSerializer.parseChar(hex.charAt(1));
            if (format != null && format.color() != null) {
                return format.color();
            } else {
                return NamedTextColor.WHITE;
            }
        }
        if (hex.length() != 7 && !hex.startsWith("#")) {
            me.botsko.prism.PrismLogHandler.log("Could not decode as hex:" + hex);
            TextColor color = NamedTextColor.NAMES.value(hex);
            if (color != null) {
                return color;
            }
            return NamedTextColor.WHITE;
        }
        TextColor color = TextColor.fromHexString(hex);
        if (color == null) {
            return NamedTextColor.WHITE;
        }
        return color;
    }

    /**
     * Strips all text format codes - from colors codes like &2 to text format codes
     * like &k.
     *
     * @param text String
     * @return String
     */
    public static String stripTextFormatCodes(String text) {
        return PlainComponentSerializer.INSTANCE.deserialize(text).content();
    }

    /**
     * Joins an arraylist together by a delimiter.
     *
     * @param s         Collection
     * @param delimiter String
     * @return String
     */
    public static String join(Iterable<String> s, String delimiter) {
        StringBuilder buffer = new StringBuilder();
        Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

    /**
     * Method to join array elements of type string.
     *
     * @param inputArray Array which contains strings
     * @param glueString String between each array element
     * @return String containing all array elements separated by glue string
     * @author Hendrik Will, imwill.com, bug fixes by viveleroi
     */
    public static String join(String[] inputArray, String glueString) {
        String output = "";
        if (inputArray.length > 0) {
            StringBuilder sb = new StringBuilder();
            if (!inputArray[0].isEmpty()) {
                sb.append(inputArray[0]);
            }
            for (int i = 1; i < inputArray.length; i++) {
                if (!inputArray[i].isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append(glueString);
                    }
                    sb.append(inputArray[i]);
                }
            }
            output = sb.toString();
        }
        return output;
    }

    /**
     * Upper case.
     *
     * @param s String
     * @return String
     */
    public static String strToUpper(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    /**
     * Java implementation of preg_match_all by https://github.com/raimonbosch
     *
     * @param p       Pattern
     * @param subject CharSequence
     * @return String[]
     */
    public static String[] preg_match_all(Pattern p, CharSequence subject) {
        Matcher m = p.matcher(subject);
        StringBuilder out = new StringBuilder();
        boolean split = false;
        while (m.find()) {
            out.append(m.group());
            out.append("~");
            split = true;
        }
        return (split) ? out.toString().split("~") : new String[0];
    }

    /**
     * Position of occurance.
     *
     * @param str     String
     * @param findStr String
     * @return int
     */
    public static int subStrOccurences(String str, String findStr) {
        int lastIndex = 0;
        int count = 0;
        while (lastIndex != -1) {
            lastIndex = str.indexOf(findStr, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }

    /**
     * Pad a String.
     *
     * @param str           String
     * @param desiredLength int
     * @return String
     */
    public static String padStringRight(String str, int desiredLength) {
        if (str.length() >= desiredLength) {
            return str.substring(0, desiredLength);
        }
        StringBuilder sb = new StringBuilder();
        int rest = desiredLength - str.length();
        sb.append(" ".repeat(rest - 1));
        return str + sb;
    }
}
