package me.neznamy.tab.shared.chat.component;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TextColor;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import me.neznamy.tab.shared.util.function.TriFunction;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for managing minecraft components.
 */
public abstract class TabComponent {

    /** Formatter to convert gradient into TAB's #RRGGBB spam */
    private static final TriFunction<TextColor, String, TextColor, String> TABGradientFormatter = (start, text, end) -> {
        //lazy support for magic codes in gradients
        String magicCodes = EnumChatFormat.getLastColors(text);
        String deColorized = text.substring(magicCodes.length());
        StringBuilder sb = new StringBuilder();
        int length = deColorized.length();
        if (length == 1) {
            sb.append("#");
            sb.append(start.getHexCode());
            sb.append(magicCodes);
            sb.append(deColorized);
            return sb.toString();
        }
        for (int i=0; i<length; i++) {
            int red = (int) (start.getRed() + (float)(end.getRed() - start.getRed())/(length-1)*i);
            int green = (int) (start.getGreen() + (float)(end.getGreen() - start.getGreen())/(length-1)*i);
            int blue = (int) (start.getBlue() + (float)(end.getBlue() - start.getBlue())/(length-1)*i);
            sb.append("#");
            sb.append(new TextColor(red, green, blue).getHexCode());
            sb.append(magicCodes);
            sb.append(deColorized.charAt(i));
        }
        return sb.toString();
    };

    /** Formatter to convert RGB code to use TAB's #RRGGBB */
    private static final Function<TextColor, String> TABRGBFormatter = color -> "#" + color.getHexCode();

    /** Pattern for detecting fonts */
    private static final Pattern fontPattern = Pattern.compile("<font:(.*?)>(.*?)</font>");

    @Nullable
    private Object convertedModern;

    @Nullable
    private Object convertedLegacy;

    /** Adventure component from this component */
    @Nullable
    @Setter
    private Component adventureComponent;

    @Nullable
    private Object fixedFormat;

    /** TextHolder object for Velocity */
    @Nullable
    private Object textHolder;

    /**
     * Last color of this component.
     * Used to determine team color based on the last color of prefix.
     * Saved as TextColor instead of EnumChatFormat to have things ready if Mojang adds RGB support to team color.
     */
    @Nullable
    private TextColor lastColor;

    /** Chat modifier containing color, magic codes, hover and click event */
    @NotNull
    @Getter
    protected ChatModifier modifier = new ChatModifier();

    /** Extra components used in "extra" field */
    protected List<TabComponent> extra;

    /**
     * Returns list of extra components. If no extra components are defined, returns empty list.
     *
     * @return  list of extra components
     */
    public List<TabComponent> getExtra() {
        if (extra == null) return Collections.emptyList();
        return extra;
    }

    /**
     * Adds extra component to this component.
     *
     * @param   extra
     *          Extra component to append
     */
    public void addExtra(@NotNull TabComponent extra) {
        if (this.extra == null) this.extra = new ArrayList<>();
        this.extra.add(extra);
    }

    /**
     * Converts this component to platform's component.
     *
     * @param   clientVersion
     *          Client version
     * @return  Converted component
     * @param   <T>
     *          Platform's component class
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T convert(@NotNull ProtocolVersion clientVersion) {
        if (clientVersion.supportsRGB()) {
            if (convertedModern == null) convertedModern = TAB.getInstance().getPlatform().convertComponent(this, true);
            return (T) convertedModern;
        } else {
            if (convertedLegacy == null) convertedLegacy = TAB.getInstance().getPlatform().convertComponent(this, false);
            return (T) convertedLegacy;
        }
    }

    /**
     * Converts this component to platform's component.
     *
     * @return  Converted component
     * @param   <T>
     *          Platform's component class
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T convert() {
        if (convertedModern == null) convertedModern = TAB.getInstance().getPlatform().convertComponent(this, true);
        return (T) convertedModern;
    }

    /**
     * Converts this component to an Adventure component.
     * @return  Converted component
     */
    @NotNull
    public Component toAdventure() {
        if (adventureComponent == null) adventureComponent = AdventureHook.convert(this);
        return adventureComponent;
    }

