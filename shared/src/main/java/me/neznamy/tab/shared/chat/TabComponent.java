package me.neznamy.tab.shared.chat;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.util.FunctionWithException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for managing minecraft components.
 */
public abstract class TabComponent {

    /** Pattern for detecting fonts */
    private static final Pattern fontPattern = Pattern.compile("<font:(.*?)>(.*?)</font>");

    @Nullable
    private Object convertedModern;

    @Nullable
    private Object convertedLegacy;

    /** Adventure component from this component for 1.16+ players */
    @Nullable
    private Component adventureModern;

    /** Adventure component from this component for 1.15- players */
    @Nullable
    private Component adventureLegacy;

    @Nullable
    private String jsonModern;

    @Nullable
    private String jsonLegacy;

    @Nullable
    private Object fixedFormat;

    /** TextHolder object for Velocity */
    @Nullable
    private Object textHolder;

    /**
     * Last color of this component. Used to determine team color based on last color of prefix.
     * Saves as TextColor instead of EnumChatFormat to have things ready if Mojang adds RGB support to team color.
     */
    @Nullable
    private TextColor lastColor;

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
     * Converts this component to adventure component.
     *
     * @param   clientVersion
     *          Client version
     * @return  Converted component
     */
    @NotNull
    public Component toAdventure(@NotNull ProtocolVersion clientVersion) {
        if (clientVersion.supportsRGB()) {
            if (adventureModern == null) adventureModern = AdventureHook.toAdventureComponent(this, true);
            return adventureModern;
        } else {
            if (adventureLegacy == null) adventureLegacy = AdventureHook.toAdventureComponent(this, false);
            return adventureLegacy;
        }
    }

    /**
     * Serializes this component to string using Adventure API.
     *
     * @param   clientVersion
     *          Client version to serialize for
     * @return  Serialized json
     */
    @NotNull
    public String serialize(@NotNull ProtocolVersion clientVersion) {
        if (clientVersion.supportsRGB()) {
            if (jsonModern == null) jsonModern = GsonComponentSerializer.gson().serialize(toAdventure(clientVersion));
            return jsonModern;
        } else {
            if (jsonLegacy == null) jsonLegacy = GsonComponentSerializer.gson().serialize(toAdventure(clientVersion));
            return jsonLegacy;
        }
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
        if (fixedFormat == null) fixedFormat = createFunction.apply(convert(ProtocolVersion.LATEST_KNOWN_VERSION)); // Numbers formats are 1.20.3+, which is above 1.16
        return (F) fixedFormat;
    }

    /**
     * Creates a text holder object using provided function if it does not exist and returns it.
     *
     * @param   convertFunction
     *          Function for converting adventure Component to TextHolder
     * @param   version
     *          Player version
     * @return  Converted TextHolder
     * @param   <T>
     *          TextHolder type
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T toTextHolder(@NotNull BiFunction<TabComponent, ProtocolVersion, T> convertFunction, @NotNull ProtocolVersion version) {
        if (textHolder == null) textHolder = convertFunction.apply(this, version);
        return (T) textHolder;
    }

    /**
     * Returns last color of this component. This value is cached.
     *
     * @return  Last color of this component
     */
    @NotNull
    public TextColor getLastColor() {
        if (lastColor == null) lastColor = fetchLastColor();
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
     * Converts this component into a string. RGB colors are represented as #RRGGBB.
     *
     * @return  String version of this component
     */
    @NotNull
    public abstract String toFlatText();

    /**
     * Converts this component into a string that only consists of text without any formatting.
     *
     * @return  String containing text of the component and extras
     */
    @NotNull
    public abstract String toRawText();

    /**
     * Returns last color of this component.
     *
     * @return  Last color of this component
     */
    @NotNull
    protected abstract TextColor fetchLastColor();

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
                    if (builder.length() > 0) {
                        component.setText(builder.toString());
                        components.add(component);
                        component = new StructuredComponent(component);
                        component.setText("");
                        component.getModifier().setFont(font);
                        builder = new StringBuilder();
                    }
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
                if (isHexCode(hex)) {
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
