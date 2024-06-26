package me.neznamy.tab.shared.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A component with structure (style, color, extra).
 */
@NoArgsConstructor
@Getter
public class StructuredComponent extends TabComponent {

    /** Text of the component */
    @Setter
    @NotNull
    private String text = "";

    /** Chat modifier containing color, magic codes, hover and click event */
    @NotNull
    private ChatModifier modifier = new ChatModifier();

    /** Extra components used in "extra" field */
    @Nullable
    private List<StructuredComponent> extra;

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

    @Override
    @NotNull
    public String toRawText() {
        StringBuilder builder = new StringBuilder(text);
        for (StructuredComponent extra : getExtra()) {
            builder.append(extra.toRawText());
        }
        return builder.toString();
    }

}