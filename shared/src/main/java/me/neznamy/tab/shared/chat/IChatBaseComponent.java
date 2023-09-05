package me.neznamy.tab.shared.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.util.ComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class representing the n.m.s.IChatBaseComponent class to make work with it much easier
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor
public class IChatBaseComponent {

    /**
     * Component cache maps to avoid large memory allocations as well as
     * higher CPU usage when using animations which send the same text on repeat.
     */
    private static final ComponentCache<String, IChatBaseComponent> stringCache = new ComponentCache<>(1000, (text, clientVersion) -> {
                return text.contains("#") || text.contains("&x") || text.contains(EnumChatFormat.COLOR_CHAR + "x") || text.contains("<") ?
                    IChatBaseComponent.fromColoredText(text) : //contains RGB colors or font
                    new IChatBaseComponent(text); //no RGB
            });

    private static final ComponentCache<IChatBaseComponent, String> serializeCache = new ComponentCache<>(1000,
            (component, clientVersion) -> component.toString());

    public static final String EMPTY_COMPONENT = "{\"text\":\"\"}";

    private static final Pattern fontPattern = Pattern.compile("<font:(.*?)>(.*?)</font>");

    /** Text of the component */
    @Getter @Setter private String text;

    /** Chat modifier containing color, magic codes, hover and click event */
    @Getter @Setter @NotNull private ChatModifier modifier = new ChatModifier();

    /** Extra components used in "extra" field */
    @Nullable private List<IChatBaseComponent> extra;

    @Nullable private ProtocolVersion targetVersion;

    /**
     * Constructs a new component which is a clone of provided component
     *
     * @param   component
     *          component to clone
     */
    public IChatBaseComponent(@NotNull IChatBaseComponent component) {
        this.text = component.text;
        this.modifier = new ChatModifier(component.modifier);
        this.extra = component.extra == null ? null : component.extra.stream().map(IChatBaseComponent::new).collect(Collectors.toList());
        this.targetVersion = component.targetVersion;
    }

    /**
     * Constructs new instance with given text
     *
     * @param   text
     *          text to display
     */
    public IChatBaseComponent(String text) {
        this.text = text;
    }

    /**
     * Returns list of extra components. If no extra components are defined, returns empty list.
     *
     * @return  list of extra components
     */
    public @NotNull List<IChatBaseComponent> getExtra() {
        if (extra == null) return Collections.emptyList();
        return extra;
    }

    /**
     * Sets full list of extra components to given list. Does not allow empty list.
     *
     * @param   components
     *          components to use as extra
     * @return  self
     * @throws  IllegalArgumentException
     *          if {@code components} is an empty list
     */
    public @NotNull IChatBaseComponent setExtra(@NotNull List<IChatBaseComponent> components) {
        if (components.isEmpty()) throw new IllegalArgumentException("Unexpected empty array of components"); //exception taken from minecraft
        this.extra = components;
        return this;
    }

    /**
     * Converts the component to a string representing the serialized component.
     * This method is only used internally by json library since it's missing
     * protocol version field used by the method.
     *
     * @return  serialized component in string form
     * @see     #toString(ProtocolVersion)
     */
    @Override
    public @NotNull String toString() {
        JSONObject json = new JSONObject();
        if (text != null) json.put("text", text);
        json.putAll(modifier.serialize(targetVersion == null || targetVersion.getMinorVersion() >= 16));
        if (extra != null) json.put("extra", extra);
        return json.toString();
    }

    /**
     * Serializes this component with colors based on client version.
     * If client version is &lt;1.16, HEX colors will be converted to legacy colors.
     *
     * @param   clientVersion
     *          client version to adapt component for
     * @return  serialized string
     */
    public @NotNull String toString(@NotNull ProtocolVersion clientVersion) {
        if (extra == null && (text == null || text.length() == 0)) return EMPTY_COMPONENT;
        targetVersion = clientVersion;
        for (IChatBaseComponent child : getExtra()) {
            child.targetVersion = clientVersion;
        }
        return serializeCache.get(this, clientVersion);
    }

