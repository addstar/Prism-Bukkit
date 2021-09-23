package me.botsko.prism.text;


import me.botsko.prism.Il8nHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.PropertyKey;

import java.util.regex.Pattern;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 6/08/2020.
 */
public class ReplaceableTextComponent {

    private Component component;

    /**
     * Helper Class to assist with replacement during Il8n.  May be removed when Adventure library
     * provides its own methods.
     * @param component Component.
     */
    private ReplaceableTextComponent(Component component) {
        this.component = component;
    }

    /**
     * Static builder.
     * @param key Il8n key
     * @return ReplaceableTextComponent
     */
    public static ReplaceableTextComponent builder(@PropertyKey(resourceBundle = "languages.message") String key) {
        return new ReplaceableTextComponent(Il8nHelper.getMessage(key));
    }

    /**
     * Replace Text with style.
     *
     * @param key       String
     * @param content   String
     * @param withStyle Style
     * @return ReplaceableTextComponent
     */
    public ReplaceableTextComponent replace(String key, String content, Style withStyle) {
        this.component = component
                .replaceText(TextReplacementConfig.builder().match(Pattern.compile(key))
                        .replacement(Component.text(content).style(withStyle))
                        .build());
        return this;
    }

    /**
     * Replace Text with style.
     *
     * @param key     String
     * @param content String
     * @return ReplaceableTextComponent
     */
    public ReplaceableTextComponent replace(String key, String content) {
        replace(key, content, Style.empty());
        return this;
    }

    /**
     * Replace Text with style.
     *
     * @param key       String
     * @param content   Object
     * @param withStyle Style
     * @return ReplaceableTextComponent
     */
    public ReplaceableTextComponent replace(String key, Object content, Style withStyle) {
        replace(key, String.valueOf(content), withStyle);
        return this;
    }

    /**
     * Replace Text with style.
     *
     * @param key     String
     * @param content Object
     * @return ReplaceableTextComponent
     */
    public ReplaceableTextComponent replace(String key, Object content) {
        replace(key, String.valueOf(content), Style.empty());
        return this;
    }

    /**
     * Replace Text with style.
     *
     * @param key     String
     * @param content Object
     * @return ReplaceableTextComponent
     */
    public ReplaceableTextComponent replaceFirst(String key, Object content) {
        return replaceFirst(key, String.valueOf(content));
    }

    /**
     * Replace Text with style.
     *
     * @param key     String
     * @param content Object
     * @return ReplaceableTextComponent
     */
    public ReplaceableTextComponent replaceFirst(String key, String content) {
        this.component = component.replaceText(TextReplacementConfig.builder().match(Pattern.compile(key))
                .replacement(Component.text(content)).once().build()
        );
        return this;
    }

    /**
     * Build the Component.
     * @return Component
     */
    public Component build() {
        return component;
    }
}