    /**
     * Creates a FixedFormat using given platform-specific create function.
     * If the value is already initialized, it is returned immediately instead.
     *
     * @param   createFunction
     *          Platform's function to convert platform component to FixedFormat
     * @return  Platform's FixedFormat from this component
     * @param   <F>
     *          Platform's FixedFormat type
     * @param   <C>
     *          Platform's Component type
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public <F, C> F toFixedFormat(@NotNull FunctionWithException<C, F> createFunction) {
        if (fixedFormat == null) fixedFormat = createFunction.apply(convert());
        return (F) fixedFormat;
    }

    /**
     * Creates a text holder object using provided function if it does not exist and returns it.
     *
     * @param   convertFunction
     *          Function for converting adventure Component to TextHolder
     * @return  Converted TextHolder
     * @param   <T>
     *          TextHolder type
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T toTextHolder(@NotNull Function<TabComponent, T> convertFunction) {
        if (textHolder == null) textHolder = convertFunction.apply(this);
        return (T) textHolder;
    }

    /**
     * Returns last color of this component. This value is cached. If no color is used, WHITE color is returned.
     *
     * @return  Last color of this component
     */
    @NotNull
    public TextColor getLastColor() {
        if (lastColor == null) {
            lastColor = fetchLastColor();
            if (lastColor == null) lastColor = TextColor.WHITE;
        }
        return lastColor;
    }

    /**
     * Converts this component into a simple text with legacy colors (the closest match if color is set to RGB)
     *
     * @return  The simple text format using legacy colors
     */
    @NotNull
    public abstract String toLegacyText();

    /**
     * Computes and returns the last used color code in this component.
     * If no color is present, {@code null} is returned.
     *
     * @return  Last color of this component, {@code null} if no colors are used
     */
    @Nullable
    protected TextColor fetchLastColor() {
        TextColor lastColor = modifier.getColor();
        for (TabComponent extra : getExtra()) {
            TextColor color = extra.fetchLastColor();
            if (color != null) {
                lastColor = color;
            }
        }
        return lastColor;
    }

    /**
     * Converts this component into a string that only consists of text without any formatting.
     *
     * @return  String containing text of the component and extras
     */
    @NotNull
    public String toRawText() {
        StringBuilder builder = new StringBuilder();
        if (this instanceof TextComponent) builder.append(((TextComponent)this).getText());
        for (TabComponent extra : extra) {
            if (extra instanceof TextComponent) {
                builder.append(((TextComponent) extra).getText());
            }
        }
        return builder.toString();
    }

    /**
     * Returns organized component from colored text
     *
     * @param   originalText
     *          text to convert
     * @return  organized component from colored text
     */
    @NotNull
    public static TextComponent fromColoredText(@NotNull String originalText) {
        String remainingText = originalText;
        List<TabComponent> components = new ArrayList<>();
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
        return new TextComponent("", components);
    }

    @NotNull
    private static List<TextComponent> toComponentArray(@NotNull String originalText, @Nullable String font) {
        String text = RGBUtils.getInstance().applyFormats(EnumChatFormat.color(originalText), TABGradientFormatter, TABRGBFormatter);
        List<TextComponent> components = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        component.modifier.setFont(font);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§') {
                i++;
                if (i >= text.length()) {
                    break;
                }
                c = text.charAt(i);
                if ((c >= 'A') && (c <= 'Z')) {
                    c = (char)(c + ' ');
                }
                TextColor format = TextColor.getLegacyByChar(c);
                if (format != null) {
                    if (builder.length() > 0) {
                        component.setText(builder.toString());
                        components.add(component);
                        component = new TextComponent(component);
                        component.setText("");
                        component.modifier.setFont(font);
                        builder = new StringBuilder();
                    }
                    if (format == TextColor.BOLD) {
                        component.modifier.setBold(true);
                    } else if (format == TextColor.ITALIC) {
                        component.modifier.setItalic(true);
                    } else if (format == TextColor.UNDERLINE) {
                        component.modifier.setUnderlined(true);
                    } else if (format == TextColor.STRIKETHROUGH) {
                        component.modifier.setStrikethrough(true);
                    } else if (format == TextColor.OBFUSCATED) {
                        component.modifier.setObfuscated(true);
                    } else if (format == TextColor.RESET) {
                        component = new TextComponent();
                        component.modifier.setColor(TextColor.WHITE);
                        component.modifier.setFont(font);
                    } else {
                        component = new TextComponent();
                        component.modifier.setColor(format);
                        component.modifier.setFont(font);
                    }
                }
            } else if (c == '#' && text.length() > i+6) {
                String hex = text.substring(i+1, i+7);
                if (isHexCode(hex)) {
                    TextColor color = new TextColor(hex);
                    i += 6;
                    if (builder.length() > 0) {
                        component.setText(builder.toString());
                        components.add(component);
                        builder = new StringBuilder();
                    }
                    component = new TextComponent();
                    component.modifier.setColor(color);
                    component.modifier.setFont(font);
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

    /**
     * Returns true if entered string is a valid 6-digit combination of
     * hexadecimal numbers, false if not
     *
     * @param   string
     *          string to check
     * @return  {@code true} if valid, {@code false} if not
     */
    private static boolean isHexCode(@NotNull String string) {
        for (int i=0; i<string.length(); i++) {
            if ("0123456789AaBbCcDdEeFf".indexOf(string.charAt(i)) == -1) return false;
        }
        return true;
    }
}