    /**
     * Returns organized component from colored text
     *
     * @param   originalText
     *          text to convert
     * @return  organized component from colored text
     */
    @NotNull
    public static IChatBaseComponent fromColoredText(@NotNull String originalText) {
        if (originalText.isEmpty()) return new IChatBaseComponent("");
        String remainingText = originalText;
        List<IChatBaseComponent> components = new ArrayList<>();
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
                        match.substring(match.indexOf(">")+1, match.length()-7),
                        match.substring(6, match.indexOf(">"))
                ));
                // Prepare the rest for next loop
                remainingText = remainingText.substring(m.start() + match.length());
            } else {
                components.addAll(toComponentArray(remainingText, null));
                break;
            }
        }
        return new IChatBaseComponent("").setExtra(components);
    }

    @NotNull
    private static List<IChatBaseComponent> toComponentArray(@NotNull String originalText, @Nullable String font) {
        String text = RGBUtils.getInstance().applyFormats(EnumChatFormat.color(originalText));
        List<IChatBaseComponent> components = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        IChatBaseComponent component = new IChatBaseComponent();
        component.modifier.setFont(font);
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
                        component = new IChatBaseComponent(component);
                        component.text = null;
                        component.modifier.setFont(font);
                        builder = new StringBuilder();
                    }
                    switch (format) {
                    case BOLD: 
                        component.modifier.setBold(true);
                        break;
                    case ITALIC: 
                        component.modifier.setItalic(true);
                        break;
                    case UNDERLINE: 
                        component.modifier.setUnderlined(true);
                        break;
                    case STRIKETHROUGH: 
                        component.modifier.setStrikethrough(true);
                        break;
                    case OBFUSCATED: 
                        component.modifier.setObfuscated(true);
                        break;
                    case RESET: 
                        component = new IChatBaseComponent();
                        component.modifier.setColor(new TextColor(EnumChatFormat.WHITE));
                        component.modifier.setFont(font);
                        break;
                    default:
                        component = new IChatBaseComponent();
                        component.modifier.setColor(new TextColor(format));
                        component.modifier.setFont(font);
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
                    component = new IChatBaseComponent();
                    component.modifier.setColor(color);
                    component.modifier.setFont(font);
                } else {
                    builder.append(c);
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
     * Converts this component into a simple text with legacy colors (the closest match if color is set to RGB)
     *
     * @return  The simple text format
     */
    public @NotNull String toLegacyText() {
        StringBuilder builder = new StringBuilder();
        append(builder, "");
        return builder.toString();
    }

    /**
     * Appends text to string builder, might also add color and magic codes if they are different
     * from previous component in chain.
     *
     * @param   builder
     *          builder to append text to
     * @param   previousFormatting
     *          colors and magic codes in previous component
     * @return  new formatting, might be identical to previous one
     */
    private @NotNull String append(@NotNull StringBuilder builder, @NotNull String previousFormatting) {
        String formatting = previousFormatting;
        if (text != null) {
            formatting = getFormatting();
            if (!formatting.equals(previousFormatting)) {
                builder.append(formatting);
            }
            builder.append(text);

        }
        for (IChatBaseComponent component : getExtra()) {
            formatting = component.append(builder, formatting);
        }
        return formatting;
    }

    /**
     * Returns colors and magic codes of this component
     *
     * @return  used colors and magic codes
     */
    private @NotNull String getFormatting() {
        StringBuilder builder = new StringBuilder();
        if (modifier.getColor() != null) {
            if (modifier.getColor().getLegacyColor() == EnumChatFormat.WHITE) {
                //preventing unwanted &r -> &f conversion and stopping the <1.13 client bug fix from working
                builder.append(EnumChatFormat.RESET.getFormat());
            } else {
                builder.append(modifier.getColor().getLegacyColor().getFormat());
            }
        }
        builder.append(modifier.getMagicCodes());
        return builder.toString();
    }

    /**
     * Returns raw text without colors, only works correctly when component is organized
     *
     * @return  raw text in this component and all child components
     */
    public @NotNull String toRawText() {
        StringBuilder builder = new StringBuilder();
        if (text != null) builder.append(text);
        for (IChatBaseComponent child : getExtra()) {
            if (child.text != null) builder.append(child.text);
        }
        return builder.toString();
    }

    /**
     * Returns the most optimized component based on text. Returns null if text is null,
     * organized component if RGB colors are used or simple component with only text field
     * containing the whole text when no RGB colors are used
     *
     * @param   text
     *          text to create component from
     * @return  The most performance-optimized component based on text
     */
    public static @NotNull IChatBaseComponent optimizedComponent(@NotNull String text) {
        return stringCache.get(text, null);
    }
}