package me.neznamy.chat.component;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import me.neznamy.chat.ChatModifier;
import me.neznamy.chat.EnumChatFormat;
import me.neznamy.chat.TextColor;
import me.neznamy.chat.component.object.AtlasSprite;
import me.neznamy.chat.component.object.ObjectComponent;
import me.neznamy.chat.hook.AdventureHook;
import me.neznamy.chat.rgb.RGBUtils;
import me.neznamy.chat.util.TriFunction;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for managing minecraft components.
 */
public abstract class TabComponent {

    /** Empty component to avoid recreating one over and over */
    public static final LegacyTextComponent EMPTY_LEGACY_TEXT = new LegacyTextComponent("");

    /** Function for converting this class into platform's actual component */
    @Nullable
    public static Function<TabComponent, Object> CONVERT_FUNCTION;

    /** Formatter to convert gradient into TAB's #RRGGBB spam */
    private static final TriFunction<TextColor, String, TextColor, String> TABGradientFormatter = (start, text, end) -> {
        if (text.length() == 1) {
            return "#" + start.getHexCode() + text;
        }
        StringBuilder sb = new StringBuilder();
        List<Character> characters = new ArrayList<>();
        List<ChatModifier> modifiers = new ArrayList<>();
        ChatModifier modifier = new ChatModifier();
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i < text.length() - 1) {
                switch (text.charAt(i+1)) {
                    case 'l':
                        modifier.setBold(true);
                        i++;
                        break;
                    case 'o':
                        modifier.setItalic(true);
                        i++;
                        break;
                    case 'k':
                        modifier.setObfuscated(true);
                        i++;
                        break;
                    case 'm':
                        modifier.setStrikethrough(true);
                        i++;
                        break;
                    case 'n':
                        modifier.setUnderlined(true);
                        i++;
                        break;
                    case 'r':
                        modifier = new ChatModifier();
                        i++;
                        break;
                    default:
                        // Invalid code
                        characters.add('§');
                        modifiers.add(new ChatModifier(modifier));
                        break;
                }
            } else {
                characters.add(c);
                modifiers.add(new ChatModifier(modifier));
            }
        }

        int length = characters.size();
        for (int i=0; i<length; i++) {
            int red = (int) (start.getRed() + (float)(end.getRed() - start.getRed())/(length-1)*i);
            int green = (int) (start.getGreen() + (float)(end.getGreen() - start.getGreen())/(length-1)*i);
            int blue = (int) (start.getBlue() + (float)(end.getBlue() - start.getBlue())/(length-1)*i);
            sb.append(String.format("#%02X%02X%02X", red, green, blue));
            sb.append(modifiers.get(i).getMagicCodes());
            sb.append(characters.get(i));
        }
        return sb.toString();
    };

    /** Formatter to convert RGB code to use TAB's #RRGGBB */
    private static final Function<TextColor, String> TABRGBFormatter = color -> "#" + color.getHexCode();

    /** Pattern for detecting fonts */
    private static final Pattern fontPattern = Pattern.compile("<font:(.*?)>(.*?)</font>");

    private static final Pattern SPRITE_PATTERN = Pattern.compile("<sprite:(?:\"([^\"]+)\"|([^:]+)):(?:\"([^\"]+)\"|([^>]+))>");;

    @Nullable
    private Object converted;

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
    @Setter
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
     * @return  Converted component
     * @param   <T>
     *          Platform's component class
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T convert() {
        if (CONVERT_FUNCTION == null) throw new IllegalStateException("Convert function is not initialized");
        if (converted == null) converted = CONVERT_FUNCTION.apply(this);
        return (T) converted;
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
    public <F, C> F toFixedFormat(@NotNull Function<C, F> createFunction) {
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
        TextComponent component = new TextComponent("", components);
        // Safe check to avoid rare mojang "bug" that display text as italic by default
        // This doesn't affect #toLegacyText() method at all
        component.modifier.setItalic(false);
        return component;
    }

    @NotNull
    private static List<TabComponent> toComponentArray(@NotNull String originalText, @Nullable String font) {
        String text = RGBUtils.getInstance().applyFormats(EnumChatFormat.color(originalText), TABGradientFormatter, TABRGBFormatter);
        List<TabComponent> components = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        component.modifier.setFont(font);
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '<') {
                Matcher matcher = SPRITE_PATTERN.matcher(text.substring(i));
                if (matcher.find() && matcher.start() == 0) {
                    // flush current builder
                    if (builder.length() > 0) {
                        component.setText(builder.toString());
                        components.add(component);
                        component = new TextComponent(component);
                        component.setText("");
                        component.modifier.setFont(font);
                        builder = new StringBuilder();
                    }
                    String atlas = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                    String sprite = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
                    components.add(objectAtlasSprite(atlas, sprite));

                    // skip
                    i += matcher.group(0).length() - 1;

                    // reset formatting component safely
                    component = new TextComponent(component);
                    component.setText("");
                    component.modifier.setFont(font);
                    continue;
                }
            }

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

    /**
     * Returns an empty component of "text" type.
     * This is used to avoid creating new empty components over and over.
     *
     * @return  Empty legacy text component
     */
    public static TabComponent empty() {
        return EMPTY_LEGACY_TEXT;
    }

    /**
     * Creates a new component of "text" type with given legacy text.
     *
     * @param   text
     *          Text to use in the component
     * @return  New legacy text component with given text
     */
    public static LegacyTextComponent legacyText(@NonNull String text) {
        if (text.isEmpty()) return EMPTY_LEGACY_TEXT;
        return new LegacyTextComponent(text);
    }

    /**
     * Creates a new component of "translatable" type with given key.
     *
     * @param   key
     *          Translatable to use in the component
     * @return  New translatable component with given key
     */
    public static TranslatableComponent translatable(@NonNull String key) {
        return new TranslatableComponent(key);
    }

    /**
     * Creates a new component of "keybind" type with given key.
     *
     * @param   keybind
     *          Key to use in the component
     * @return  New keybind text component with given key
     */
    public static KeybindComponent keybind(@NonNull String keybind) {
        return new KeybindComponent(keybind);
    }

    /**
     * Creates a new component of "object" type with given atlas and sprite.
     *
     * @param   atlas
     *          Atlas to use in the component
     * @param   sprite
     *          Sprite to use in the component
     * @return  New object component with given atlas and sprite
     */
    public static ObjectComponent objectAtlasSprite(@NonNull String atlas, @NonNull String sprite) {
        return new ObjectComponent(new AtlasSprite(
                atlas.toLowerCase(Locale.US).replace(" ", "_"),
                sprite.toLowerCase(Locale.US).replace(" ", "_")
        ));
    }
}
