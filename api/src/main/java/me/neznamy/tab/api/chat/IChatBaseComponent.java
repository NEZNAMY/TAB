package me.neznamy.tab.api.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.api.util.ComponentCache;
import org.json.simple.JSONObject;

import java.util.*;

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
    private static final ComponentCache<String, IChatBaseComponent> stringCache = new ComponentCache<>(10000, (text, clientVersion) -> {
                return text.contains("#") || text.contains("&x") || text.contains(EnumChatFormat.COLOR_CHAR + "x") || text.contains("<") ?
                    IChatBaseComponent.fromColoredText(text) : //contains RGB colors
                    new IChatBaseComponent(text); //no RGB
            });
    private static final ComponentCache<IChatBaseComponent, String> serializeCache = new ComponentCache<>(10000,
            (component, clientVersion) -> component.toString());

    /** Text of the component */
    @Getter @Setter private String text;

    /** Chat modifier containing color, magic codes, hover and click event */
    @Getter @Setter @NonNull private ChatModifier modifier = new ChatModifier();

    /** Extra components used in "extra" field */
    private List<IChatBaseComponent> extra;

    /**
     * Constructs a new component which is a clone of provided component
     *
     * @param   component
     *          component to clone
     */
    public IChatBaseComponent(@NonNull IChatBaseComponent component) {
        this.text = component.text;
        this.modifier = new ChatModifier(component.modifier);
        for (IChatBaseComponent child : component.getExtra()) {
            addExtra(new IChatBaseComponent(child));
        }
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
    public List<IChatBaseComponent> getExtra() {
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
    public IChatBaseComponent setExtra(List<IChatBaseComponent> components) {
        if (components.isEmpty()) throw new IllegalArgumentException("Unexpected empty array of components"); //exception taken from minecraft
        this.extra = components;
        return this;
    }

    /**
     * Appends provided component as extra component
     *
     * @param   child
     *          component to append
     */
    public void addExtra(@NonNull IChatBaseComponent child) {
        if (extra == null) extra = new ArrayList<>();
        extra.add(child);
    }

    /**
     * Deserializes string and returns created component. If provided string is null, returns null.
     *
     * @param   json
     *          serialized string
     * @return  Deserialized component or null if input is null
     */
    public static IChatBaseComponent deserialize(String json) {
        if (json == null) return null;
        return new DeserializedChatComponent(json);
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
    public String toString() {
        JSONObject json = new JSONObject();
        if (text != null) json.put("text", text);
        if (modifier.getTargetVersion() == null) modifier.setTargetVersion(TabAPI.getInstance().getServerVersion()); //packet.toString() was called as a part of a debug message
        json.putAll(modifier.serialize());
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
    public String toString(ProtocolVersion clientVersion) {
        return toString(clientVersion, false);
    }

    /**
     * Serializes this component with colors based on client version.
     * If client version is &lt;1.16, HEX colors will be converted to legacy colors.
     *
     * @param   clientVersion
     *          client version to adapt component for
     * @param   sendTranslatableIfEmpty
     *          whether empty translatable should be sent if text is empty or not
     * @return  serialized string
     */
    public String toString(ProtocolVersion clientVersion, boolean sendTranslatableIfEmpty) {
        if (extra == null) {
            if (text == null) return null;
            if (text.length() == 0) {
                if (sendTranslatableIfEmpty) {
                    return "{\"translate\":\"\"}";
                } else {
                    return "{\"text\":\"\"}";
                }
            }
        }
        modifier.setTargetVersion(clientVersion);
        for (IChatBaseComponent child : getExtra()) {
            child.modifier.setTargetVersion(clientVersion);
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
    public static IChatBaseComponent fromColoredText(@NonNull String originalText) {
        String text = RGBUtils.getInstance().applyFormats(EnumChatFormat.color(originalText));
        List<IChatBaseComponent> components = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        IChatBaseComponent component = new IChatBaseComponent();
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
                        break;
                    default:
                        component = new IChatBaseComponent();
                        component.modifier.setColor(new TextColor(format));
                        break;
                    }
                }
            } else if (c == '#' && text.length() > i+6) {
                String hex = text.substring(i+1, i+7);
                if (RGBUtils.getInstance().isHexCode(hex)) {
                    TextColor color;
                    if (containsLegacyCode(text, i)) {
                        color = new TextColor(hex, EnumChatFormat.getByChar(text.charAt(i+8)));
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
                } else {
                    builder.append(c);
                }
            } else {
                builder.append(c);
            }
        }
        component.setText(builder.toString());
        components.add(component);
        return new IChatBaseComponent("").setExtra(components);
    }

    /**
     * Returns true if text contains legacy color request at defined RGB index start
     *
     * @param   text
     *          text to check
     * @param   i
     *          current index start
     * @return  true if legacy color is defined, false if not
     */
    private static boolean containsLegacyCode(String text, int i) {
        if (text.length() - i < 9 || text.charAt(i+7) != '|') return false;
        return EnumChatFormat.getByChar(text.charAt(i+8)) != null;
    }

    /**
     * Converts this component into a simple text with legacy colors (the closest match if color is set to RGB)
     *
     * @return  The simple text format
     */
    public String toLegacyText() {
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
    private String append(StringBuilder builder, String previousFormatting) {
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
    private String getFormatting() {
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
    public String toRawText() {
        StringBuilder builder = new StringBuilder();
        if (text != null) builder.append(text);
        for (IChatBaseComponent child : getExtra()) {
            if (child.text != null) builder.append(child.text);
        }
        return builder.toString();
    }

    /**
     * Converts the component into flat text with used colors (including rgb) and magic codes
     *
     * @return  converted text
     */
    public String toFlatText() {
        StringBuilder builder = new StringBuilder();
        if (modifier.getColor() != null) builder.append("#").append(modifier.getColor().getHexCode());
        builder.append(modifier.getMagicCodes());
        if (text != null) builder.append(text);
        for (IChatBaseComponent child : getExtra()) {
            builder.append(child.toFlatText());
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
    public static IChatBaseComponent optimizedComponent(String text) {
        return stringCache.get(text, null);
    }
}