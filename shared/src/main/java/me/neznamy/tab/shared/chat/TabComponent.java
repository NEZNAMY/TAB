package me.neznamy.tab.shared.chat;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.util.ComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for managing minecraft components.
 */
public abstract class TabComponent {

    /** Pattern for detecting fonts */
    private static final Pattern fontPattern = Pattern.compile("<font:(.*?)>(.*?)</font>");

    /**
     * Component cache maps to avoid large memory allocations as well as
     * higher CPU usage when using animations which send the same text on repeat.
     */
    private static final ComponentCache<String, TabComponent> stringCache = new ComponentCache<>(1000, (text, clientVersion) -> {
        return text.contains("#") || text.contains("&x") || text.contains(EnumChatFormat.COLOR_CHAR + "x") || text.contains("<") ?
                fromColoredText(text) : //contains RGB colors or font
                new SimpleComponent(text); //no RGB
    });

    /**
     * Serializes this component with colors based on client version.
     * If client version is below 1.16, HEX colors will be converted to legacy colors.
     *
     * @param   clientVersion
     *          client version to adapt component for
     * @return  serialized string
     */
    @NotNull
    public abstract String toString(@NotNull ProtocolVersion clientVersion);

    /**
     * Converts this component into a simple text with legacy colors (the closest match if color is set to RGB)
     *
     * @return  The simple text format using legacy colors
     */
    public abstract String toLegacyText();

    /**
     * Returns the most optimized component based on text. Returns null if text is null,
     * organized component if RGB colors are used or simple component with only text field
     * containing the whole text when no RGB colors are used
     *
     * @param   text
     *          text to create component from
     * @return  The most performance-optimized component based on text
     */
    @NotNull
    public static TabComponent optimized(@NotNull String text) {
        return stringCache.get(text, null);
    }

    /**
     * Returns organized component from colored text
     *
     * @param   originalText
     *          text to convert
     * @return  organized component from colored text
     */
    @NotNull
    public static TabComponent fromColoredText(@NotNull String originalText) {
        String remainingText = originalText;
        List<StructuredComponent> components = new ArrayList<>();
        while (!remainingText.isEmpty()) {
            Matcher m = fontPattern.matcher(remainingText);
            if (m.find()) {
                if (m.start() > 0) {
                    // Something is before the text with font, process normally
                    components.addAll(toComponentArray(remainingText.substring(0, m.start()), null));
                }
                // Process text with font
                String match = m.group();
                components.addAll(toComponentArray(
                        match.substring(match.indexOf('>')+1, match.length()-7),
                        match.substring(6, match.indexOf('>'))
                ));
                // Prepare the rest for next loop
                remainingText = remainingText.substring(m.start() + match.length());
            } else {
                components.addAll(toComponentArray(remainingText, null));
                break;
            }
        }
        if (components.isEmpty()) {
            return new SimpleComponent("");
        } else {
            return new StructuredComponent("", components);
        }
    }

    @NotNull
    private static List<StructuredComponent> toComponentArray(@NotNull String originalText, @Nullable String font) {
        String text = RGBUtils.getInstance().applyFormats(EnumChatFormat.color(originalText));
        List<StructuredComponent> components = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        StructuredComponent component = new StructuredComponent();
        component.getModifier().setFont(font);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == EnumChatFormat.COLOR_CHAR) {
                i++;
                if (i >= text.length()) {
                    break;
                }
                c = text.charAt(i);
                if ((c >= 'A') && (c <= 'Z')) {
                    c = (char)(c + ' ');
                }
                EnumChatFormat format = EnumChatFormat.getByChar(c);
                if (format != null) {
                    component.setText(builder.toString());
                    components.add(component);
                    component = new StructuredComponent(component);
                    component.setText("");
                    component.getModifier().setFont(font);
                    builder = new StringBuilder();
                    switch (format) {
                        case BOLD:
                            component.getModifier().setBold(true);
                            break;
                        case ITALIC:
                            component.getModifier().setItalic(true);
                            break;
                        case UNDERLINE:
                            component.getModifier().setUnderlined(true);
                            break;
                        case STRIKETHROUGH:
                            component.getModifier().setStrikethrough(true);
                            break;
                        case OBFUSCATED:
                            component.getModifier().setObfuscated(true);
                            break;
                        case RESET:
                            component = new StructuredComponent();
                            component.getModifier().setColor(TextColor.legacy(EnumChatFormat.WHITE));
                            component.getModifier().setFont(font);
                            break;
                        default:
                            component = new StructuredComponent();
                            component.getModifier().setColor(TextColor.legacy(format));
                            component.getModifier().setFont(font);
                            break;
                    }
                }
            } else if (c == '#' && text.length() > i+6) {
                String hex = text.substring(i+1, i+7);
                if (RGBUtils.getInstance().isHexCode(hex)) {
                    TextColor color;
                    EnumChatFormat code = text.length() - i >= 9 ? EnumChatFormat.getByChar(text.charAt(i+8)) : null;
                    if (code != null && text.charAt(i+7) == '|') {
                        color = new TextColor(hex, code);
                        i += 8;
                    } else {
                        color = new TextColor(hex);
                        i += 6;
                    }
                    if (builder.length() > 0) {
                        component.setText(builder.toString());
                        components.add(component);
                        builder = new StringBuilder();
                    }
                    component = new StructuredComponent();
                    component.getModifier().setColor(color);
                    component.getModifier().setFont(font);
                } else {
                    builder.append('#');
                }
            } else {
                builder.append(c);
            }
        }
        component.setText(builder.toString());
        components.add(component);
        return components;
    }
}
