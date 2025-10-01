package me.neznamy.tab.shared.chat.component;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import me.neznamy.tab.shared.chat.hook.AdventureHook;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.function.TriFunction;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for managing minecraft components.
 */
public abstract class TabComponent {

    /** Empty component to avoid recreating one over and over */
    public static final LegacyTextComponent EMPTY_LEGACY_TEXT = new LegacyTextComponent("");

    /** Formatter to convert gradient into TAB's #RRGGBB spam */
    private static final TriFunction<TabTextColor, String, TabTextColor, String> TABGradientFormatter = (start, text, end) -> {
        if (text.length() == 1) {
            return "#" + start.getHexCode() + text;
        }
        StringBuilder sb = new StringBuilder();
        List<Character> characters = new ArrayList<>();
        List<TabStyle> modifiers = new ArrayList<>();
        TabStyle modifier = new TabStyle();
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
                        modifier = new TabStyle();
                        i++;
                        break;
                    default:
                        // Invalid code
                        characters.add('§');
                        modifiers.add(new TabStyle(modifier));
                        break;
                }
            } else {
                characters.add(c);
                modifiers.add(new TabStyle(modifier));
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
    private static final Function<TabTextColor, String> TABRGBFormatter = color -> "#" + color.getHexCode();

    /** Pattern for detecting fonts */
    private static final Pattern fontPattern = Pattern.compile("<font:(.*?)>(.*?)</font>");

    private static final Pattern ATLAS_PATTERN = Pattern.compile("<sprite:(?:\"([^\"]+)\"|([^:]+)):(?:\"([^\"]+)\"|([^>]+))>");

    private static final Pattern HEAD_PATTERN = Pattern.compile("<head:([^>]+)>");

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
     * Last style of this component.
     * Used to determine team color based on the last color of prefix.
     * Saved as ChatModifier instead of EnumChatFormat to have things ready if Mojang adds RGB support to team color, as well as
     * properly handle cases when both color and magic codes are used.
     */
    @Nullable
    private TabStyle lastStyle;

    /** Chat modifier containing color, magic codes, hover and click event */
    @NotNull
    @Getter
    @Setter
    protected TabStyle modifier = new TabStyle();

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
        if (converted == null) converted = TAB.getInstance().getPlatform().convertComponent(this);
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
     * Returns last style of this component. This value is cached.
     *
     * @return  Last style of this component
     */
    @NotNull
    public TabStyle getLastStyle() {
        if (lastStyle == null) lastStyle = fetchLastStyle();
        return lastStyle;
    }

    /**
     * Converts this component into a simple text with legacy colors (the closest match if color is set to RGB)
     *
     * @return  The simple text format using legacy colors
     */
    @NotNull
    public abstract String toLegacyText();

    /**
     * Computes and returns the last used style in this component.
     *
     * @return  Last style of this component
     */
    @NotNull
    protected TabStyle fetchLastStyle() {
        TabStyle lastStyle = modifier;
        for (TabComponent extra : getExtra()) {
            lastStyle = extra.fetchLastStyle();
        }
        return lastStyle;
    }

    /**
     * Returns organized component from colored text
     *
     * @param   originalText
     *          text to convert
     * @return  organized component from colored text
     */
    @NotNull
    public static TabTextComponent fromColoredText(@NotNull String originalText) {
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
        TabTextComponent root = new TabTextComponent("", components);
        // Avoid team color affecting prefix/suffix
        root.modifier.setColor(TabTextColor.WHITE);
        root.modifier.setBold(false);
        root.modifier.setItalic(false);
        root.modifier.setUnderlined(false);
        root.modifier.setStrikethrough(false);
        root.modifier.setObfuscated(false);
        return root;
    }

    @NotNull
    private static List<TabComponent> toComponentArray(@NotNull String originalText, @Nullable String font) {
        String text = RGBUtils.getInstance().applyFormats(EnumChatFormat.color(originalText), TABGradientFormatter, TABRGBFormatter);
        List<TabComponent> components = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        TabTextComponent component = new TabTextComponent();
        component.modifier.setFont(font);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '<') {
                Matcher matcher = ATLAS_PATTERN.matcher(text.substring(i));
                if (matcher.find() && matcher.start() == 0) {
                    // flush current builder
                    if (builder.length() > 0) {
                        component.setText(builder.toString());
                        components.add(component);
                        component = new TabTextComponent(component);
                        component.setText("");
                        component.modifier.setFont(font);
                        builder = new StringBuilder();
                    }
                    String atlas = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                    String sprite = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
                    components.add(atlasSprite(atlas, sprite));

                    // skip
                    i += matcher.group(0).length() - 1;

                    // reset formatting component safely
                    component = new TabTextComponent(component);
                    component.setText("");
                    component.modifier.setFont(font);
                    continue;
                }

                matcher = HEAD_PATTERN.matcher(text.substring(i));
                if (matcher.find() && matcher.start() == 0) {
                    String skinDefinition = matcher.group(1);
                    // flush current builder
                    if (builder.length() > 0) {
                        component.setText(builder.toString());
                        components.add(component);
                        component = new TabTextComponent(component);
                        component.setText("");
                        component.modifier.setFont(font);
                        builder = new StringBuilder();
                    }
                    components.add(head(skinDefinition));

                    // skip
                    i += matcher.group(0).length() - 1;

                    // reset formatting component safely
                    component = new TabTextComponent(component);
                    component.setText("");
                    component.modifier.setFont(font);
                    continue;
                }
            }

            if (c == '§') {
                i++;
                if (i >= text.length()) {
                    break;
                }
                c = text.charAt(i);
                if ((c >= 'A') && (c <= 'Z')) {
                    c = (char)(c + ' ');
                }
                TabTextColor format = TabTextColor.getLegacyByChar(c);
                if (format != null) {
                    if (builder.length() > 0) {
                        component.setText(builder.toString());
                        components.add(component);
                        component = new TabTextComponent(component);
                        component.setText("");
                        component.modifier.setFont(font);
                        builder = new StringBuilder();
                    }
                    if (format == TabTextColor.BOLD) {
                        component.modifier.setBold(true);
                    } else if (format == TabTextColor.ITALIC) {
                        component.modifier.setItalic(true);
                    } else if (format == TabTextColor.UNDERLINE) {
                        component.modifier.setUnderlined(true);
                    } else if (format == TabTextColor.STRIKETHROUGH) {
                        component.modifier.setStrikethrough(true);
                    } else if (format == TabTextColor.OBFUSCATED) {
                        component.modifier.setObfuscated(true);
                    } else if (format == TabTextColor.RESET) {
                        component = new TabTextComponent();
                        component.modifier.setColor(TabTextColor.WHITE);
                        component.modifier.setFont(font);
                    } else {
                        component = new TabTextComponent();
                        component.modifier.setColor(format);
                        component.modifier.setFont(font);
                    }
                }
            } else if (c == '#' && text.length() > i+6) {
                String hex = text.substring(i+1, i+7);
                if (isHexCode(hex)) {
                    TabTextColor color = new TabTextColor(hex);
                    i += 6;
                    if (builder.length() > 0) {
                        component.setText(builder.toString());
                        components.add(component);
                        builder = new StringBuilder();
                    }
                    component = new TabTextComponent();
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
    @NotNull
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
    @NotNull
    public static LegacyTextComponent legacyText(@NonNull String text) {
        if (text.isEmpty()) return EMPTY_LEGACY_TEXT;
        LegacyTextComponent component = new LegacyTextComponent(text);
        // Avoid team color affecting prefix/suffix
        component.modifier.setColor(TabTextColor.WHITE);
        component.modifier.setBold(false);
        component.modifier.setItalic(false);
        component.modifier.setUnderlined(false);
        component.modifier.setStrikethrough(false);
        component.modifier.setObfuscated(false);
        return component;
    }

    /**
     * Creates a new component of "translatable" type with given key.
     *
     * @param   key
     *          Translatable to use in the component
     * @return  New translatable component with given key
     */
    @NotNull
    public static TabTranslatableComponent translatable(@NonNull String key) {
        return new TabTranslatableComponent(key);
    }

    /**
     * Creates a new component of "keybind" type with given key.
     *
     * @param   keybind
     *          Key to use in the component
     * @return  New keybind text component with given key
     */
    @NotNull
    public static TabKeybindComponent keybind(@NonNull String keybind) {
        return new TabKeybindComponent(keybind);
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
    @NotNull
    public static TabObjectComponent atlasSprite(@NonNull String atlas, @NonNull String sprite) {
        return new TabObjectComponent(new TabAtlasSprite(
                atlas.toLowerCase(Locale.US).replace(" ", "_"),
                sprite.toLowerCase(Locale.US).replace(" ", "_")
        ));
    }

    /**
     * Creates a new component of "object" type with given player skin definition.
     * If the skin definition is invalid in any way, text component with the error message is returned.
     *
     * @param   skinDefinition
     *          Skin definition to use in the component
     * @return  New object component with given player skin or text component with error message
     */
    @NotNull
    public static TabComponent head(@NonNull String skinDefinition) {
        UUID id = null;
        String name = null;
        TabList.Skin skin = null;
        if (skinDefinition.startsWith("id:")) {
            String stringUUID = skinDefinition.substring(3);
            try {
                id = UUID.fromString(stringUUID);
            } catch (IllegalArgumentException e) {
                return new LegacyTextComponent(String.format("<Invalid UUID: \"%s\">", stringUUID));
            }
        } else if (skinDefinition.startsWith("name:")) {
            name = skinDefinition.substring(5);
            if (name.length() > 16) {
                return new LegacyTextComponent(String.format("<Invalid name (too long): \"%s\">", name));
            }
        } else {
            skin = TAB.getInstance().getConfiguration().getSkinManager().getSkin(skinDefinition);
            if (skin == null) {
                return new LegacyTextComponent(String.format("<Invalid skin: \"%s\">", skinDefinition));
            }
        }
        TabObjectComponent component = new TabObjectComponent(new TabPlayerSprite(id, name, skin, true)); // Always show hat
        if (TAB.getInstance().getConfiguration().getConfig().getComponents().isDisableShadowForHeads()) {
            component.modifier.setShadowColor(0); // Hide shadow to match heads in online mode
        }
        return component;
    }

    /**
     * Creates a new component of "object" type with given player sprite.
     *
     * @param   sprite
     *          Player sprite to use in the component
     * @return  New object component with given player sprite
     */
    @NotNull
    public static TabComponent head(@NonNull TabPlayerSprite sprite) {
        return new TabObjectComponent(sprite);
    }
}
