package me.neznamy.tab.shared.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.util.ComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A component with structure (style, color, extra).
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor
public class StructuredComponent extends TabComponent {

    private static final ComponentCache<StructuredComponent, String> serializeCache = new ComponentCache<>(1000,
            (component, clientVersion) -> component.toString());

    /** Text of the component */
    @Getter
    @Setter
    @NotNull
    private String text = "";

    /** Chat modifier containing color, magic codes, hover and click event */
    @Getter
    @NotNull
    private ChatModifier modifier = new ChatModifier();

    /** Extra components used in "extra" field */
    @Nullable
    private List<StructuredComponent> extra;

    @Nullable
    private ProtocolVersion targetVersion;

    /**
     * Constructs a new component which is a clone of provided component
     *
     * @param   component
     *          component to clone
     */
    public StructuredComponent(@NotNull StructuredComponent component) {
        text = component.text;
        modifier = new ChatModifier(component.modifier);
        extra = component.extra == null ? null : component.extra.stream().map(StructuredComponent::new).collect(Collectors.toList());
        targetVersion = component.targetVersion;
    }

    /**
     * Constructs new instance with given text and extra components.
     *
     * @param   text
     *          Component text
     * @param   components
     *          Extra components
     */
    public StructuredComponent(@NotNull String text, @NotNull List<StructuredComponent> components) {
        this.text = text;
        if (components.isEmpty()) throw new IllegalArgumentException("Unexpected empty array of components"); //exception taken from minecraft
        extra = components;
    }

    /**
     * Returns list of extra components. If no extra components are defined, returns empty list.
     *
     * @return  list of extra components
     */
    public @NotNull List<StructuredComponent> getExtra() {
        if (extra == null) return Collections.emptyList();
        return extra;
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
        json.put("text", text);
        json.putAll(modifier.serialize(targetVersion == null || targetVersion.supportsRGB()));
        if (extra != null) json.put("extra", extra);
        return json.toString();
    }

    @Override
    @NotNull
    public String toString(@NotNull ProtocolVersion clientVersion) {
        targetVersion = clientVersion;
        for (StructuredComponent child : getExtra()) {
            child.targetVersion = clientVersion;
        }
        return serializeCache.get(this, clientVersion);
    }

    @Override
    @NotNull
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
    private @NotNull String append(@NotNull StringBuilder builder, @NotNull String previousFormatting) {
        String formatting = getFormatting();
        if (!formatting.equals(previousFormatting)) {
            builder.append(formatting);
        }
        builder.append(text);
        for (StructuredComponent component : getExtra()) {
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
                builder.append(EnumChatFormat.RESET);
            } else {
                builder.append(modifier.getColor().getLegacyColor());
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
        builder.append(text);
        for (StructuredComponent child : getExtra()) {
            builder.append(child.text);
        }
        return builder.toString();
    }
}